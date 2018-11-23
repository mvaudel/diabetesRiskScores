package no.uib.drs.model.features;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import no.uib.drs.model.ScoringFeature;

/**
 * Feature adding weight if haplotypic alleles are found.
 *
 * @author Marc Vaudel
 */
public class HaplotypeFeature implements ScoringFeature {

    /**
     * The type of feature.
     */
    public static final String type = "HaplotypeFeature";
    /**
     * The name of this feature.
     */
    private final String name;
    /**
     * The rsIds of the markers to test.
     */
    private final String[] rsIds;
    /**
     * The alleles required.
     */
    private final List<String>[] alleles;
    /**
     * The number of each allele for each snp.
     */
    private final HashMap<String, Long>[] allelesMap;
    /**
     * The weight to add to the score.
     */
    private final double weight;

    /**
     * Constructor.
     *
     * @param name The name of this feature
     * @param rsIds The rsIds of the snps to test
     * @param alleles The effect alleles
     * @param weight The weight to add to the score
     */
    public HaplotypeFeature(String name, String[] rsIds, List<String>[] alleles, double weight) {

        this.name = name;
        this.rsIds = rsIds;
        this.alleles = alleles;
        this.weight = weight;

        allelesMap = Arrays.stream(alleles)
                .map(allelesList -> allelesList.stream()
                        .collect(Collectors.groupingBy(
                                a -> a,
                                HashMap::new,
                                Collectors.counting())))
                .toArray(HashMap[]::new);

    }

    @Override
    public double getScoreContribution(List<String>[] alleles) {

        return IntStream.range(0, alleles.length)
                .allMatch(i -> isHaplotype(alleles[i], this.alleles[i], this.allelesMap[i])) ? weight : 0.0;

    }

    /**
     * Compares whether the alleles correspond to the given haplotype.
     *
     * @param sampleAlleles the sample alleles in a list
     * @param haplotypeAlleles the haplotype alleles in a list
     * @param haplotypeAllelesOccurrence map of the occurrences of the alleles in the haplotype
     *
     * @return a boolean indicating whether the alleles correspond to the given haplotype
     */
    public static boolean isHaplotype(List<String> sampleAlleles, List<String> haplotypeAlleles, HashMap<String, Long> haplotypeAllelesOccurrence) {

        if (sampleAlleles.size() != haplotypeAlleles.size()) {
            return false;
        }

        if (sampleAlleles.stream()
                .anyMatch(allele -> !haplotypeAllelesOccurrence.containsKey(allele))) {
            return false;
        }

        HashMap<String, Long> sampleAllelesOccurrence = sampleAlleles.stream()
                .collect(Collectors.groupingBy(
                        a -> a, 
                        HashMap::new, 
                        Collectors.counting()));
        
        return haplotypeAllelesOccurrence.entrySet().stream()
                .allMatch(entry -> sampleAllelesOccurrence.containsKey(entry.getKey()) && sampleAllelesOccurrence.get(entry.getKey()).equals(entry.getValue()));
        
    }

    @Override
    public String[] getVariants() {
        return rsIds;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }
    
    /**
     * Returns the single letter code for this feature.
     * 
     * @return 
     */
    public static char getSingleLetterCode() {
        return 'H';
    }
}
