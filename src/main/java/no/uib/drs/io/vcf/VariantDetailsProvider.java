package no.uib.drs.io.vcf;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import static no.uib.drs.io.Utils.getFileReader;
import no.uib.drs.io.flat.SimpleFileReader;
import no.uib.drs.io.flat.SimpleFileWriter;
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
     * Map of the variant coordinates to vcf file name.
     */
    private final HashMap<String, HashMap<Integer, HashMap<String, HashMap<String, String>>>> coordinatesFileMap = new HashMap<>();
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
     * Set of all vcf file names.
     */
    public final HashSet<String> vcfFileNames = new HashSet<>();

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
     * Constructor.
     */
    public VariantDetailsProvider() {

        snpFound = null;

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
                    
                    mutex.acquire();
                    vcfFileNames.add(vcfName);
                    mutex.release();

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

                String[] lineSplit = line.split(SimpleFileWriter.separator);

                String chr = lineSplit[0];
                int bp = Integer.parseInt(lineSplit[1]);
                String id = lineSplit[2];
                String ref = lineSplit[3];
                String alt = lineSplit[4];
                double maf = Double.parseDouble(lineSplit[5]);
                boolean typed = lineSplit[6].equals("1");
                double score = Double.parseDouble(lineSplit[7]);

                Variant variant = new Variant(id, chr, bp, ref, alt, maf, typed, score);

                mutex.acquire();

                variantDetailsMap.put(id, variant);
                variantFileMap.put(id, vcfName);
                addCoordinates(chr, bp, ref, alt, vcfName);

                if (snpFound != null) {

                    snpFound.put(id, true);

                    allFound = snpFound.values().stream()
                            .allMatch(a -> a);

                }

                mutex.release();

            }
        }
    }

    /**
     * Indexes the file in which the given coordinates can be found.
     * 
     * @param chr the chromosome name
     * @param bp the base pair
     * @param ref the ref allele
     * @param alt the alt allele
     * @param vcfName the vcf name
     */
    private void addCoordinates(String chr, int bp, String ref, String alt, String vcfName) {

        HashMap<Integer, HashMap<String, HashMap<String, String>>> chrMap = coordinatesFileMap.get(chr);

        if (chrMap == null) {

            chrMap = new HashMap<>();
            coordinatesFileMap.put(chr, chrMap);

        }

        HashMap<String, HashMap<String, String>> bpMap = chrMap.get(bp);

        if (bpMap == null) {

            bpMap = new HashMap<>(1);
            chrMap.put(bp, bpMap);

        }

        HashMap<String, String> refMap = bpMap.get(ref);

        if (refMap == null) {

            refMap = new HashMap<>(1);
            bpMap.put(ref, refMap);

        }

        refMap.put(alt, vcfName);

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

    /**
     * Returns the name of the vcf file where the given variant can be found.
     *
     * @param chr the chromosome name
     * @param bp the base pair
     * @param ref the ref allele
     * @param alt the alt allele
     *
     * @return the name of the vcf file where the given variant can be found
     */
    public String getVcfName(String chr, int bp, String ref, String alt) {
        
        HashMap<Integer, HashMap<String, HashMap<String, String>>> chrMap = coordinatesFileMap.get(chr);
        
        if (chrMap == null) {
            return null;
        }
        
        HashMap<String, HashMap<String, String>> bpMap = chrMap.get(bp);
        
        if (bpMap == null) {
            return null;
        }
        
        HashMap<String, String> refMap = bpMap.get(ref);
        
        if (refMap == null) {
            return null;
        }
        
        return refMap.get(alt);
        
    }

}