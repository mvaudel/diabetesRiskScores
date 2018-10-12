package no.uib.drs.model.features;

import java.util.Arrays;
import java.util.HashMap;
import no.uib.drs.model.ScoringFeature;
import no.uib.drs.model.biology.Proxy;

/**
 * Feature scoring the status of a single allele.
 *
 * @author Marc Vaudel
 */
public class SingleAlleleFeature implements ScoringFeature {

    /**
     * The type of feature.
     */
    public static final String type = "SingleAlleleFeature";
    /**
     * The rsId of the snp to test.
     */
    private String rsId;
    /**
     * The effect allele.
     */
    private String allele;
    /**
     * The weight this snp confers to the score.
     */
    private double weight;

    /**
     * Constructor.
     *
     * @param rsId The rsId of the snp to test
     * @param allele The effect allele
     * @param weight The weight this snp confers to the score
     */
    public SingleAlleleFeature(String rsId, String allele, double weight) {

        this.rsId = rsId;
        this.allele = allele;
        this.weight = weight;

    }

    @Override
    public double getScoreContribution(String sample, HashMap<String, HashMap<String, String[]>> genotypeMap, HashMap<String, Proxy> proxiesMap) {

        int nAlleles = getGenotype(sample, genotypeMap, proxiesMap);

        return nAlleles * weight;

    }

    @Override
    public int getGenotype(String sample, HashMap<String, HashMap<String, String[]>> genotypeMap, HashMap<String, Proxy> proxiesMap) {

        Proxy proxy = proxiesMap.get(rsId);

        if (proxy != null) {

            HashMap<String, String[]> snpMap = genotypeMap.get(proxy.proxyId);

            if (snpMap != null) {

                String[] alleles = snpMap.get(sample);

                if (alleles != null) {

                    String proxyAllele = proxy.getProxyAllele(allele);

                    return (int) Arrays.stream(alleles)
                            .filter(sampleAllele -> sampleAllele.equals(proxyAllele))
                            .count();

                }
            }
        }

        return 0;
    }

    @Override
    public String[] getVariants() {
        return new String[]{rsId};
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public String getName() {
        return rsId;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public int nPossibilities() {
        return 3;
    }
}
