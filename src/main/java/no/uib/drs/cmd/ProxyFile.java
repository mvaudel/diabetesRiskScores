package no.uib.drs.cmd;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import no.uib.drs.DiabetesRiskScore;
import static no.uib.drs.io.Utils.getFileReader;
import static no.uib.drs.io.Utils.getVcfIndexFile;
import static no.uib.drs.io.Utils.lineSeparator;
import no.uib.drs.io.flat.SimpleFileReader;
import no.uib.drs.io.flat.SimpleGzWriter;
import no.uib.drs.io.json.SimpleObjectMapper;
import no.uib.drs.io.vcf.GenotypeProvider;
import no.uib.drs.io.vcf.VariantDetailsProvider;
import no.uib.drs.model.biology.Proxy;
import no.uib.drs.model.score.RiskScore;
import no.uib.drs.model.score.VariantFeatureMap;
import no.uib.drs.processing.ScoreComputer;
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

        } catch (Throwable e) {

            e.printStackTrace();
        }

    }
    
    private static void mapProxies(File scoreDetailsFile, File[] proxyFiles, File[] vcfFiles, File[] variantDetailsFiles, File destinationFile) {

        ProgressHandler progressHandler = new ProgressHandler();

        String mainTaskName = "1. Mapping proxies";
        progressHandler.start(mainTaskName);

        String taskName = "1.1 Loading score details";
        progressHandler.start(taskName);

        RiskScore riskScore = SimpleObjectMapper.read(scoreDetailsFile, RiskScore.class);

        progressHandler.end(taskName);

        taskName = "1.2 Loading proxies";
        progressHandler.start(taskName);

        HashMap<String, HashSet<String>> proxyMap = getProxyMap(proxyFiles);
        
        HashSet<String> allSnps = proxyMap.entrySet().stream()
                .flatMap(entry -> Stream.concat(Stream.of(entry.getKey()), entry.getValue().stream()))
                .collect(Collectors.toCollection(HashSet::new));

        progressHandler.end(taskName);

        taskName = "1.3 Loading variant details";
        progressHandler.start(taskName);

        VariantDetailsProvider variantDetailsProvider = new VariantDetailsProvider(allSnps, null);
        Arrays.stream(vcfFiles)
                .parallel()
                .forEach(vcfFile -> variantDetailsProvider.addVariants(vcfFile, vcfFile.getName()));

        IntStream.range(0, variantDetailsFiles.length)
                .parallel()
                .forEach(i -> variantDetailsProvider.addVariants(variantDetailsFiles[i], vcfFiles[i].getName()));

        progressHandler.end(taskName);

        taskName = "1.4 Setting up vcf file readers";
        progressHandler.start(taskName);

        GenotypeProvider genotypeProvider = new GenotypeProvider();
        Arrays.stream(vcfFiles).forEach(file -> genotypeProvider.addVcfFile(file, getVcfIndexFile(file)));

        progressHandler.end(taskName);

        taskName = "1.5 Selecting best proxy";
        progressHandler.start(taskName);
        
        
        
        progressHandler.end(taskName);

        taskName = "1.6 Exporting results";
        progressHandler.start(taskName);

        

        progressHandler.end(taskName);

        progressHandler.end(mainTaskName);

    }
    
    private static HashMap<String, String> selectBestProxy(HashMap<String, HashSet<String>> proxyMap, VariantDetailsProvider variantDetailsProvider) {
        
        return null;
        
    }
    
    private static HashMap<String, HashSet<String>> getProxyMap(File[] proxiesFiles) {
        
        return Arrays.stream(proxiesFiles)
                .collect(Collectors.toMap(
                        file -> getSnpId(file), 
                        file -> getProxies(file), 
                        (a, b) -> a, 
                        HashMap::new));
        
    }
    
    private static String getSnpId(File proxyFile) {
        
        String fileName = proxyFile.getName();
        
        return fileName.substring(0, fileName.indexOf(".snp"));
        
    }
    
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
