package no.uib.drs.io.vcf;

/**
 * Settings for the parsing of a vcf file.
 *
 * @author Marc Vaudel
 */
public class VcfSettings {
    
    /**
     * The flag for the typed argument.
     */
    public final String typedFlag;
    /**
     * Boolean indicating whether the typed argument is in the filter or the info column.
     */
    public final boolean typedFilter;
    /**
     * The flag for the score.
     */
    public final String scoreFlag;
    
    /**
     * Constructor.
     * 
     * @param typedFlag the flag for the typed argument
     * @param typedFilter boolean indicating whether the typed argument is in the filter or the info column 
     * @param scoreFlag the flag for the score
     */
    public VcfSettings(String typedFlag, boolean typedFilter, String scoreFlag) {
        
        this.typedFlag = typedFlag;
        this.typedFilter = typedFilter;
        this.scoreFlag = scoreFlag;
        
    }

}
