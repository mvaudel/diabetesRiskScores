package no.uib.drs.cmd;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import no.uib.drs.DiabetesRiskScore;
import no.uib.drs.io.Utils;
import static no.uib.drs.io.Utils.lineSeparator;
import no.uib.drs.io.flat.SimpleFileReader;
import no.uib.drs.io.json.SimpleObjectMapper;
import no.uib.drs.model.ScoringFeature;
import no.uib.drs.model.features.HaplotypeFeature;
import no.uib.drs.model.features.AdditiveFeature;
import no.uib.drs.model.score.RiskScore;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

/**
 * This command builds a score definition as json file from a snp summary file.
 *
 * @author Marc Vaudel
 */
public class BuildScore {

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

            BuildScoreOptionsBean bean = new BuildScoreOptionsBean(commandLine);
            
            RiskScore riskScore = getRiskScore(bean.inputFile, bean.name, bean.version);
            
            SimpleObjectMapper.write(bean.destinationFile, riskScore);
    
        } catch (Throwable e) {

            e.printStackTrace();
        }
    }
    
    /**
     * Imports the risk score from a text file.
     * 
     * @param variantsTable the score definition as text file
     * @param scoreName the score name
     * @param scoreVersion the score version
     * 
     * @return the risk score definition
     */
    private static RiskScore getRiskScore(File variantsTable, String scoreName, String scoreVersion) {
        
        ArrayList<ScoringFeature> scoringFeatures = new ArrayList<>();
        
        try (SimpleFileReader reader = Utils.getFileReader(variantsTable)) {

            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
            
            String[] lineSplit = line.split(Utils.separator);

                String rsId = lineSplit[0].trim();

                String name = lineSplit[1].trim();

                String effectAllele = lineSplit[2].trim();

                String weightString = lineSplit[4].trim();
                double weight = Double.parseDouble(weightString);

                String[] rsIdSplit = rsId.split(",");
                List<String>[] alleleSplit = Arrays.stream(effectAllele.split(","))
                        .map(snpAlleles -> Arrays.asList(snpAlleles.split("|")))
                        .toArray(List[]::new);

                if (rsIdSplit.length == 1) {

                    scoringFeatures.add(new AdditiveFeature(rsId, effectAllele, weight));

                } else if (rsIdSplit.length == 2) {

                    scoringFeatures.add(new HaplotypeFeature(name, rsIdSplit, alleleSplit, weight));

                }
            }
        }
        
        ScoringFeature[] featuresArray = scoringFeatures.stream().toArray(ScoringFeature[]::new);
        
        return new RiskScore(scoreName, scoreVersion, featuresArray);
        
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
            lPrintWriter.print("     Build Score Command Line     " + lineSeparator);
            lPrintWriter.print("==================================" + lineSeparator);
            lPrintWriter.print(lineSeparator
                    + "The BuildScore command line builds a score definition file in the json format." + lineSeparator
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
