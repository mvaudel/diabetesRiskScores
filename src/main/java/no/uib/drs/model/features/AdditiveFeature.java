package no.uib.drs.model.features;

import java.util.Arrays;
import java.util.List;
import no.uib.drs.model.ScoringFeature;

/**
 * Feature adding weight based on the prevalence of the allele of a SNP.
 *
 * @author Marc Vaudel
 */
public class AdditiveFeature implements ScoringFeature {

    /**
     * The type of feature.
     */
    public static final String type = "AdditiveFeature";
    /**
     * The rsId of the snp to test.
     */
    private final String rsId;
    /**
     * The name of the locus associated to this variant.
     */
    private final String name;
    /**
     * The effect allele.
     */
    private final String allele;
    /**
     * The weight this snp confers to the score.
     */
    private final double weight;

    /**
     * Constructor.
     *
     * @param rsId The rsId of the snp to test
     * @param allele The effect allele
     * @param name The name of the locus associated to this variant
     * @param weight The weight this snp confers to the score
     */
    public AdditiveFeature(String rsId, String name, String allele, double weight) {

        this.rsId = rsId;
        this.name = name;
        this.allele = allele;
        this.weight = weight;

    }

    @Override
    public double getScoreContribution(List<String>[] alleles) {

        return weight * alleles[0].stream()
                .filter(sampleAllele -> sampleAllele.equals(allele))
                .count();

    }

    @Override
    public String[] getVariants() {
        return new String[]{rsId};
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public String getName() {
        return rsId;
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
        return 'A';
    }
}
