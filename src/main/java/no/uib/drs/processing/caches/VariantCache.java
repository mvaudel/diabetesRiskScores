package no.uib.drs.processing.caches;

import htsjdk.variant.variantcontext.Allele;
import java.util.HashMap;
import java.util.List;
import no.uib.drs.utils.SimpleSemaphore;

/**
 * Cache for variants that need to be kept in memory.
 *
 * @author Marc Vaudel
 */
public class VariantCache {
    
    private final HashMap<String, String[]> cache = new HashMap<>();
    
    private final SimpleSemaphore mutex = new SimpleSemaphore(1);
    
    public void acquire() {
        
        mutex.acquire();
        
    }
    
    public void release() {
        
        mutex.release();
        
    }
    
    public String[] getAlleles(String id) {
        
        return cache.get(id);
        
    }
    
    public void addAlleles(String id, String[] alleles) {
        
        cache.put(id, alleles);
        
    }
    
    public void removeVariant(String id) {
        
        cache.remove(id);
        
    }

}
