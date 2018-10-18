package no.uib.drs.cmd;

import htsjdk.variant.vcf.VCFFileReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import no.uib.drs.io.Utils;
import no.uib.drs.io.json.SimpleObjectMapper;
import no.uib.drs.io.vcf.GenotypeProvider;
import no.uib.drs.io.vcf.VariantCoordinatesMap;
import no.uib.drs.io.vcf.VariantDetailsProvider;
import no.uib.drs.model.biology.Proxy;
import no.uib.drs.model.score.RiskScore;
import no.uib.drs.model.biology.Variant;
import no.uib.drs.model.score.VariantFeatureMap;
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
        

        taskName = "1.2 Loading samples details";
        progressHandler.start(taskName);

        VCFFileReader vcfFileReader = new VCFFileReader(vcfFiles.get(0));
        ArrayList<String> sampleNames = vcfFileReader.getFileHeader().getSampleNamesInOrder();
        vcfFileReader.close();

        progressHandler.end(taskName);
        

        String taskName = "1.1 Loading proxies";
        progressHandler.start(taskName);

        HashMap<String, String> proxyIds = Proxy.getProxyMap(proxiesMapFile);

        progressHandler.end(taskName);
        

        taskName = "1.1 Loading variant details";
        progressHandler.start(taskName);;

        VariantDetailsProvider variantDetailsProvider = new VariantDetailsProvider;
        vcfFiles.stream()
                .parallel()
                .forEach(vcfFile -> variantDetailsProvider.addVariants(vcfFile, vcfFile.getName(), variantFeatureMap, proxyIds));
        
        HashMap<String, Proxy> proxyMap = Proxy.getProxyMap(proxyIds, HashMap<String, VariantDetailsProvider> );

        progressHandler.end(taskName);
        

        taskName = "1.4 Setting up vcf file readers";
        progressHandler.start(taskName);

        GenotypeProvider genotypeProvider = new GenotypeProvider();
        vcfFilesPath.forEach(filePath -> genotypeProvider.addVcfFile(new File(filePath), Utils.getVcfIndexFile(filePath));

        progressHandler.end(taskName);

        taskName = "1." + progress++ + " Closing connection to files";
        progressHandler.start(taskName);

        genotypeProviders.values()
                .forEach(genotypeProvider -> genotypeProvider.close());

        progressHandler.end(taskName);

        progressHandler.end(mainTaskName);

    }
}
