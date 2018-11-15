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
public class ComputeScoreOptionsBean {

    /**
     * The file containing the score details.
     */
    public final File scoreDetailsFile;
    /**
     * The proxies mapping file. Null if none provided.
     */
    public final File proxiesMapFile;
    /**
     * the vcf files.
     */
    public final File[] vcfFiles;
    /**
     * The variant details files.
     */
    public final File[] variantDetailsFiles;
    /**
     * The file where to write the scores.
     */
    public final File destinationFile;

    /**
     * Constructor. Parses the command line options and conducts minimal sanity check.
     * 
     * @param aLine a command line
     */
    public ComputeScoreOptionsBean(CommandLine aLine) {
        
        // Check that mandatory options are provided

        for (ComputeScoreOptions option : ComputeScoreOptions.values()) {

            if (option.mandatory && !aLine.hasOption(option.opt)) {

                throw new IllegalArgumentException("No value found for mandatory option " + option.opt + " (" + option.longOpt + ")");

            }
        }

        
        // Score definition
        
        String filePath = aLine.getOptionValue(ComputeScoreOptions.score.opt);

        scoreDetailsFile = new File(filePath);

        if (!scoreDetailsFile.exists()) {

            throw new IllegalArgumentException("Score definition file (" + filePath + ") not found.");

        }
        
        
        // VCF files or folder

        filePath = aLine.getOptionValue(ComputeScoreOptions.vcf.opt);

        vcfFiles = Arrays.stream(filePath.split(","))
                .map(path -> new File(path))
                .flatMap(file -> file.isDirectory() ? Arrays.stream(file.listFiles()) : Stream.of(file))
                .filter(file -> file.getName().toLowerCase().endsWith(".vcf") || file.getName().toLowerCase().endsWith(".vcf.gz"))
                .toArray(File[]::new);
        
        if (vcfFiles.length == 0) {
            
                throw new IllegalArgumentException("No vcf found at (" + filePath + ").");
            
        }
        
        Arrays.stream(vcfFiles)
                .filter(file -> !file.exists())
                .forEach(file -> {
                    throw new IllegalArgumentException("Vcf file (" + file.getAbsolutePath() + ") not found.");
                            });
        
        
        // Variant details files

        variantDetailsFiles = Arrays.stream(filePath.split(","))
                .map(path -> new File(path))
                .toArray(File[]::new);
        
        if (vcfFiles.length == 0) {
            
                throw new IllegalArgumentException("No variant details file found at (" + filePath + ") not found.");
            
        }
        
        Arrays.stream(vcfFiles)
                .filter(file -> !file.exists())
                .forEach(file -> {
                    throw new IllegalArgumentException("Variants details file (" + file.getAbsolutePath() + ") not found.");
                            });

        
        // Output
        
        filePath = aLine.getOptionValue(ComputeScoreOptions.out.opt);

        destinationFile = new File(filePath);

        if (!destinationFile.getParentFile().exists()) {

            throw new IllegalArgumentException("Output folder (" + destinationFile.getParent() + ") not found.");

        }
        
        
        // Proxies file

        if (aLine.hasOption(ComputeScoreOptions.proxies.opt)) {

            filePath = aLine.getOptionValue(ComputeScoreOptions.proxies.opt);

            proxiesMapFile = new File(filePath);

            if (!proxiesMapFile.exists()) {

                throw new IllegalArgumentException("Proxies file (" + filePath + ") not found.");

            }
        } else {
            proxiesMapFile = null;
        }
    }
}
