package no.uib.drs.model;

/**
 * A variant feature is a set of alleles that give a given weight to a score.
 *
 * @author Marc Vaudel
 */
public interface ScoringFeature {
    
    public double getScoreContribution(String[] alleles);
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
    /**
     * Returns the number of possibilities;
     */
    public int nPossibilities();
    
}
