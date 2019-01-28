package no.uib.drs.cmd;

import java.io.File;
import org.apache.commons.cli.CommandLine;

/**
 * Parses and stores the command line options.
 *
 * @author Marc Vaudel
 */
public class ListMarkersOptionsBean {

    /**
     * The file containing the score details.
     */
    public final File scoreDetailsFile;
    /**
     * The file where to write the scores.
     */
    public final File destinationFile;

    /**
     * Constructor. Parses the command line options and conducts minimal sanity
     * check.
     *
     * @param aLine a command line
     */
    public ListMarkersOptionsBean(CommandLine aLine) {

        // Check that mandatory options are provided
        for (ListMarkersOptions option : ListMarkersOptions.values()) {

            if (option.mandatory && !aLine.hasOption(option.opt)) {

                throw new IllegalArgumentException("No value found for mandatory option " + option.opt + " (" + option.longOpt + ")");

            }
        }

        // Score definition
        String filePath = aLine.getOptionValue(ProxyFileOptions.score.opt);

        scoreDetailsFile = new File(filePath);

        if (!scoreDetailsFile.exists()) {

            throw new IllegalArgumentException("Score definition file (" + filePath + ") not found.");

        }

        // Output
        filePath = aLine.getOptionValue(ProxyFileOptions.out.opt);

        destinationFile = new File(filePath);

        if (!destinationFile.getParentFile().exists()) {

            throw new IllegalArgumentException("Output folder (" + destinationFile.getParent() + ") not found.");

        }
    }
}
