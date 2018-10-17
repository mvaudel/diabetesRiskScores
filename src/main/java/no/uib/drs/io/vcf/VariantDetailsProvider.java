package no.uib.drs.io.vcf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static no.uib.drs.io.Utils.getFileReader;
import no.uib.drs.io.flat.SimpleFileReader;
import no.uib.drs.model.biology.Variant;
import no.uib.drs.model.score.VariantFeatureMap;

/**
 * This class returns information on the variant based on the snp tables of the
 * genotyping pipeline.
 *
 * @author Marc Vaudel
 */
public class VariantDetailsProvider {

    /**
     * Map of the variant details.
     */
    private HashMap<String, Variant> variantDetailsMap = new HashMap<>();

    /**
     * Constructor.
     *
     * @param variantDetailsMap the variants details map
     */
    private VariantDetailsProvider(HashMap<String, Variant> variantDetailsMap) {
        this.variantDetailsMap = variantDetailsMap;
    }

    /**
     * Parses the variant details from the given table.
     *
     * @param snpTable the snp table as exported from the genotyping pipeline
     * @param variantFeatureMap variant to features map
     * @param proxyIds map of id to proxy
     *
     * @return a VariantDetailsProvider
     */
    public static VariantDetailsProvider getVariantDetailsProvider(File snpTable, VariantFeatureMap variantFeatureMap, HashMap<String, String> proxyIds) {
        
        HashMap<String, Boolean> snpFound = Stream.concat(variantFeatureMap.variantIds.stream(), proxyIds.values().stream())
                .collect(Collectors.toMap(
                        id -> id, 
                        id -> false, 
                        (a, b) -> a, 
                        HashMap::new));

        HashMap<String, Variant> variantDetailsMap = new HashMap<>();

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

                    if (lineChars[i] == '\t') {

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
                    variantDetailsMap.put(id, variant);
                    snpFound.put(id, true);
                    
                    if (snpFound.values().stream().allMatch(a -> a)) {
                        
                        break;
                        
                    }
                }
            }

            return new VariantDetailsProvider(variantDetailsMap);

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

}
