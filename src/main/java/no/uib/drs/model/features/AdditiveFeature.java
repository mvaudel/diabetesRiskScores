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
    private String rsId;
    /**
     * The effect allele.
     */
    private String allele;
    /**
     * The weight this snp confers to the score.
     */
    private double weight;

    /**
     * Constructor.
     *
     * @param rsId The rsId of the snp to test
     * @param allele The effect allele
     * @param weight The weight this snp confers to the score
     */
    public AdditiveFeature(String rsId, String allele, double weight) {

        this.rsId = rsId;
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

    @Override
    public int nPossibilities() {
        return 3;
    }
}
