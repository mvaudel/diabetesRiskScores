package no.uib.drs.model.score;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import no.uib.drs.io.Utils;
import no.uib.drs.io.flat.SimpleFileReader;
import no.uib.drs.model.ScoringFeature;
import no.uib.drs.model.features.AdditiveFeature;
import no.uib.drs.model.features.HaplotypeFeature;

/**
 * This class represents a GRS.
 *
 * @author Marc Vaudel
 */
public class RiskScore {

    /**
     * The name of the risk score.
     */
    public final String name;
    /**
     * The version of the risk score.
     */
    public final String version;
    /**
     * The pubmed id of the publication of the score.
     */
    public final String pmid;
    /**
     * The markers to use for scoring.
     */
    public final ScoringFeature[] features;

    /**
     * Constructs a GRS.
     *
     * @param name The name of the risk score
     * @param version The version of the risk score
     * @param pmid The pubmed id of the publication of the score
     * @param features The features to use for scoring
     */
    public RiskScore(String name, String version, String pmid, ScoringFeature[] features) {

        this.name = name;
        this.version = version;
        this.pmid = pmid;
        this.features = features;

    }

    /**
     * Imports the risk score from a text file.
     *
     * @param variantsTable the score definition as text file
     *
     * @return the risk score definition
     */
    public static RiskScore parseRiskScore(File variantsTable) {

        ArrayList<ScoringFeature> scoringFeatures = new ArrayList<>();
        String name = null;
        String version = null;
        String pmid = null;

        try (SimpleFileReader reader = Utils.getFileReader(variantsTable)) {

            int i = 0;

            String line;
            while ((line = reader.readLine()) != null && line.charAt(0) == '#') {

                i++;

                int separator = line.indexOf(":");

                if (separator > 1) {

                    String key = line.substring(1, separator).trim();

                    if (key.equalsIgnoreCase("name")) {

                        name = line.substring(separator + 1).trim();

                    } else if (key.equalsIgnoreCase("PMID")) {

                        pmid = line.substring(separator + 1).trim();

                    } else if (key.equalsIgnoreCase("version")) {

                        version = line.substring(separator + 1).trim();

                    }
                }
            }

            i++;

            if (name == null) {
                throw new IllegalArgumentException("Name of the score not found.");
            }

            if (version == null) {
                throw new IllegalArgumentException("Version of the score not found.");
            }

            if (pmid == null) {
                throw new IllegalArgumentException("PMID of the score not found.");
            }

            while ((line = reader.readLine()) != null) {

                i++;

                try {

                    line = line.trim();

                    if (!line.equals("") && line.charAt(0) != '#') {

                        String[] lineSplit = line.split(Utils.separator);

                        String rsId = lineSplit[0].trim();

                        String locus = lineSplit[1].trim();

                        String effectAllele = lineSplit[2].trim();

                        String weightString = lineSplit[3].trim();

                        String featureType = lineSplit[4].trim();
                        char featureSingleLetter = featureType.charAt(0);

                        double weight = Double.parseDouble(weightString);

                        String[] rsIdSplit = rsId.split(",");
                        List<String>[] alleleSplit = Arrays.stream(effectAllele.split(","))
                                .map(snpAlleles -> Arrays.asList(snpAlleles.split("\\|")))
                                .toArray(List[]::new);

                        if (featureSingleLetter == AdditiveFeature.getSingleLetterCode()) {

                            scoringFeatures.add(new AdditiveFeature(rsId, locus, effectAllele, weight));

                        } else if (featureSingleLetter == HaplotypeFeature.getSingleLetterCode()) {

                            scoringFeatures.add(new HaplotypeFeature(locus, rsIdSplit, alleleSplit, weight));

                        } else {

                            throw new IllegalArgumentException("Feature code " + featureSingleLetter + " not supported.");

                        }
                    }
                } catch (Exception e) {
                    System.out.println("An error occurred while pasing line " + i + " in score " + variantsTable.getName() + ".");
                    throw (e);
                }
            }
        }

        ScoringFeature[] featuresArray = scoringFeatures.stream().toArray(ScoringFeature[]::new);

        return new RiskScore(name, version, pmid, featuresArray);

    }

}
