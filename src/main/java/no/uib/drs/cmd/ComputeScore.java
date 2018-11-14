package no.uib.drs.cmd;

import htsjdk.variant.vcf.VCFFileReader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.IntStream;
import no.uib.drs.io.Utils;
import no.uib.drs.io.flat.SimpleGzWriter;
import no.uib.drs.io.json.SimpleObjectMapper;
import no.uib.drs.io.vcf.GenotypeProvider;
import no.uib.drs.io.vcf.VariantDetailsProvider;
import no.uib.drs.model.biology.Proxy;
import no.uib.drs.model.score.RiskScore;
import no.uib.drs.model.score.VariantFeatureMap;
import no.uib.drs.processing.ScoreComputer;
import no.uib.drs.utils.ProgressHandler;

/**
 * This class writes the summary for a given score on a set of vcf files.
 *
 * @author Marc Vaudel
 */
public class ComputeScore {

    /**
     * Writes a summary.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public ComputeScore() {

    }

    private void writeScores(File scoreDetailsFile, File proxiesMapFile, ArrayList<File> vcfFiles, ArrayList<File> variantDetailsFiles, File destinationFile) {

        ProgressHandler progressHandler = new ProgressHandler();

        String mainTaskName = "1. Computing GRS";
        progressHandler.start(mainTaskName);

        String taskName = "1.1 Loading score details";
        progressHandler.start(taskName);

        RiskScore riskScore = SimpleObjectMapper.read(scoreDetailsFile, RiskScore.class);
        VariantFeatureMap variantFeatureMap = new VariantFeatureMap(riskScore);

        progressHandler.end(taskName);

        taskName = "1.2 Loading proxies";
        progressHandler.start(taskName);

        HashMap<String, String> proxyIds = Proxy.getProxyMap(proxiesMapFile);

        progressHandler.end(taskName);

        taskName = "1.3 Loading variant details";
        progressHandler.start(taskName);

        VariantDetailsProvider variantDetailsProvider = new VariantDetailsProvider(variantFeatureMap, proxyIds);
        vcfFiles.stream()
                .parallel()
                .forEach(vcfFile -> variantDetailsProvider.addVariants(vcfFile, vcfFile.getName()));
        
        HashMap<String, Proxy> proxiesMap = Proxy.getProxyMap(proxyIds, variantDetailsProvider);

        progressHandler.end(taskName);

        taskName = "1.4 Setting up vcf file readers";
        progressHandler.start(taskName);

        GenotypeProvider genotypeProvider = new GenotypeProvider();
        vcfFiles.forEach(file -> genotypeProvider.addVcfFile(file, Utils.getVcfIndexFile(file)));

        progressHandler.end(taskName);

        taskName = "1.5 compute score";
        progressHandler.start(taskName);

        ScoreComputer scoreComputer = new ScoreComputer(vcfFiles, variantDetailsProvider);
        double[] scores = scoreComputer.computeRiskScores(riskScore, proxiesMap);
        ArrayList<String> sampleNames = scoreComputer.getSampleNames();

        progressHandler.end(taskName);

        taskName = "1.6 Exporting results";
        progressHandler.start(taskName);

        exportResults(destinationFile, sampleNames, scores);

        progressHandler.end(taskName);

        progressHandler.end(mainTaskName);

    }

    /**
     * Exports the score results to the file.
     * 
     * @param destinationFile the destination file
     * @param sampleNames the name of the samples
     * @param scores the scores array
     */
    private void exportResults(File destinationFile, ArrayList<String> sampleNames, double[] scores) {

        try (SimpleGzWriter writer = new SimpleGzWriter(destinationFile)) {

            writer.writeLine("Sample", "Score");

            IntStream.range(0, sampleNames.size())
                    .forEach(i -> writer.writeLine(sampleNames.get(i), Double.toString(scores[i])));

        }

    }
}
