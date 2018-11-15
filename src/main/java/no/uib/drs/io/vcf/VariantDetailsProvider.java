package no.uib.drs.io.vcf;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
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
     * Map of the markers found.
     */
    private final HashMap<String, Boolean> snpFound;
    /**
     * Boolean indicating whether all markers were found.
     */
    private boolean allFound = false;

    /**
     * Constructor.
     *
     * @param variants variants to load
     * @param proxyIds map of id to proxy
     */
    public VariantDetailsProvider(HashSet<String> variants, HashMap<String, String> proxyIds) {

        snpFound = variants.stream()
                .map(id -> proxyIds.containsKey(id) ? proxyIds.get(id) : id)
                .collect(Collectors.toMap(
                        id -> id,
                        id -> false,
                        (a, b) -> a,
                        HashMap::new));

    }

    /**
     * Parses the variant details from the given table and stores them in the
     * internal maps.
     *
     * @param snpTable the snp table as exported from the genotyping pipeline
     * @param vcfName the name of the vcf file
     */
    public void addVariants(File snpTable, String vcfName) {

        try (SimpleFileReader reader = getFileReader(snpTable)) {

            String line = reader.readLine();
            while ((line = reader.readLine()) != null && !allFound) {

                char[] lineChars = line.toCharArray();

                int nSeparators = 0,
                        lastSeparator = -1;

                String id = null;
                String chr = null;
                int bp = -1;
                String ref = null;
                String alt = null;
                double maf;

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

                                if (snpFound != null && !snpFound.containsKey(id)) {
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

                    if (snpFound != null) {

                        snpFound.put(id, true);

                        allFound = snpFound.values().stream()
                                .allMatch(a -> a);

                    }

                    mutex.release();

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
