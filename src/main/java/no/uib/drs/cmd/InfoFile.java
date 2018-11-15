package no.uib.drs.cmd;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import no.uib.drs.DiabetesRiskScore;
import static no.uib.drs.io.Utils.getFileReader;
import static no.uib.drs.io.Utils.lineSeparator;
import no.uib.drs.io.flat.SimpleFileReader;
import no.uib.drs.io.flat.SimpleGzWriter;
import no.uib.drs.io.vcf.VcfSettings;
import no.uib.drs.utils.ProgressHandler;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

/**
 * This command line creates a file containing information on the variants.
 *
 * @author Marc Vaudel
 */
public class InfoFile {

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
            InfoFileOptions.createOptionsCLI(lOptions);
            CommandLineParser parser = new DefaultParser();
            CommandLine commandLine = parser.parse(lOptions, args);

            InfoFileOptionsBean bean = new InfoFileOptionsBean(commandLine);

            writeInfoFile(bean.vcfFile, bean.destinationFile, bean.snpFile, bean.vcfSettings);

        } catch (Throwable e) {

            e.printStackTrace();
        }

    }

    /**
     * Writes a file containin information on variants.
     *
     * @param vcfFile the vcf file
     * @param destinationFile the output file
     * @param snpFile the file containing the ids of the variants to extract
     * @param vcfSettings the vcf parsing settings
     */
    private static void writeInfoFile(File vcfFile, File destinationFile, File snpFile, VcfSettings vcfSettings) {

        ProgressHandler progressHandler = new ProgressHandler();

        String mainTaskName = "1. Extracting variant information";
        progressHandler.start(mainTaskName);

        String taskName = "1.1 Loading variants";
        progressHandler.start(taskName);

        HashSet<String> variants = snpFile == null ? null : loadVariants(snpFile);

        progressHandler.end(taskName);

        taskName = "1.2 Extracting variant details";
        progressHandler.start(taskName);

        extractDetails(vcfFile, destinationFile, variants, vcfSettings);

        progressHandler.end(taskName);

        progressHandler.end(mainTaskName);
    }

    /**
     * Loads the ids of variants to extract from a file. One id per line.
     *
     * @param snpFile the file containing the variants
     *
     * @return the ids as set
     */
    private static HashSet<String> loadVariants(File snpFile) {

        HashSet<String> variants = new HashSet<>();

        try (SimpleFileReader reader = getFileReader(snpFile)) {

            String line;
            while ((line = reader.readLine()) != null) {

                if (!(line = line.trim()).equals("")) {

                    variants.add(line);

                }
            }
        }

        return variants;
    }

    /**
     * Extracts the snp details from the vcf file. CHR, BP, ID, REF, ALT, MAF,
     * are extracted. One line per variant. Multi-allelic and monomorphic
     * variants are excluded.
     *
     * @param vcfFile the vcf file
     * @param destinationFile the destination file
     * @param variants the ids of variants to select, ignored if null
     * @param vcfSettings the vcf parsing settings
     */
    private static void extractDetails(File vcfFile, File destinationFile, HashSet<String> variants, VcfSettings vcfSettings) {

        try (VCFFileReader vcfFileReader = new VCFFileReader(vcfFile)) {

            try (SimpleGzWriter writer = new SimpleGzWriter(destinationFile)) {

                writer.writeLine("CHR", "BP", "ID", "REF", "ALT", "MAF");

                try (CloseableIterator<VariantContext> iterator = vcfFileReader.iterator()) {

                    while (iterator.hasNext()) {

                        VariantContext variantContext = iterator.next();
                        String variantId = variantContext.getID();

                        if (variants == null || variants.contains(variantId)) {

                            String contig = variantContext.getContig();
                            int start = variantContext.getStart();
                            String ref = variantContext.getReference().getBaseString();
                            
                            boolean typed = vcfSettings.typedFilter && variantContext.getFilters().contains(vcfSettings.typedFlag)
                                    || !vcfSettings.typedFilter ;

                            List<Allele> altAlleles = variantContext.getAlternateAlleles();

                            if (altAlleles.size() == 1) {

                                String alt = altAlleles.get(0).getBaseString();

                                if (!alt.equals(ref)) {

                                    double nAlt = (double) variantContext.getGenotypes().stream()
                                            .parallel()
                                            .flatMap(genotype -> genotype.getAlleles().stream())
                                            .filter(allele -> allele.getBaseString().equals(alt))
                                            .count();
                                    double nAll = (double) variantContext.getGenotypes().stream()
                                            .parallel()
                                            .flatMap(genotype -> genotype.getAlleles().stream())
                                            .count();

                                    double maf = nAlt / nAll;

                                    writer.writeLine(
                                            contig,
                                            Integer.toString(start),
                                            variantId,
                                            ref,
                                            alt,
                                            Double.toString(maf)
                                    );

                                }
                            }
                        }
                    }
                }
            }
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
            lPrintWriter.print("  Variant Information Extraction  " + lineSeparator);
            lPrintWriter.print("==================================" + lineSeparator);
            lPrintWriter.print(lineSeparator
                    + "The InfoFile command line extracts information on the variants of a vcf file." + lineSeparator
                    + lineSeparator
                    + "For documentation and bug report see https://github.com/mvaudel/diabetesRiskScores." + lineSeparator
                    + lineSeparator
                    + "----------------------"
                    + lineSeparator
                    + "OPTIONS"
                    + lineSeparator
                    + "----------------------" + lineSeparator
                    + lineSeparator);
            lPrintWriter.print(InfoFileOptions.getOptionsAsString());
            lPrintWriter.flush();
        }
    }

}
