package no.uib.drs.cmd;

import java.io.File;
import java.io.PrintWriter;
import no.uib.drs.DiabetesRiskScore;
import static no.uib.drs.io.Utils.lineSeparator;
import no.uib.drs.io.flat.SimpleFileWriter;
import no.uib.drs.model.score.RiskScore;
import no.uib.drs.model.score.VariantFeatureMap;
import no.uib.drs.utils.ProgressHandler;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

/**
 * This class writes the summary for a given score on a set of vcf files.
 *
 * @author Marc Vaudel
 */
public class ListMarkers {

    /**
     * Main method.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        if (args.length == 0
                || args.length == 1 && args[0].equals("-h")
                || args.length == 1 && args[0].equals("--help")) {

            printHelp();
            return;

        }

        if (args.length == 1 && args[0].equals("-v")
                || args.length == 1 && args[0].equals("--version")) {

            System.out.println(DiabetesRiskScore.getVersion());

            return;

        }

        try {

            Options lOptions = new Options();
            ComputeScoreOptions.createOptionsCLI(lOptions);
            CommandLineParser parser = new DefaultParser();
            CommandLine commandLine = parser.parse(lOptions, args);

            ListMarkersOptionsBean bean = new ListMarkersOptionsBean(commandLine);

            writeMarkers(bean.scoreDetailsFile, bean.destinationFile);

        } catch (Throwable e) {

            e.printStackTrace();
        }
    }

    /**
     * Computes the scores and writes them to the given file.
     *
     * @param scoreDetailsFile the file containing the score details
     * @param destinationFile the file where to write the scores
     */
    private static void writeMarkers(File scoreDetailsFile, File destinationFile) {

        ProgressHandler progressHandler = new ProgressHandler();

        String mainTaskName = "1. listing markers";
        progressHandler.start(mainTaskName);

        String taskName = "1.1 Loading score details";
        progressHandler.start(taskName);

        RiskScore riskScore = RiskScore.parseRiskScore(scoreDetailsFile);
        VariantFeatureMap variantFeatureMap = new VariantFeatureMap(riskScore);

        progressHandler.end(taskName);

        taskName = "1.2 Exporting markers";
        progressHandler.start(taskName);

        exportResults(variantFeatureMap, destinationFile);

        progressHandler.end(taskName);

        progressHandler.end(mainTaskName);

    }

    /**
     * Exports the score markers to the destination file.
     *
     * @param riskScore the risk score
     * @param variantFeatureMap the risk score
     * @param destinationFile the destination file
     */
    private static void exportResults(VariantFeatureMap variantFeatureMap, File destinationFile) {

        try (SimpleFileWriter writer = new SimpleFileWriter(destinationFile, false)) {
            
            variantFeatureMap.variantIds.stream()
                    .forEach(id -> writer.writeLine(id));

        }
    }

    /**
     * Prints basic help
     */
    private static void printHelp() {

        try (PrintWriter lPrintWriter = new PrintWriter(System.out)) {
            lPrintWriter.print(lineSeparator);
            lPrintWriter.print("==================================" + lineSeparator);
            lPrintWriter.print("        DiabetesRiskScores        " + lineSeparator);
            lPrintWriter.print("               ****               " + lineSeparator);
            lPrintWriter.print("           List Markers           " + lineSeparator);
            lPrintWriter.print("==================================" + lineSeparator);
            lPrintWriter.print(lineSeparator
                    + "The ListMarkers command line exports the variants needed for a given score." + lineSeparator
                    + lineSeparator
                    + "For documentation and bug report see https://github.com/mvaudel/diabetesRiskScores." + lineSeparator
                    + lineSeparator
                    + "----------------------"
                    + lineSeparator
                    + "OPTIONS"
                    + lineSeparator
                    + "----------------------" + lineSeparator
                    + lineSeparator);
            lPrintWriter.print(ComputeScoreOptions.getOptionsAsString());
            lPrintWriter.flush();
        }
    }
}
