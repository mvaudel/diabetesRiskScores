package no.uib.drs.processing;

import no.uib.drs.processing.caches.VariantCache;
import no.uib.drs.processing.caches.ScoreProgress;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import no.uib.drs.io.Utils;
import static no.uib.drs.io.Utils.getVcfIndexFile;
import no.uib.drs.model.ScoringFeature;
import no.uib.drs.model.score.RiskScore;
import no.uib.drs.model.biology.Proxy;
import no.uib.drs.model.score.VariantFeatureMap;
import no.uib.drs.processing.caches.ProxyCoordinates;

/**
 * Computes risk scores.
 *
 * @author Marc Vaudel
 */
public class ScoreComputer {

    public static double[] computeRiskScores(RiskScore riskScore, ArrayList<File> vcfFiles, ArrayList<String> sampleNames, HashMap<String, Proxy[]> proxiesMap) {

        // Intermediate scores map
        final HashMap<String, HashMap<String, Double>> intermediateScoresMap = new HashMap<>(vcfFiles.size());
        vcfFiles.forEach(file -> intermediateScoresMap.put(file.getName(), new HashMap<>(sampleNames.size())));

        // Variant to feature mapping
        VariantFeatureMap variantFeatureMap = new VariantFeatureMap(riskScore);

        // Various caches
        VariantCache variantCache = new VariantCache();
        ScoreProgress scoreProgress = new ScoreProgress();

        // Iterate vcf files
        vcfFiles.parallelStream()
                .forEach(vcfFile -> {

                    String fileName = vcfFile.getName();

                    try (VCFFileReader vcfFileReader = new VCFFileReader(vcfFile)) {

                        // Set up scores
                        HashMap<String, Double> intermediateScores = intermediateScoresMap.get(fileName);
                        ArrayList<String> vcfSamples = vcfFileReader.getFileHeader().getSampleNamesInOrder();
                        HashMap<String, Integer> sampleIndexes = sampleNames.stream()
                                .collect(Collectors.toMap(
                                        id -> id,
                                        id -> vcfSamples.indexOf(id),
                                        (a, b) -> a,
                                        HashMap::new));
                        sampleNames.forEach(sampleId -> {
                            intermediateScores.put(sampleId, 0.0);
                            if (!sampleIndexes.containsKey(sampleId)) {
                                throw new IllegalArgumentException(sampleId + " not found in " + fileName + ".");
                            }
                        });

                        // Set up cache for proxies
                        ProxyCoordinates proxyCoordinates = new ProxyCoordinates(proxiesMap);

                        // Iterate variants
                        try (CloseableIterator<VariantContext> iterator = vcfFileReader.iterator()) {

                            while (iterator.hasNext()) {

                                // Get variant
                                VariantContext variantContext = iterator.next();
                                String variantId = variantContext.getID();

                                // Check whether it is used in the score 
                                if (variantFeatureMap.variantIds.contains(variantId)) {

                                    List<Allele> alleles = variantContext.getAlleles();

                                    // Iterate the features where this variant can be used
                                    for (String featureId : variantFeatureMap.variantToFeatureMap.get(variantId)) {

                                        ScoringFeature scoringFeature = variantFeatureMap.featureMap.get(featureId);

                                        String[] variantsNeeded = scoringFeature.getVariants();

                                        if (variantsNeeded.length == 1) {

                                            // Single variant scoring
                                            sampleNames.parallelStream()
                                                    .forEach(sampleId -> {
                                                        int i = sampleIndexes.get(sampleId);
                                                        Allele allele = alleles.get(i);

                                                        double scoreContribution = scoringFeature.
                                                    });

                                        } else {

                                            // Multiple variant scoring
                                        }

                                    }

                                }

                                // Check if it can be a proxy
                                Proxy proxy = proxyCoordinates.proxyIdToProxy.get(variantId);

                                if (proxy != null) {

                                    String originalVariant = proxy.snpId;

                                    // Keep coordinates for single variant features
                                    if (Arrays.stream(variantFeatureMap.variantToFeatureMap.get(variantId))
                                            .anyMatch(featureId -> variantFeatureMap.featureMap.get(featureId).getVariants().length == 1)) {

                                        proxyCoordinates.chrMap.put(variantId, variantContext.getContig());
                                        proxyCoordinates.bpMap.put(fileName, variantContext.getStart());

                                    }

                                    // Store genotype in cache for multiple variant features
                                    if (Arrays.stream(variantFeatureMap.variantToFeatureMap.get(variantId))
                                            .anyMatch(featureId -> variantFeatureMap.featureMap.get(featureId).getVariants().length > 1)) {

                                        List<Allele> alleles = variantContext.getAlleles();
                                        String[] sampleAlleles = sampleNames.stream()
                                                .map(sample -> alleles.get(sampleIndexes.get(sample)).getBaseString())
                                                .toArray(String[]::new);
                                        
                                        variantCache.acquire();
                                        variantCache.addAlleles(variantId, sampleAlleles);
                                        variantCache.release();

                                    }
                                }
                            }
                        }
                    }
                });

        return new double[1];

    }

}
