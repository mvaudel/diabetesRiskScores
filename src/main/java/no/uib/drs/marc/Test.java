package no.uib.drs.marc;

import java.io.File;
import no.uib.drs.cmd.ListMarkers;

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
        File destinationFile = new File("docs/T1D-GRS2_makers");
        
        String[] cli = new String[]{"-s", scoreFile.getAbsolutePath(), "-o", destinationFile.getAbsolutePath()};
        
        ListMarkers.main(cli);
        
    }
}
