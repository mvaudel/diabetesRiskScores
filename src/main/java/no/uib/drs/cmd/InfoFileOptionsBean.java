package no.uib.drs.cmd;

import java.io.File;
import no.uib.drs.io.vcf.VcfSettings;
import org.apache.commons.cli.CommandLine;

/**
 * Parses and stores the command line options.
 *
 * @author Marc Vaudel
 */
public class InfoFileOptionsBean {

    /**
     * The vcf file..
     */
    public final File vcfFile;
    /**
     * The file with the list of variants to select.
     */
    public final File snpFile;
    /**
     * The file where to write the scores.
     */
    public final File destinationFile;
    /**
     * The vcf parsing settings.
     */
    public final VcfSettings vcfSettings;

    /**
     * Constructor. Parses the command line options and conducts minimal sanity check.
     * 
     * @param aLine a command line
     */
    public InfoFileOptionsBean(CommandLine aLine) {
        
        // Check that mandatory options are provided

        for (InfoFileOptions option : InfoFileOptions.values()) {

            if (option.mandatory && !aLine.hasOption(option.opt)) {

                throw new IllegalArgumentException("No value found for mandatory option " + option.opt + " (" + option.longOpt + ")");

            }
        }

        
        // vcf file
        
        String filePath = aLine.getOptionValue(InfoFileOptions.vcf.opt);

        vcfFile = new File(filePath);

        if (!vcfFile.exists()) {

            throw new IllegalArgumentException("Score definition file (" + filePath + ") not found.");

        }

        
        // Output
        
        filePath = aLine.getOptionValue(InfoFileOptions.out.opt);

        destinationFile = new File(filePath);

        if (!destinationFile.getParentFile().exists()) {

            throw new IllegalArgumentException("Output folder (" + destinationFile.getParent() + ") not found.");

        }
        
        
        // vcf 
        
        String typedFlag = aLine.getOptionValue(InfoFileOptions.typed.opt);
        String filterString = aLine.getOptionValue(InfoFileOptions.filter.opt);
        String scoreFlag = aLine.getOptionValue(InfoFileOptions.score.opt);
                
        if (!filterString.equals("0") && !filterString.equals("1")) {

            throw new IllegalArgumentException("Input for filter not recognized. Supported input: 0, 1.");
            
        }
        
        vcfSettings = new VcfSettings(typedFlag, filterString.equals("1"), scoreFlag);
        
        
        // Variants file

        if (aLine.hasOption(InfoFileOptions.vatriants.opt)) {

            filePath = aLine.getOptionValue(InfoFileOptions.vatriants.opt);

            snpFile = new File(filePath);

            if (!snpFile.exists()) {

                throw new IllegalArgumentException("Variants file (" + filePath + ") not found.");

            }
        } else {
            snpFile = null;
        }
    }
}
