package no.uib.drs.model.features;

import java.util.HashMap;
import no.uib.drs.model.ScoringFeature;
import no.uib.drs.model.biology.Proxy;

/**
 * Feature scoring the status of two alleles in distinct variants.
 *
 * @author Marc Vaudel
 */
public class TwoAllelesFeature implements ScoringFeature {

    /**
     * The type of feature.
     */
    public static final String type = "DoubleAllelesFeature";
    /**
     * The name of this feature.
     */
    private final String name;
    /**
     * The rsId of the first snp to test.
     */
    private final String rsId1;
    /**
     * The rsId of the second snp to test.
     */
    private final String rsId2;
    /**
     * The effect allele of the first snp.
     */
    private final String allele1;
    /**
     * The effect allele of the second snp.
     */
    private final String allele2;
    /**
     * The weight to add to the score.
     */
    private final double weight;

    /**
     * Constructor.
     *
     * @param name The name of this feature
     * @param rsId1 The rsId of the first snp to test
     * @param allele1 The effect allele of the first snp
     * @param rsId2 The rsId of the second snp to test
     * @param allele2 The effect allele of the second snp
     * @param weight The weight to add to the score
     */
    public TwoAllelesFeature(String name, String rsId1, String allele1, String rsId2, String allele2, double weight) {

        this.name = name;
        this.rsId1 = rsId1;
        this.allele1 = allele1;
        this.rsId2 = rsId2;
        this.allele2 = allele2;
        this.weight = weight;

    }

    @Override
    public double getScoreContribution(String sample, HashMap<String, HashMap<String, String[]>> genotypeMap, HashMap<String, Proxy> proxiesMap) {

        boolean found1 = hasAllele(rsId1, allele1, sample, genotypeMap, proxiesMap);
        boolean found2 = hasAllele(rsId2, allele2, sample, genotypeMap, proxiesMap);

        return found1 && found2 ? weight : 0.0;

    }

    @Override
    public int getGenotype(String sample, HashMap<String, HashMap<String, String[]>> genotypeMap, HashMap<String, Proxy> proxiesMap) {

        boolean found1 = hasAllele(rsId1, allele1, sample, genotypeMap, proxiesMap);
        boolean found2 = hasAllele(rsId2, allele2, sample, genotypeMap, proxiesMap);

        return found1 && found2 ? 1 : 0;

    }

    /**
     * Indicates whether the given sample has the given allele for the given
     * snp.
     *
     * @param rsId the id of the snp
     * @param allele the expected allele combination separated by a pipe
     * @param sample the sample
     * @param genotypeMap a map of all genotypes
     * @param proxiesMap a map of snp proxies
     *
     * @return a boolean indicating whether the given sample has the given
     * allele for the given snp
     */
    private boolean hasAllele(String rsId, String allele, String sample, HashMap<String, HashMap<String, String[]>> genotypeMap, HashMap<String, Proxy> proxiesMap) {

        if (allele.equals("X")) {

            return true;

        }

        Proxy proxy = proxiesMap.get(rsId);

        if (proxy != null) {

            String[] alleleSplit = allele.split("\\|");

            if (alleleSplit.length == 1) {

                HashMap<String, String[]> snpMap = genotypeMap.get(proxy.proxyId);

                if (snpMap != null) {

                    String[] alleles = snpMap.get(sample);

                    if (alleles != null) {

                        String proxyAllele = proxy.getProxyAllele(allele);

                        if (proxyAllele == null) {
                            throw new IllegalArgumentException("No allele found in proxy for " + allele + " in " + rsId + ".");
                        }

                        return alleles[0].equals(proxyAllele) && !alleles[1].equals(proxyAllele)
                                || alleles[1].equals(proxyAllele) && !alleles[0].equals(proxyAllele);

                    }
                }

            } else if (alleleSplit.length == 2) {

                HashMap<String, String[]> snpMap = genotypeMap.get(proxy.proxyId);

                if (snpMap != null) {

                    String[] alleles = snpMap.get(sample);

                    if (alleles != null) {

                        String proxyAllele0 = proxy.getProxyAllele(alleleSplit[0]);
                        String proxyAllele1 = proxy.getProxyAllele(alleleSplit[1]);

                        return alleles[0].equals(proxyAllele0) && alleles[1].equals(proxyAllele1)
                                || alleles[0].equals(proxyAllele1) && alleles[1].equals(proxyAllele0);

                    }
                }

            } else {
                throw new IllegalArgumentException("Invalid allele " + allele + ".");
            }
        }

        return false;

    }

    @Override
    public String[] getVariants() {
        return new String[]{rsId1, rsId2};
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public int nPossibilities() {
        return 2;
    }

}
