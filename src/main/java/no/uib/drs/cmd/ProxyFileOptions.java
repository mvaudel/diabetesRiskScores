package no.uib.drs.cmd;

import java.util.Arrays;
import static no.uib.drs.io.Utils.lineSeparator;
import org.apache.commons.cli.Options;

/**
 * Enum of the different options
 *
 * @author Marc Vaudel
 */
public enum ProxyFileOptions {

    score("s", "score", "The score details file.", true, true),
    variants("i", "info", "Information file on the variants needed for the score and proxies.", true, true),
    out("o", "out", "File where to write the proxy mapping.", true, true),
    proxies("p", "proxies", "Proxies files (snpId.snp) or parent folder.", true, true);

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
    private ProxyFileOptions(String opt, String longOpt, String description, boolean mandatory, boolean hasArg) {
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

        for (ProxyFileOptions option : values()) {

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
