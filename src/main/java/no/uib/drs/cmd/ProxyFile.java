package no.uib.drs.cmd;

import java.io.File;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.uib.drs.DiabetesRiskScore;
import static no.uib.drs.io.Utils.getFileReader;
import static no.uib.drs.io.Utils.lineSeparator;
import no.uib.drs.io.flat.SimpleFileReader;
import no.uib.drs.io.flat.SimpleGzWriter;
import no.uib.drs.io.vcf.VariantDetailsProvider;
import no.uib.drs.model.biology.Variant;
import no.uib.drs.utils.ProgressHandler;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

/**
 * This command line creates a file containing mapping of proxies.
 *
 * @author Marc Vaudel
 */
public class ProxyFile {

    /**
     * Main method.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        if (args.length == 0
                || args.length == 1 && args[0].equals("-h")
                || args.length == 1 && args[0].equals("--help")) {

            printHelp();
            return;

        }

        if (args.length == 1 && args[0].equals("-v")
                || args.length == 1 && args[0].equals("--version")) {

            System.out.println(DiabetesRiskScore.getVersion());

            return;

        }

        try {

            Options lOptions = new Options();
            ProxyFileOptions.createOptionsCLI(lOptions);
            CommandLineParser parser = new DefaultParser();
            CommandLine commandLine = parser.parse(lOptions, args);

            ProxyFileOptionsBean bean = new ProxyFileOptionsBean(commandLine);

            mapProxies(bean.proxiesMapFiles, bean.variantDetailsFiles, bean.destinationFile);

        } catch (Throwable e) {

            e.printStackTrace();
        }

    }

    /**
     * Creates a file containing the proxy mapping
     *
     * @param proxyFiles the proxy files
     * @param variantDetailsFiles the variant details files
     * @param destinationFile the destination file
     */
    private static void mapProxies(File[] proxyFiles, File[] variantDetailsFiles, File destinationFile) {

        ProgressHandler progressHandler = new ProgressHandler();

        String mainTaskName = "1. Mapping proxies";
        progressHandler.start(mainTaskName);

        String taskName = "1.1 Loading proxies";
        progressHandler.start(taskName);

        HashMap<String, HashSet<String>> proxyMap = getProxyMap(proxyFiles);

        HashSet<String> allSnps = proxyMap.entrySet().stream()
                .flatMap(entry -> Stream.concat(Stream.of(entry.getKey()), entry.getValue().stream()))
                .collect(Collectors.toCollection(HashSet::new));

        progressHandler.end(taskName);

        taskName = "1.2 Loading variant details";
        progressHandler.start(taskName);

        VariantDetailsProvider variantDetailsProvider = new VariantDetailsProvider(allSnps, null);
        Arrays.stream(variantDetailsFiles)
                .parallel()
                .forEach(file -> variantDetailsProvider.addVariants(file));

        progressHandler.end(taskName);

        taskName = "1.5 Selecting best proxy";
        progressHandler.start(taskName);

        TreeMap<String, String> bestProxyMap = selectBestProxy(proxyMap, variantDetailsProvider);

        progressHandler.end(taskName);

        taskName = "1.6 Exporting results";
        progressHandler.start(taskName);

        exportProxies(bestProxyMap, destinationFile);

        progressHandler.end(taskName);

        progressHandler.end(mainTaskName);

    }

    /**
     * Writes the proxy mapping to a file.
     *
     * @param proxies the proxies
     * @param destinationFile the file
     */
    private static void exportProxies(TreeMap<String, String> proxies, File destinationFile) {

        try (SimpleGzWriter writer = new SimpleGzWriter(destinationFile)) {

            writer.writeLine("ID", "proxy");

            proxies.entrySet()
                    .forEach(entry -> writer.writeLine(entry.getKey(), entry.getValue()));

        }
    }

    /**
     * Selects the best proxy among the possible proxies
     *
     * @param proxyMap the snp to possible proxies map
     * @param variantDetailsProvider the variants details provider
     *
     * @return the snp to best proxy map
     */
    private static TreeMap<String, String> selectBestProxy(HashMap<String, HashSet<String>> proxyMap, VariantDetailsProvider variantDetailsProvider) {

        return proxyMap.entrySet().stream()
                .parallel()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), getBestProxy(entry.getKey(), entry.getValue(), variantDetailsProvider)))
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(
                        entry -> entry.getKey(),
                        entry -> entry.getValue(),
                        (a, b) -> a,
                        TreeMap::new));

    }

    /**
     * Selects the best proxy from the given set. The best proxy is either typed
     * or has best imputation score above the given threshold. Ties are broken
     * by taking the first alphabetically. Returns null if none is found.
     *
     * @param id the id of the original variant
     * @param proxies the possible proxies
     * @param variantDetailsProvider the variant details provider
     *
     * @return the best proxy
     */
    private static String getBestProxy(String id, HashSet<String> proxies, VariantDetailsProvider variantDetailsProvider) {

        Variant originalVariant = variantDetailsProvider.getVariant(id);

        if (originalVariant.genotyped) {

            return id;

        }

        String[] genotypedProxies = proxies.stream()
                .filter(snp -> variantDetailsProvider.getVariant(snp).genotyped)
                .sorted()
                .toArray(String[]::new);

        if (genotypedProxies.length > 0) {

            return genotypedProxies[0];

        }

        TreeSet<String> bestSnps = new TreeSet<>();
        bestSnps.add(id);
        double bestScore = originalVariant.imputationScore;

        for (String snp : proxies) {

            double score = variantDetailsProvider.getVariant(snp).imputationScore;

            if (score > originalVariant.imputationScore) {

                if (score > bestScore) {

                    bestSnps.clear();
                    bestScore = score;

                }

                if (score == bestScore) {

                    bestSnps.add(snp);

                }
            }
        }

        return bestSnps.isEmpty() ? null : bestSnps.first();

    }

    /**
     * Returns a map of tall proxies for each snps based on the given files.
     *
     * @param proxiesFiles an array of proxy files
     *
     * @return a snp to proxies map
     */
    private static HashMap<String, HashSet<String>> getProxyMap(File[] proxiesFiles) {

        return Arrays.stream(proxiesFiles)
                .collect(Collectors.toMap(
                        file -> getSnpId(file),
                        file -> getProxies(file),
                        (a, b) -> a,
                        HashMap::new));

    }

    /**
     * Returns the id of a snp based on a file name. Expected file name: id.snp.
     *
     * @param proxyFile the proxy file name
     *
     * @return the id of the snp
     */
    private static String getSnpId(File proxyFile) {

        String fileName = proxyFile.getName();

        return fileName.substring(0, fileName.indexOf(".snp"));

    }

    /**
     * Returns the proxies listed in a file along with the snp corresponding to
     * the file. Expected: one id per line.
     *
     * @param proxyFile the proxy file
     *
     * @return the proxies listed in a file as HashSet
     */
    private static HashSet<String> getProxies(File proxyFile) {

        HashSet<String> results = new HashSet<>(1);

        try (SimpleFileReader reader = getFileReader(proxyFile)) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (!(line = line.trim()).equals("")) {

                    results.add(line);

                }
            }
        }

        results.add(getSnpId(proxyFile));

        return results;

    }

    /**
     * Prints basic help
     */
    private static void printHelp() {

        try (PrintWriter lPrintWriter = new PrintWriter(System.out)) {
            lPrintWriter.print(lineSeparator);
            lPrintWriter.print("==================================" + lineSeparator);
            lPrintWriter.print("        DiabetesRiskScores        " + lineSeparator);
            lPrintWriter.print("               ****               " + lineSeparator);
            lPrintWriter.print("          Proxy Map File          " + lineSeparator);
            lPrintWriter.print("==================================" + lineSeparator);
            lPrintWriter.print(lineSeparator
                    + "The ProxyFile command line selects the best proxy for each variant needed in the score." + lineSeparator
                    + lineSeparator
                    + "For documentation and bug report see https://github.com/mvaudel/diabetesRiskScores." + lineSeparator
                    + lineSeparator
                    + "----------------------"
                    + lineSeparator
                    + "OPTIONS"
                    + lineSeparator
                    + "----------------------" + lineSeparator
                    + lineSeparator);
            lPrintWriter.print(ProxyFileOptions.getOptionsAsString());
            lPrintWriter.flush();
        }
    }

}
