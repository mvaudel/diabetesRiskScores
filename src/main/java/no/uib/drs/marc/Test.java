package no.uib.drs.marc;

import java.io.File;
import no.uib.drs.cmd.ListMarkers;
import no.uib.drs.model.score.CdpkScore;

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

        CdpkScore riskScore = CdpkScore.parseScore(new File("resources\\scores\\Type2Diabetes_PRS_LDpred_rho0.01_v3.txt.gz"));
        
    }
}
