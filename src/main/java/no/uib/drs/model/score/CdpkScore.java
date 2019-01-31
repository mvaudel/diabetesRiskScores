package no.uib.drs.model.score;

import java.io.File;
import java.util.HashMap;
import no.uib.drs.io.flat.SimpleFileReader;
import no.uib.drs.model.features.CdpkFeature;

/**
 * CDPK score.
 *
 * @author Marc Vaudel
 */
public class CdpkScore {

    /**
     * The expected header.
     */
    public final static String defaultHeader = "variant	effect_allele	effect_weight	chr	position_hg19	A1	A2";
/**
 * Map of the features.
 */
    public final HashMap<String, HashMap<Integer, HashMap<String, HashMap<String, CdpkFeature>>>> featureMap;
/**
 * Constructor.
 * 
 * @param featureMap map fo the features.
 */
    private CdpkScore(HashMap<String, HashMap<Integer, HashMap<String, HashMap<String, CdpkFeature>>>> featureMap) {

        this.featureMap = featureMap;

    }

    /**
     * Parses a CDPK weights file.
     * 
     * @param weightDetailsFile the weights file
     * 
     * @return a Cdpk score
     */
    public static CdpkScore parseScore(File weightDetailsFile) {

        HashMap<String, HashMap<Integer, HashMap<String, HashMap<String, CdpkFeature>>>> featureMap = new HashMap<>(22);

        try (SimpleFileReader reader = SimpleFileReader.getFileReader(weightDetailsFile)) {

            boolean header = true;

            String line;
            while ((line = reader.readLine()) != null) {

                line = line.trim();

                if (!line.equals("") && line.charAt(0) != '#') {

                    if (header) {

                        if (!line.equals(defaultHeader)) {

                            throw new IllegalArgumentException("Unexpected header. (Found: \"" + line + "\"; Expected: \"" + defaultHeader + "\")");

                        }

                        header = false;

                    } else {

                        String[] lineSplit = line.split("\t");

                        String chr = lineSplit[3];
                        int bp = Integer.parseInt(lineSplit[4]);
                        String a = lineSplit[5];
                        String b = lineSplit[6];

                        String name = lineSplit[0];
                        String effect = lineSplit[1];
                        double weight = Double.parseDouble(lineSplit[2]);
                        CdpkFeature feature = new CdpkFeature(name, effect, weight);

                        HashMap<Integer, HashMap<String, HashMap<String, CdpkFeature>>> chrMap = featureMap.get(chr);

                        if (chrMap == null) {

                            chrMap = new HashMap<>();
                            featureMap.put(chr, chrMap);

                        }

                        HashMap<String, HashMap<String, CdpkFeature>> bpMap = chrMap.get(bp);

                        if (bpMap == null) {

                            bpMap = new HashMap<>(1);
                            chrMap.put(bp, bpMap);

                        }

                        HashMap<String, CdpkFeature> aMap = bpMap.get(a);

                        if (aMap == null) {

                            aMap = new HashMap<>(1);
                            bpMap.put(a, aMap);

                        }

                        aMap.put(b, feature);

                    }
                }
            }

            return new CdpkScore(featureMap);

        }
    }
}
