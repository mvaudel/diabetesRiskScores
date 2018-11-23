package no.uib.drs.cmd;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;
import org.apache.commons.cli.CommandLine;

/**
 * Parses and stores the command line options.
 *
 * @author Marc Vaudel
 */
public class BuildScoreOptionsBean {

    /**
     * The file to import.
     */
    public final File inputFile;
    /**
     * The file to export.
     */
    public final File destinationFile;
    /**
     * The name of the score.
     */
    public final String name;
    /**
     * The version of the score.
     */
    public final String version;

    /**
     * Constructor. Parses the command line options and conducts minimal sanity check.
     * 
     * @param aLine a command line
     */
    public BuildScoreOptionsBean(CommandLine aLine) {
        
        // Check that mandatory options are provided

        for (BuildScoreOptions option : BuildScoreOptions.values()) {

            if (option.mandatory && !aLine.hasOption(option.opt)) {

                throw new IllegalArgumentException("No value found for mandatory option " + option.opt + " (" + option.longOpt + ")");

            }
        }

        
        // Score input
        
        String filePath = aLine.getOptionValue(BuildScoreOptions.input.opt);

        inputFile = new File(filePath);

        if (!inputFile.exists()) {

            throw new IllegalArgumentException("Input file (" + filePath + ") not found.");

        }

        
        // Output
        
        filePath = aLine.getOptionValue(BuildScoreOptions.out.opt);

        destinationFile = new File(filePath);

        if (!destinationFile.getParentFile().exists()) {

            throw new IllegalArgumentException("Output folder (" + destinationFile.getParent() + ") not found.");

        }
        
        
        // Name
        
        name = aLine.getOptionValue(BuildScoreOptions.name.opt);
        
        
        // Version
        
        version = aLine.getOptionValue(BuildScoreOptions.version.opt);
        
    }
}
