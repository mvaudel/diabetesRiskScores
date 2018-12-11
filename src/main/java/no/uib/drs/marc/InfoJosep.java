package no.uib.drs.marc;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import no.uib.drs.io.Utils;
import no.uib.drs.io.flat.SimpleFileReader;
import no.uib.drs.io.flat.SimpleGzWriter;
import no.uib.drs.io.vcf.VariantDetailsProvider;

/**
 * Formats the file from Josep.
 *
 * @author Marc Vaudel
 */
public class InfoJosep {

    /**
     * Main method.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        HashMap<String, Boolean> typedMap = new HashMap<>();
        HashMap<String, Double> scoreMap = new HashMap<>();

        SimpleFileReader infoReader = Utils.getFileReader(new File("/mnt/archive/marc/partners/grs/mgh/updated_with_21Kdataset/plink/infos.txt"));

        String line = infoReader.readLine();
        while ((line = infoReader.readLine()) != null) {

            String[] lineSplit = line.split("\t");

            String id = String.join("_", lineSplit[0], lineSplit[1], lineSplit[2]);

            boolean typed = lineSplit[7].equals("Genotyped");

            double score = Double.parseDouble(lineSplit[6]);

            typedMap.put(id, typed);
            scoreMap.put(id, score);

        }

        File vcfFile = new File("/mnt/archive/marc/partners/grs/mgh/updated_with_21Kdataset/diabetesGrs.vcf.gz");
        File infoFile = new File("/mnt/archive/marc/partners/grs/mgh/updated_with_21Kdataset/diabetesGrs.info.gz");

        try (SimpleGzWriter writer = new SimpleGzWriter(infoFile)) {

            writer.writeLine("# Vcf: " + vcfFile.getName());
            writer.writeLine("# Version: " + VariantDetailsProvider.version);

            writer.writeLine("CHR", "BP", "ID", "REF", "ALT", "MAF", "TYPED", "SCORE");

            try (VCFFileReader reader = new VCFFileReader(vcfFile)) {

                try (CloseableIterator<VariantContext> iterator = reader.iterator()) {

                    while (iterator.hasNext()) {

                        VariantContext variantContext = iterator.next();
                        String variantId = variantContext.getID();

                        String contig = variantContext.getContig();
                        int start = variantContext.getStart();
                        String ref = variantContext.getReference().getBaseString();

                        List<Allele> altAlleles = variantContext.getAlternateAlleles();

                        if (altAlleles.size() > 1) {

                            throw new IllegalArgumentException("Multiple alleles found for variant " + variantId + ".");

                        }

                        String alt = variantContext.getAlternateAlleles().get(0).getBaseString();

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

                            String positionalId = getPositionalId(contig, start, ref, alt);

                            boolean typed = typedMap.get(positionalId);
                            double score = scoreMap.get(positionalId);

                            writer.writeLine(
                                    contig,
                                    Integer.toString(start),
                                    variantId,
                                    ref,
                                    alt,
                                    Double.toString(maf),
                                    typed ? "1" : "0",
                                    Double.toString(score)
                            );
                        }
                    }
                }
            }
        }
    }

    public static String getPositionalId(String chr, int bp, String ref, String alt) {

        return String.join("_",
                String.join(":",
                        chr,
                        Integer.toString(bp)),
                ref,
                alt);

    }

}
