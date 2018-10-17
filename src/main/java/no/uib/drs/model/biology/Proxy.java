package no.uib.drs.model.biology;

import java.io.File;
import static java.io.File.separator;
import java.util.HashMap;
import java.util.Set;
import static no.uib.drs.io.Utils.getFileReader;
import no.uib.drs.io.flat.SimpleFileReader;

/**
 * This class contains information on the proxy of a variant.
 *
 * @author Marc Vaudel
 */
public class Proxy {

    /**
     * The id of the original snp.
     */
    public final String snpId;
    /**
     * The id of the proxy.
     */
    public final String proxyId;
    /**
     * Mapping of the snp alleles to proxy alleles.
     */
    private final HashMap<String, String> alleleMap;

    /**
     * Constructor.
     *
     * @param snpId the id of the snp
     * @param proxyId the id of the proxy
     * @param snpA the reference allele of the snp
     * @param snpB the alternative allele of the snp
     * @param proxyA the reference allele of the proxy
     * @param proxyB the alternative allele of the proxy
     */
    public Proxy(String snpId, String proxyId, String snpA, String snpB, String proxyA, String proxyB) {

        this.snpId = snpId;
        this.proxyId = proxyId;

        this.alleleMap = new HashMap<>(2);
        addAllele(snpA, proxyA);
        addAllele(snpB, proxyB);

    }

    /**
     * Adds an allele.
     *
     * @param snpAllele the snp allele
     * @param proxyAllele the proxy allele
     */
    public void addAllele(String snpAllele, String proxyAllele) {

        if (!snpAllele.equals("NA") && !proxyAllele.equals("NA")) {
            alleleMap.put(snpAllele, proxyAllele);
        }
    }

    /**
     * Returns the proxy alleles corresponding to a snp allele.
     *
     * @param snpAllele the snp allele
     *
     * @return the proxy allele
     */
    public String getProxyAllele(String snpAllele) {

        return alleleMap.get(snpAllele);

    }

    /**
     * Returns the snp alleles in the map.
     *
     * @return the snp alleles in the map
     */
    public Set<String> getSnpAlleles() {

        return alleleMap.keySet();

    }

    public static HashMap<String, String> getProxyMap(File proxyFile) {

        HashMap<String, String> result = new HashMap<>();
        
        try (SimpleFileReader reader = getFileReader(proxyFile)) {

            String line = reader.readLine();
            String[] lineSplit = line.split(separator);

            if (lineSplit.length != 2) {

                throw new IllegalArgumentException(lineSplit.length + " columns found in proxy file, two expected: id, proxy.");

            }

            while ((line = reader.readLine()) != null) {

                lineSplit = line.split(separator);

                if (lineSplit.length != 2) {

                    throw new IllegalArgumentException(lineSplit.length + " columns found in proxy file at line: " + line + ". Two expected: id, proxy.");

                }
                
                String snpId = lineSplit[0];
                String proxyId = lineSplit[1];
                
                if (result.containsKey(snpId)) {

                    throw new IllegalArgumentException("Two proxies found for " + snpId + ".");
                    
                }
                
                result.put(snpId, proxyId);

            }
        }
        
        return result;

    }
    
    public static HashMap<String, Proxy> getProxyMap(HashMap<Srting, String> idsMap, )

}
