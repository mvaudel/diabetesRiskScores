package no.uib.drs;

import java.io.IOException;
import java.io.InputStream;
import no.uib.drs.cmd.ComputeScore;

/**
 * Main class.
 *
 * @author Marc Vaudel
 */
public class DiabetesRiskScore {


    /**
     * Retrieves the version number set in the pom file.
     *
     * @return the version number of PeptideShaker
     */
    public static String getVersion() {

        java.util.Properties p = new java.util.Properties();

        try {

            InputStream is = (new DiabetesRiskScore()).getClass().getClassLoader().getResourceAsStream("drs.properties");
            p.load(is);

        } catch (IOException e) {

            e.printStackTrace();

        }

        return p.getProperty("drs.version");

    }
}
