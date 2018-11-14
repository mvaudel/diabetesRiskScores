package no.uib.drs.model.features;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.IntStream;
import no.uib.drs.model.ScoringFeature;
import no.uib.drs.model.biology.Proxy;

/**
 * Feature scoring the status of multiple alleles in distinct variants.
 *
 * @author Marc Vaudel
 */
public class MultipleAllelesFeature implements ScoringFeature {

    /**
     * The type of feature.
     */
    public static final String type = "MulatipleAllelesFeature";
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
    private final String[] alleles;
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
    public MultipleAllelesFeature(String name, String[] rsIds, String[] alleles, double weight) {

        this.name = name;
        this.rsIds = rsIds;
        this.alleles = alleles;
        this.weight = weight;

    }

    @Override
    public double getScoreContribution(String[] alleles) {
        
        return IntStream.range(0, alleles.length).allMatch(i -> this.alleles[i].equals(alleles[i])) ? weight : 0.0;
        
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

    @Override
    public int nPossibilities() {
        return 2;
    }

}
