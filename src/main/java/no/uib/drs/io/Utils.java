package no.uib.drs.io;

import java.io.File;
import no.uib.drs.io.flat.SimpleFileReader;
import no.uib.drs.io.flat.readers.SimpleGzReader;
import no.uib.drs.io.flat.readers.SimpleTextReader;

/**
 * Utilities for reading and writing files.
 *
 * @author Marc Vaudel
 */
public class Utils {

    /**
     * Default encoding, cf the second rule.
     */
    public static final String encoding = "UTF-8";
    /**
     * Default separator
     */
    public static final String separator = "\t";
    
    /**
     * Returns the index file for the given vcf file.
     * 
     * @param vcfFilePath the complete vcf file path
     * 
     * @return the index file
     */
    public static File getVcfIndexFile(File vcfFilePath) {
        
        return new File(vcfFilePath + ".tbi");
        
    }
    
    /**
     * Returns a file reader for the given file. Gz reader if the file ends with ".gz", text reader otherwise.
     * 
     * @param file the file to read
     * 
     * @return a file reader for the given file
     */
    public static SimpleFileReader getFileReader(File file) {
        
        return file.getName().endsWith(".gz") ? new SimpleGzReader(file) : new SimpleTextReader(file);
        
    }
}
