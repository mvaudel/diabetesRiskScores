package no.uib.drs.processing.caches;

import java.util.HashSet;
import no.uib.drs.utils.SimpleSemaphore;

/**
 * This class keeps track of the features used while computing the score.
 *
 * @author Marc Vaudel
 */
public class ScoreProgress {
    
    private final HashSet<String> computedFeatures = new HashSet<>();
    
    private final SimpleSemaphore mutex = new SimpleSemaphore(1);
    
    public void acquire() {
        mutex.acquire();
    }
    
    public void release() {
        mutex.release();
    }
    
    public boolean isComputed(String featureId) {
        
        return computedFeatures.contains(featureId);
        
    }
    
    public void setComputed(String featureId) {
        
        computedFeatures.add(featureId);
        
    }

}
