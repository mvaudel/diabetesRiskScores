package no.uib.drs.io.vcf;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import no.uib.drs.cmd.InfoFile;
import static no.uib.drs.io.Utils.getFileReader;
import no.uib.drs.io.flat.SimpleFileReader;
import no.uib.drs.model.biology.Variant;
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
     * The version of the info file format.
     */
    public static final String version = "0.0.1";
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
     */
    public void addVariants(File snpTable) {

        try (SimpleFileReader reader = getFileReader(snpTable)) {

            String vcfName = null;
            String version = null;

            String line;
            while ((line = reader.readLine()) != null && line.charAt(0) == '#') {

                int separator = line.indexOf(":");
                String key = line.substring(1, separator).trim();

                if (key.equalsIgnoreCase("vcf")) {

                    vcfName = line.substring(separator + 1).trim();

                } else if (key.equalsIgnoreCase("version")) {

                    version = line.substring(separator + 1).trim();

                }
            }

        if (vcfName == null) {
            throw new IllegalArgumentException("Name of the vcf file not found.");
        }

        if (version == null) {
            throw new IllegalArgumentException("Version of the info file not found.");
        }
        
        if (!version.equals(this.version)) {
            throw new IllegalArgumentException("Version of the info file (" + version + ") not compatible with this version of the tool (" + this.version + ").");
        }
            
            while ((line = reader.readLine()) != null && !allFound) {

                char[] lineChars = line.toCharArray();

                int nSeparators = 0,
                        lastSeparator = -1;

                String id = null;
                String chr = null;
                int bp = -1;
                String ref = null;
                String alt = null;
                boolean typed = false;
                double score = -1.0;
                double maf;

                OUTER:
                for (int i = 0; i < lineChars.length; i++) {

                    if (lineChars[i] == separator) {

                        nSeparators++;
                        
                        String input = new String(lineChars, lastSeparator + 1, i - lastSeparator - 1);

                        switch (nSeparators) {

                            case 1:
                                chr = input;
                                break;

                            case 2:
                                bp = Integer.parseInt(input);
                                break;

                            case 3:
                                id = input;

                                if (snpFound != null && !snpFound.containsKey(id)) {
                                    break OUTER;
                                }

                                break;

                            case 4:
                                ref = input;
                                break;

                            case 5:
                                alt = input;
                                break;

                            case 6:
                                typed = input.equals("1");
                                break;

                            case 7:
                                score = Double.parseDouble(input);
                                break;

                            default:

                        }

                        lastSeparator = i;

                    }
                }

                if (nSeparators == 5) {

                    maf = Double.parseDouble(new String(lineChars, lastSeparator + 1, lineChars.length - lastSeparator - 1));

                    Variant variant = new Variant(id, chr, bp, ref, alt, maf, typed, score);

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
