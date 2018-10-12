package no.uib.drs.io.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import no.uib.drs.utils.SimpleSemaphore;

/**
 * Simple mapper to read and write objects in json. Exceptions are thrown as runnable exceptions.
 *
 * @author Marc Vaudel
 */
public class SimpleObjectMapper {

    /**
     * Mapper.
     */
    private static final ObjectMapper mapper = new ObjectMapper();
    
    /**
     * Reads an object from a file.
     * 
     * @param <K> the class to cast the object into
     * @param file the file
     * @param objectClass the object class
     * 
     * @return the object from the file
     */
    public synchronized static <K extends Object> K read(File file, Class<K> objectClass) {
        
        try {
        
            return mapper.readValue(file, objectClass);
            
        } catch (Throwable e) {
            
            throw new RuntimeException(e);
            
        }
    }
    
    /**
     * Writes an object to file.
     * 
     * @param <K> the class of the object to write
     * @param file the file where to write
     * @param object the object to write
     */
    public synchronized static <K extends Object> void write(File file, K object) {
        
        try {
        
            mapper.writeValue(file, object);
            
        } catch (Throwable e) {
            
            throw new RuntimeException(e);
            
        }
    }
}
