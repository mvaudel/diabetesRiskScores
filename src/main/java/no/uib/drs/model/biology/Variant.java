package no.uib.drs.model.biology;

/**
 * This class represents a marker that can be used to calculate a GRS.
 *
 * @author Marc Vaudel
 */
public class Variant {
    
    /**
     * The marker id.
     */
    public final String id;
    /**
     * The chromosome number.
     */
    public final String chr;
    /**
     * The base pair number.
     */
    public final int bp;
    /**
     * The effect allele.
     */
    public final String ref;
    /**
     * The effect allele.
     */
    public final String alt;
    /**
     * The estimated maf.
     */
    public final double maf;
    /**
     * Boolean indicating whether the marker was genotyped.
     */
    public final boolean genotyped;
    /**
     * Score of the imputation.
     */
    public final double imputationScore;
    
    /**
     * Constructor.
     * 
     * @param id The marker id
     * @param chr the chromosome number
     * @param bp the base pair number
     * @param ref The reference allele
     * @param alt The alternative allele
     * @param maf The estimated maf in this cohort
     * @param genotyped Boolean indicating whether the marker was genotyped
     * @param imputationScore Score of the imputation
     */
    public Variant(String id, String chr, int bp, String ref, String alt, double maf, boolean genotyped, double imputationScore) {
        
        this.id = id;
        this.chr = chr;
        this.bp = bp;
        this.ref = ref;
        this.alt = alt;
        this.maf = maf;
        this.genotyped = genotyped;
        this.imputationScore = imputationScore;
        
    }
    
    /**
     * Indicates whether the given variant id starts with "rs".
     * 
     * @param id the id to test
     * 
     * @return a boolean indicating whether the variant id starts with "rs".
     */
    public static boolean rsId(String id) {
        
        return id.length() > 2 && id.charAt(0) == 'r' && id.charAt(1) == 's';
        
    }

}
