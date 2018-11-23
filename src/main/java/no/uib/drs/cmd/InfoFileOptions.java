package no.uib.drs.cmd;

import java.util.Arrays;
import static no.uib.drs.io.Utils.lineSeparator;
import org.apache.commons.cli.Options;

/**
 * Enum of the different options
 *
 * @author Marc Vaudel
 */
public enum InfoFileOptions {

    vcf("g", "geno", "The genotype files in vcf format as comma separated list.", true, true),
    out("o", "out", "File where to write the file.", true, true),
    vatriants("i", "info", "List of variants to include. All variants are used if not provided.", false, true),
    typed("t", "typed", "Attribute used to flag genotyped markers in the vcf file (e.g. 'TYPED' for imputation at Sanger, 'GENOTYPED' for imputation at umich).", false, true),
    filter("f", "filter", "Whether the typed attribute is in the filter or the info column (0 for info, 1 for filter e.g. '0' for imputation at Sanger, '1' for imputation at umich).", false, true),
    score("s", "score", "Attribute used for imputation score in the vcf file (e.g. 'INFO' for imputation at Sanger, 'R2' for imputation at umich).", false, true);

    /**
     * The short option.
     */
    public final String opt;
    /**
     * The long option.
     */
    public final String longOpt;
    /**
     * Explanation for the CLI option.
     */
    public final String description;
    /**
     * Boolean indicating whether the option is mandatory.
     */
    public final boolean mandatory;
    /**
     * Boolean indicating whether the option has an argument.
     */
    public final boolean hasArg;

    /**
     * Private constructor managing the various variables for the enum
     * instances.
     *
     * @param opt the sort option
     * @param longOpt the long option
     * @param description the description
     * @param mandatory is the option mandatory
     * @param hasArg has the option an argument
     */
    private InfoFileOptions(String opt, String longOpt, String description, boolean mandatory, boolean hasArg) {
        this.opt = opt;
        this.longOpt = longOpt;
        this.description = description;
        this.mandatory = mandatory;
        this.hasArg = hasArg;
    }

    /**
     * Creates the options for the command line interface based on the possible
     * values.
     *
     * @param options the apache options object
     */
    public static void createOptionsCLI(Options options) {

        for (InfoFileOptions option : values()) {

            options.addOption(option.opt, option.longOpt, option.hasArg, option.description);

        }
    }

    /**
     * Returns the options as a string.
     *
     * @return the options as a string
     */
    public static String getOptionsAsString() {

        final StringBuilder output = new StringBuilder();
        String formatter = "%-35s";

        output.append("General Options:");
        output.append(lineSeparator)
                .append(lineSeparator);
        
        output.append("-").append(String.format(formatter, "h (--help)")).append(" ").append("Shows a brief help message.").append(lineSeparator);
        output.append("-").append(String.format(formatter, "v (--version)")).append(" ").append("Shows the version of the tool.").append(lineSeparator);

        output.append(lineSeparator)
                .append(lineSeparator);
        output.append("Mandatory Options:");
        output.append(lineSeparator)
                .append(lineSeparator);

        Arrays.stream(values())
                .filter(option -> option.mandatory)
                .forEach(option -> output.append("-").append(String.format(formatter, option.opt + " (--" + option.longOpt + ")")).append(" ").append(option.description).append(lineSeparator));

        output.append(lineSeparator)
                .append(lineSeparator);
        output.append("Additional Options:");
        output.append(lineSeparator)
                .append(lineSeparator);

        Arrays.stream(values())
                .filter(option -> !option.mandatory)
                .forEach(option -> output.append("-").append(String.format(formatter, option.opt + " (--" + option.longOpt + ")")).append(" ").append(option.description).append(lineSeparator));

        return output.toString();
    }
}
