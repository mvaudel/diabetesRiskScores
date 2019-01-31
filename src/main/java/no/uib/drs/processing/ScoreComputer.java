package no.uib.drs.processing;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
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
import no.uib.drs.io.vcf.VariantDetailsProvider;
import no.uib.drs.model.ScoringFeature;
import no.uib.drs.model.score.RiskScore;
import no.uib.drs.model.biology.Proxy;
import no.uib.drs.model.biology.Variant;
import no.uib.drs.model.features.CdpkFeature;
import no.uib.drs.model.score.CdpkScore;
import no.uib.drs.utils.SimpleSemaphore;

/**
 * Computes risk scores.
 *
 * @author Marc Vaudel
 */
public class ScoreComputer {

    /**
     * The ordered sample names as found in a vcf file.
     */
    public final ArrayList<String> sampleNames;
    /**
     * The scores.
     */
    public final double[] scores;
    /**
     * Variants that were missing.
     */
    public final HashSet<String> missingVariants = new HashSet<>();
    /**
     * The vcf readers linked to the vcf files.
     */
    private final HashMap<String, VCFFileReader> vcfFileReaders;
    /**
     * The variant details provider for the markers in the vcf files.
     */
    private final VariantDetailsProvider variantDetailsProvider;

    /**
     * Constructor
     *
     * @param vcfFiles the vcf files
     * @param variantDetailsProvider the variants details provider
     */
    public ScoreComputer(File[] vcfFiles, VariantDetailsProvider variantDetailsProvider) {

        this.variantDetailsProvider = variantDetailsProvider;

        vcfFileReaders = Arrays.stream(vcfFiles)
                .collect(Collectors.toMap(
                        file -> file.getName(),
                        file -> new VCFFileReader(file),
                        (a, b) -> a,
                        HashMap::new));

        sampleNames = vcfFileReaders.values().stream()
                .findAny()
                .get()
                .getFileHeader()
                .getSampleNamesInOrder();

        scores = new double[sampleNames.size()];

    }

    /**
     * Computes the risk score for all patients in the given vcf files.
     *
     * @param riskScore the risk score
     * @param proxiesMap the map of proxies
     */
    public void computeRiskScores(RiskScore riskScore, HashMap<String, Proxy> proxiesMap) {

        final SimpleSemaphore scoreMutex = new SimpleSemaphore(1);

        Arrays.stream(riskScore.features)
                .forEach(feature -> {

                    final String[] variantProxies = Arrays.stream(feature.getVariants())
                            .map(id -> proxiesMap.containsKey(id) ? proxiesMap.get(id).proxyId : id)
                            .toArray(String[]::new);

                    final List<Allele>[][] alleles = new List[sampleNames.size()][variantProxies.length];

                    for (int i = 0; i < variantProxies.length; i++) {

                        String id = variantProxies[i];
                        String vcfFileName = variantDetailsProvider.getVcfName(id);
                        Variant variant = variantDetailsProvider.getVariant(id);

                        VCFFileReader vcfFileReader = vcfFileReaders.get(vcfFileName);
                        boolean found = false;

                        try (CloseableIterator<VariantContext> iterator = vcfFileReader.query(variant.chr, variant.bp, variant.bp)) {

                            while (iterator.hasNext()) {

                                VariantContext variantContext = iterator.next();
                                String variantId = variantContext.getID();

                                if (variantId.equals(id)) {

                                    found = true;

                                    for (int j = 0; j < sampleNames.size(); j++) {

                                        Genotype genotypeType = variantContext.getGenotype(j);

                                        alleles[j][i] = genotypeType.getAlleles();

                                    }
                                }
                            }
                        }

                        if (!found) {

                            throw new IllegalArgumentException("Variant " + id + " not found in vcf file " + vcfFileName + ".");

                        }
                    }

                    scoreMutex.acquire();

                    IntStream.range(0, sampleNames.size())
                            .parallel()
                            .forEach(i -> {
                                scores[i] = scores[i] + feature.getScoreContribution(getAlleles(feature, alleles[i], proxiesMap));
                            });

                    scoreMutex.release();

                });
    }

    /**
     * Returns the allele of the original snp for a given sample.
     *
     * @param feature the scoring feature
     * @param alleles the alleles found for all samples
     * @param proxiesMap the map of proxies
     * @param i the index of the patient
     *
     * @return the allele of the original snp for a given sample
     */
    private List<String>[] getAlleles(ScoringFeature feature, List<Allele>[] alleles, HashMap<String, Proxy> proxiesMap) {

        String[] variants = feature.getVariants();
        List<String>[] sampleAlleles = new List[variants.length];

        for (int j = 0; j < variants.length; j++) {

            List<Allele> variantAlleles = alleles[j];

            Proxy proxy = proxiesMap.get(variants[j]);

            if (proxy != null) {

                sampleAlleles[j] = variantAlleles.stream()
                        .map(allele -> allele.getBaseString())
                        .map(allele -> proxy.getProxyAllele(allele))
                        .collect(Collectors.toList());

            } else {

                sampleAlleles[j] = variantAlleles.stream()
                        .map(allele -> allele.getBaseString())
                        .collect(Collectors.toList());

            }
        }

        return sampleAlleles;

    }

    /**
     * Computes the risk score for all patients in the given vcf files.
     *
     * @param riskScore the risk score
     */
    public void computeRiskScores(CdpkScore riskScore) {

        final HashMap<String, SimpleSemaphore> vcfMutexMap = new HashMap<>(variantDetailsProvider.vcfFileNames.size());
        variantDetailsProvider.vcfFileNames.forEach(vcfName -> vcfMutexMap.put(vcfName, new SimpleSemaphore(1)));

        final SimpleSemaphore scoreMutex = new SimpleSemaphore(1);

        final SimpleSemaphore missingMutex = new SimpleSemaphore(1);

        riskScore.featureMap.entrySet().stream()
                .parallel()
                .forEach(entryChr -> {

                    String chr = entryChr.getKey();
                    HashMap<Integer, HashMap<String, HashMap<String, CdpkFeature>>> chrMap = entryChr.getValue();

                    chrMap.entrySet().stream()
                            .parallel()
                            .forEach(entryBp -> {

                                int bp = entryBp.getKey();
                                HashMap<String, HashMap<String, CdpkFeature>> bpMap = entryBp.getValue();

                                bpMap.entrySet().forEach(aEntry -> {

                                    String a = aEntry.getKey();
                                    HashMap<String, CdpkFeature> aMap = aEntry.getValue();

                                    aMap.entrySet().forEach(bEntry -> {

                                        String b = bEntry.getKey();
                                        CdpkFeature feature = bEntry.getValue();
                                        String vcfFileName = variantDetailsProvider.getVcfName(chr, bp, a, b);

                                        if (vcfFileName != null) {

                                            VCFFileReader vcfFileReader = vcfFileReaders.get(vcfFileName);
                                            boolean found = false;

                                            SimpleSemaphore vcfMutex = vcfMutexMap.get(vcfFileName);
                                            vcfMutex.acquire();

                                            try (CloseableIterator<VariantContext> iterator = vcfFileReader.query(chr, bp, bp)) {

                                                while (iterator.hasNext()) {

                                                    VariantContext variantContext = iterator.next();
                                                    String ref = variantContext.getReference().getBaseString();

                                                    if (ref.equals(a)) {

                                                        boolean goodAlt = variantContext.getAlternateAlleles().stream()
                                                                .anyMatch(allele -> allele.getBaseString().equals(b));

                                                        if (goodAlt) {

                                                            found = true;

                                                            IntStream.range(0, sampleNames.size())
                                                                    .parallel()
                                                                    .forEach(i -> {

                                                                        Genotype genotypeType = variantContext.getGenotype(i);

                                                                        int n = (int) genotypeType.getAlleles().stream()
                                                                                .filter(allele -> allele.getBaseString().equals(feature.effectAllele))
                                                                                .count();

                                                                        scoreMutex.acquire();
                                                                        scores[i] = scores[i] + (n * feature.weight);
                                                                        scoreMutex.release();

                                                                    });
                                                        }
                                                    }
                                                }

                                                if (!found) {

                                                    missingMutex.acquire();
                                                    missingVariants.add(feature.name);
                                                    missingMutex.release();

                                                }
                                            }

                                            vcfMutex.release();

                                        }
                                    });
                                });
                            });
                });
    }

    /**
     * Closes the connection to files.
     */
    public void close() {

        vcfFileReaders.values().forEach(reader -> reader.close());

    }

}
