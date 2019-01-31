package no.uib.drs.cmd;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;
import no.uib.drs.DiabetesRiskScore;
import static no.uib.drs.io.Utils.getVcfIndexFile;
import static no.uib.drs.io.Utils.lineSeparator;
import no.uib.drs.io.flat.SimpleFileWriter;
import no.uib.drs.io.vcf.GenotypeProvider;
import no.uib.drs.io.vcf.VariantDetailsProvider;
import no.uib.drs.model.score.CdpkScore;
import no.uib.drs.processing.ScoreComputer;
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
public class ComputeCdpkScore {

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

            ComputeScoreOptionsBean bean = new ComputeScoreOptionsBean(commandLine);

            computeScores(bean.scoreDetailsFile, bean.proxiesMapFile, bean.vcfFiles, bean.variantDetailsFiles, bean.destinationFile, bean.scoreThreshold);

        } catch (Throwable e) {

            e.printStackTrace();
        }
    }

    /**
     * Computes the scores and writes them to the given file.
     *
     * @param scoreDetailsFile the file containing the score details
     * @param proxiesMapFile the file containing the proxy mapping
     * @param vcfFiles the vcf files
     * @param variantDetailsFiles the variant details files
     * @param destinationFile the file where to write the scores
     * @param scoreThreshld the minimal imputation score to use
     */
    private static void computeScores(File scoreDetailsFile, File proxiesMapFile, File[] vcfFiles, File[] variantDetailsFiles, File destinationFile, double scoreThreshld) {

        ProgressHandler progressHandler = new ProgressHandler();

        String mainTaskName = "1. Computing GRS";
        progressHandler.start(mainTaskName);

        String taskName = "1.1 Loading score details";
        progressHandler.start(taskName);

        CdpkScore riskScore = CdpkScore.parseScore(scoreDetailsFile);

        progressHandler.end(taskName);

        taskName = "1.2 Loading variant details";
        progressHandler.start(taskName);

        VariantDetailsProvider variantDetailsProvider = new VariantDetailsProvider();
        Arrays.stream(variantDetailsFiles)
                .parallel()
                .forEach(file -> variantDetailsProvider.addVariants(file));

        progressHandler.end(taskName);

        taskName = "1.3 Sanity checks";
        progressHandler.start(taskName);

        // TODO
        progressHandler.end(taskName);

        taskName = "1.4 Setting up vcf file readers";
        progressHandler.start(taskName);

        GenotypeProvider genotypeProvider = new GenotypeProvider();
        Arrays.stream(vcfFiles).forEach(file -> genotypeProvider.addVcfFile(file, getVcfIndexFile(file)));

        progressHandler.end(taskName);

        taskName = "1.5 Computing scores";
        progressHandler.start(taskName);

        ScoreComputer scoreComputer = new ScoreComputer(vcfFiles, variantDetailsProvider);
        scoreComputer.computeRiskScores(riskScore);

        progressHandler.end(taskName);

        taskName = "1.7 Exporting results";
        progressHandler.start(taskName);

        exportResults(destinationFile, scoreComputer.sampleNames, scoreComputer.scores);

        progressHandler.end(taskName);

        progressHandler.end(mainTaskName);

    }

    /**
     * Exports the score results to the file.
     *
     * @param destinationFile the destination file
     * @param sampleNames the name of the samples
     * @param scores the scores array
     */
    private static void exportResults(File destinationFile, ArrayList<String> sampleNames, double[] scores) {

        try (SimpleFileWriter writer = new SimpleFileWriter(destinationFile, true)) {

            writer.writeLine("Sample", "Score");

            IntStream.range(0, sampleNames.size())
                    .forEach(i -> writer.writeLine(sampleNames.get(i), Double.toString(scores[i])));

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
            lPrintWriter.print("  Score Computation Command Line  " + lineSeparator);
            lPrintWriter.print("==================================" + lineSeparator);
            lPrintWriter.print(lineSeparator
                    + "The ComputeScore command line computes risk scores from vcf files." + lineSeparator
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
