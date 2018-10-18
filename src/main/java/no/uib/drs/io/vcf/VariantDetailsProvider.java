package no.uib.drs.io.vcf;

import java.io.File;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static no.uib.drs.io.Utils.getFileReader;
import no.uib.drs.io.flat.SimpleFileReader;
import no.uib.drs.model.biology.Variant;
import no.uib.drs.model.score.VariantFeatureMap;
import no.uib.drs.utils.SimpleSemaphore;

/**
 * This class returns information on the variant based on the snp tables of the
 * genotyping pipeline.
 *
 * @author Marc Vaudel
 */
public class VariantDetailsProvider {

    /**
     * Column separator.
     */
    public static final char separator = '\t';

    /**
     * Map of the variant details.
     */
    private final HashMap<String, Variant> variantDetailsMap = new HashMap<>();
    /**
     * Map of the variant to vcf file name.
     */
    private final HashMap<String, String> variantFileMap = new HashMap<>();
    /**
     * Mutex for the edition of the maps.
     */
    private final SimpleSemaphore mutex = new SimpleSemaphore(1);

    /**
     * Constructor.
     *
     * @param variantDetailsMap the variants details map
     */
    private VariantDetailsProvider() {

    }

    /**
     * Parses the variant details from the given table and stores them in the internal maps.
     *
     * @param snpTable the snp table as exported from the genotyping pipeline
     * @param vcfName the name of the vcf file
     * @param variantFeatureMap variant to features map
     * @param proxyIds map of id to proxy
     */
    public synchronized void addVariants(File snpTable, String vcfName, VariantFeatureMap variantFeatureMap, HashMap<String, String> proxyIds) {

        HashMap<String, Boolean> snpFound = Stream.concat(variantFeatureMap.variantIds.stream(), proxyIds.values().stream())
                .collect(Collectors.toMap(
                        id -> id,
                        id -> false,
                        (a, b) -> a,
                        HashMap::new));

        try (SimpleFileReader reader = getFileReader(snpTable)) {

            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {

                char[] lineChars = line.toCharArray();

                int nSeparators = 0,
                        lastSeparator = -1;

                String id = null;
                String chr = null;
                int bp = -1;
                String ref = null;
                String alt = null;
                double maf = Double.NaN;

                OUTER:
                for (int i = 0; i < lineChars.length; i++) {

                    if (lineChars[i] == separator) {

                        nSeparators++;

                        switch (nSeparators) {

                            case 1:
                                chr = new String(lineChars, lastSeparator + 1, i - lastSeparator - 1);
                                break;

                            case 2:
                                bp = Integer.parseInt(new String(lineChars, lastSeparator + 1, i - lastSeparator - 1));
                                break;

                            case 3:
                                id = new String(lineChars, lastSeparator + 1, i - lastSeparator - 1);

                                if (!snpFound.containsKey(id)) {
                                    break OUTER;
                                }

                                break;

                            case 4:
                                ref = new String(lineChars, lastSeparator + 1, i - lastSeparator - 1);
                                break;

                            case 5:
                                alt = new String(lineChars, lastSeparator + 1, i - lastSeparator - 1);
                                break;

                            default:

                        }

                        lastSeparator = i;

                    }
                }

                if (nSeparators == 5) {

                    maf = Double.parseDouble(new String(lineChars, lastSeparator + 1, lineChars.length - lastSeparator - 1));

                    Variant variant = new Variant(id, chr, bp, ref, alt, maf);
                    
                    mutex.acquire();
                    variantDetailsMap.put(id, variant);
                    variantFileMap.put(id, vcfName);
                    mutex.release();
                    
                    snpFound.put(id, true);

                    if (snpFound.values().stream().allMatch(a -> a)) {

                        break;

                    }
                }
            }
        }
    }

    /**
     * Returns the variant with the given id, null if not found.
     *
     * @param id the variant id
     *
     * @return the variant with the given id
     */
    public Variant getVariant(String id) {
        return variantDetailsMap.get(id);
    }
    
    /**
     * Returns the name of the vcf file where the given variant can be found.
     * 
     * @param id the id of the variant
     * 
     * @return the name of the vcf file where the given variant can be found
     */
    public String getVcfName(String id) {
        return variantFileMap.get(id);
    }

}
