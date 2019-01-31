package no.uib.drs.model.features;

/**
 *
 * @author Marc Vaudel
 */
public class CdpkFeature {
    
    /**
     * The name of the feature.
     */
    public final String name;
    /**
     * The effect allele.
     */
    public final String effectAllele;
    /**
     * The weight.
     */
    public final double weight;
    
    /**
     * Constructor.
     * 
     * @param name The name
     * @param effectAllele The effect allele
     * @param weight The weight
     */
    public CdpkFeature(String name, String effectAllele, double weight) {
        
        this.name = name;
        this.effectAllele = effectAllele;
        this.weight = weight;
        
    }

}
