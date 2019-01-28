package no.uib.drs.marc;

import java.io.File;
import no.uib.drs.model.ScoringFeature;
import no.uib.drs.model.score.RiskScore;

/**
 *
 * @author Marc Vaudel
 */
public class Test {

    /**
     * Main method.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        File scoreFile = new File("resources/scores/T1D-GRS2");
        
        RiskScore riskScore = RiskScore.parseRiskScore(scoreFile);
        
        for (ScoringFeature feature : riskScore.features) {
            
            double test = feature.getWeight();
            
        }
        
    }
}
