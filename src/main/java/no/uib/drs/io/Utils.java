package no.uib.drs.io;

import java.io.File;

/**
 * Utilities for reading and writing files.
 *
 * @author Marc Vaudel
 */
public class Utils {

    /**
     * Encoding, cf the second rule.
     */
    public static final String encoding = "UTF-8";
    
    /**
     * Returns the index file for the given vcf file.
     * 
     * @param vcfFile the vcf file
     * 
     * @return the index file
     */
    public static File getVcfIndexFile(File vcfFile) {
        
        return new File(vcfFile.getAbsolutePath() + ".tbi");
        
    }
}
