package no.uib.drs.io.flat;

import java.io.File;
import no.uib.drs.io.flat.readers.SimpleGzReader;
import no.uib.drs.io.flat.readers.SimpleTextReader;

/**
 * Interface for file readers.
 *
 * @author Marc Vaudel
 */
public interface SimpleFileReader extends AutoCloseable {

    /**
     * Returns a file reader, text or gz, according to the extension of the file.
     * 
     * @param file the file
     * 
     * @return a file reader
     */
    public static SimpleFileReader getFileReader(File file) {
        
        if (file.getName().endsWith(".gz")) {
            
            return new SimpleGzReader(file);
            
        }
        
        return new SimpleTextReader(file);
        
    }

    /**
     * Reads a line of the file.
     *
     * @return a line of the file
     */
    public String readLine();

    @Override
    public void close();
}
