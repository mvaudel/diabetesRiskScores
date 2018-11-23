package no.uib.drs.model;

import java.util.List;

/**
 * A variant feature is a set of alleles that give a given weight to a score.
 *
 * @author Marc Vaudel
 */
public interface ScoringFeature {
    
    /**
     * Returns the score contribution for this feature.
     * 
     * @param alleles the alleles of the SNPs to score
     * 
     * @return the score contribution for this feature
     */
    public double getScoreContribution(List<String>[] alleles);
    /**
     * Returns the ids of the variants needed for this feature.
     * 
     * @return the ids of the variants needed for this feature
     */
    public String[] getVariants();
    /**
     * Returns the weight of this feature in the score.
     * 
     * @return the weight of this feature in the score
     */
    public double getWeight();
    /**
     * Returns a name for this feature.
     * 
     * @return a name for this feature
     */
    public String getName();
    /**
     * Returns the type of feature.
     * 
     * @return the type of feature
     */
    public String getType();
    
}
