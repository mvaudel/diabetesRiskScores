package no.uib.drs.model.features;

import java.util.Arrays;
import java.util.HashMap;
import no.uib.drs.model.ScoringFeature;
import no.uib.drs.model.biology.Proxy;

/**
 * Feature scoring the status of a single allele.
 *
 * @author Marc Vaudel
 */
public class SingleAlleleFeature implements ScoringFeature {

    /**
     * The type of feature.
     */
    public static final String type = "SingleAlleleFeature";
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
    public SingleAlleleFeature(String rsId, String allele, double weight) {

        this.rsId = rsId;
        this.allele = allele;
        this.weight = weight;

    }

    @Override
    public double getScoreContribution(String[] alleles) {

        return weight * Arrays.stream(alleles)
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
