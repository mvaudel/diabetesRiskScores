package no.uib.drs.model.score;

import no.uib.drs.model.ScoringFeature;

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
     * The markers to use for scoring.
     */
    public final ScoringFeature[] features;

    /**
     * Constructs a GRS.
     *
     * @param name The name of the risk score
     * @param version The version of the risk score
     * @param features The features to use for scoring
     */
    public RiskScore(String name, String version, ScoringFeature[] features) {

        this.name = name;
        this.version = version;
        this.features = features;

    }

}
