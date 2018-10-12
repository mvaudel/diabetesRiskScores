package no.uib.drs.processing.caches;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;
import no.uib.drs.model.biology.Proxy;

/**
 * Cache for the coordinates of proxies.
 *
 * @author Marc Vaudel
 */
public class ProxyCoordinates {
    
    public final HashMap<String, String> chrMap = new HashMap<>(0);
    public final HashMap<String, Integer> bpMap = new HashMap<>(0);

    public final HashMap<String, Proxy> proxyIdToProxy;
    
    public ProxyCoordinates(HashMap<String, Proxy[]> proxies) {
        
        proxyIdToProxy = proxies.values().stream()
                .flatMap(snpProxies -> Arrays.stream(snpProxies))
                .collect(Collectors.toMap(
                        proxy -> proxy.proxyId, 
                        proxy -> proxy, 
                        (a, b) -> a, 
                        HashMap::new));
        
    }
    
}
