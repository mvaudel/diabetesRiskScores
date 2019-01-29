package no.uib.drs.cmd;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import no.uib.drs.DiabetesRiskScore;
import static no.uib.drs.io.Utils.lineSeparator;
import no.uib.drs.io.flat.SimpleFileWriter;
import no.uib.drs.io.vcf.VariantDetailsProvider;
import no.uib.drs.model.biology.Proxy;
import no.uib.drs.model.biology.Variant;
import no.uib.drs.model.score.RiskScore;
import no.uib.drs.model.score.VariantFeatureMap;
import no.uib.drs.utils.ProgressHandler;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

/**
 * This class writes the summary for a given score on a set of vcf files.
 *
 * @author Marc Vaudel
 */
public class SanityCheck {

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
            ComputeScoreOptions.createOptionsCLI(lOptions);
            CommandLineParser parser = new DefaultParser();
            CommandLine commandLine = parser.parse(lOptions, args);

            ComputeScoreOptionsBean bean = new ComputeScoreOptionsBean(commandLine);

            sanityCheck(bean.scoreDetailsFile, bean.proxiesMapFile, bean.vcfFiles, bean.variantDetailsFiles, bean.destinationFile, bean.scoreThreshold);

        } catch (Throwable e) {

            e.printStackTrace();
        }
    }

    /**
     * Runs sanity check on the files provided.
     *
     * @param scoreDetailsFile the file containing the score details
     * @param proxiesMapFile the file containing the proxy mapping
     * @param vcfFiles the vcf files
     * @param variantDetailsFiles the variant details files
     * @param destinationFile the file where to write the scores
     * @param scoreThreshld the minimal imputation score to use
     */
    private static void sanityCheck(File scoreDetailsFile, File proxiesMapFile, File[] vcfFiles, File[] variantDetailsFiles, File destinationFile, double scoreThreshld) {

        ProgressHandler progressHandler = new ProgressHandler();

        String mainTaskName = "1. Sanity Check";
        progressHandler.start(mainTaskName);

        String taskName = "1.1 Loading score details";
        progressHandler.start(taskName);

        RiskScore riskScore = RiskScore.parseRiskScore(scoreDetailsFile);
        VariantFeatureMap variantFeatureMap = new VariantFeatureMap(riskScore);

        progressHandler.end(taskName);

        taskName = "1.2 Loading proxies";
        progressHandler.start(taskName);

        HashMap<String, String> proxyIds = proxiesMapFile == null ? new HashMap<>(0) : Proxy.getProxyMap(proxiesMapFile);

        progressHandler.end(taskName);

        taskName = "1.3 Loading variant details";
        progressHandler.start(taskName);

        VariantDetailsProvider variantDetailsProvider = new VariantDetailsProvider(variantFeatureMap.variantIds, proxyIds);
        Arrays.stream(variantDetailsFiles)
                .parallel()
                .forEach(file -> variantDetailsProvider.addVariants(file));

        HashMap<String, Proxy> proxiesMap = Proxy.getProxyMap(proxyIds, variantDetailsProvider);

        progressHandler.end(taskName);

        taskName = "1.4 Sanity checks";
        progressHandler.start(taskName);
        
        TreeSet<String> missing = new TreeSet<>();
        TreeSet<String> badImputation = new TreeSet<>();
        TreeMap<String, String> badProxy = new TreeMap<>();

        for (String id : variantFeatureMap.variantIds) {

            Proxy proxy = proxiesMap.get(id);

            String usedId = proxy == null ? id : proxy.proxyId;

            if (variantDetailsProvider.getVariant(usedId) == null) {
                missing.add(usedId);
            }

            Variant variant = variantDetailsProvider.getVariant(usedId);

            if (!variant.genotyped && !Double.isNaN(scoreThreshld) && variant.imputationScore < scoreThreshld) {
                if (proxy == null) {
                    badImputation.add(usedId);
                } else {
                    badProxy.put(proxy.proxyId, id);
                }
            }
        }

        progressHandler.end(taskName);

        taskName = "1.5 Exporting results";
        progressHandler.start(taskName);

        exportResults(destinationFile, missing, badImputation, badProxy);

        progressHandler.end(taskName);

        progressHandler.end(mainTaskName);

    }

    /**
     * Exports the score results to the file.
     *
     * @param destinationFile the destination file
     * @param missing the destination file
     * @param badImputation the destination file
     * @param badProxy the destination file
     */
    private static void exportResults(File destinationFile, TreeSet<String> missing, TreeSet<String> badImputation, TreeMap<String, String> badProxy) {

        try (SimpleFileWriter writer = new SimpleFileWriter(destinationFile, false)) {

            writer.writeLine("# Missing ids");
            missing.forEach(id -> writer.writeLine(id));
            writer.newLine();

            writer.writeLine("# Poor imputation");
            badImputation.forEach(id -> writer.writeLine(id));
            writer.newLine();

            writer.writeLine("# Poor proxy");
            badProxy.entrySet().forEach(entry -> writer.writeLine(entry.getKey(), entry.getValue()));
            writer.newLine();
            
        }
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
            lPrintWriter.print("     Sanity Check Command Line    " + lineSeparator);
            lPrintWriter.print("==================================" + lineSeparator);
            lPrintWriter.print(lineSeparator
                    + "The SanityCheck command line checks that everything is in place to compute the score." + lineSeparator
                    + lineSeparator
                    + "For documentation and bug report see https://github.com/mvaudel/diabetesRiskScores." + lineSeparator
                    + lineSeparator
                    + "----------------------"
                    + lineSeparator
                    + "OPTIONS"
                    + lineSeparator
                    + "----------------------" + lineSeparator
                    + lineSeparator);
            lPrintWriter.print(ComputeScoreOptions.getOptionsAsString());
            lPrintWriter.flush();
        }
    }
}
