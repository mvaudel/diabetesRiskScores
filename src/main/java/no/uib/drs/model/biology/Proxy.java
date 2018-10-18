package no.uib.drs.model.biology;

import java.io.File;
import static java.io.File.separator;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import no.uib.drs.io.Utils;
import static no.uib.drs.io.Utils.getFileReader;
import no.uib.drs.io.flat.SimpleFileReader;
import no.uib.drs.io.vcf.VariantDetailsProvider;

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

    /**
     * Parses the proxy file into a map of ids. Two columns expected: id proxy. Separator is set in the io.Utils class.
     * 
     * @param proxyFile the file to parse
     * 
     * @return variant id to proxy id in a map
     */
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
    
    /**
     * Converts the id mapping into proxy mapping after allele alignment based on the maf.
     * 
     * @param idsMap variant to proxy id map
     * @param variantDetailsProvider the variant details mapping
     * 
     * @return variant to proxy map.
     */
    public static HashMap<String, Proxy> getProxyMap(HashMap<String, String> idsMap, VariantDetailsProvider variantDetailsProvider) {
        
        return idsMap.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey(), 
                        entry -> getProxy(entry.getKey(), entry.getValue(), variantDetailsProvider), 
                        (a, b) -> a, 
                        HashMap::new));
        
    }
    
    /**
     * Returns the proxy object after alleles alignment.
     * 
     * @param originalId the original variant id
     * @param proxyId the proxy variant id
     * @param variantDetailsProvider a variant details provider
     * 
     * @return the proxy object
     */
    private static Proxy getProxy(String originalId, String proxyId, VariantDetailsProvider variantDetailsProvider) {
        
        Variant variant = variantDetailsProvider.getVariant(originalId);
        Variant proxy = variantDetailsProvider.getVariant(proxyId);
        
        String[] alignedAlleles = alignAlleles(variant, proxy);
        
        return new Proxy(
                                originalId, 
                                proxyId, 
                                variant.ref, 
                                variant.alt, 
                                alignedAlleles[0], 
                                alignedAlleles[1]);
        
    }
    
    /**
     * Aligns proxy alleles on variant alleles so that the maf is consistently smaller or higher than 0.5.
     * 
     * @param variant the original variant
     * @param proxy the proxy variant
     * 
     * @return the ref and alt alleles to use for the proxy in an array.
     */
    public static String[] alignAlleles(Variant variant, Variant proxy) {
        
        return Math.signum(variant.maf - 0.5) * Math.signum(proxy.maf - 0.5) < 0 
                ? new String[]{proxy.ref, proxy.alt}
                : new String[]{proxy.alt, proxy.ref};
        
    }

}
