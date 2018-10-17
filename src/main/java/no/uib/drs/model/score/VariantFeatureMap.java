package no.uib.drs.model.score;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import no.uib.drs.model.ScoringFeature;
import no.uib.drs.model.biology.Proxy;

/**
 * This class keeps track of which features needs which variants and vice-versa.
 *
 * @author Marc Vaudel
 */
public class VariantFeatureMap {

    /**
     * Name to feature map.
     */
    public final HashMap<String, ScoringFeature> featureMap;
    /**
     * Variant id to feature id map.
     */
    public final HashMap<String, String[]> variantToFeatureMap;
    /**
     * Ids of all variants in this score.
     */
    public final HashSet<String> variantIds;

    /**
     * Fills the map based on the given risk score.
     * 
     * @param riskScore a risk score
     */
    public VariantFeatureMap(RiskScore riskScore) {

        featureMap = new HashMap<>(riskScore.features.length);
        variantToFeatureMap = new HashMap<>(riskScore.features.length);
        variantIds = new HashSet<>(riskScore.features.length);

        for (ScoringFeature scoringFeature : riskScore.features) {

            String featureName = scoringFeature.getName();

            if (featureMap.containsKey(featureName)) {

                throw new IllegalArgumentException("Non-unique feature name: " + featureName + ".");

            }

            featureMap.put(featureName, scoringFeature);

            for (String variantId : scoringFeature.getVariants()) {

                variantIds.add(variantId);

                String[] variants = variantToFeatureMap.get(variantId);

                if (variants == null) {

                    variants = new String[1];

                } else {

                    variants = Arrays.copyOf(variants, variants.length + 1);

                }

                variants[variants.length - 1] = variantId;

            }
        }
    }
}
