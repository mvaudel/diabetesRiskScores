package no.uib.drs.io.vcf;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;
import no.uib.drs.model.biology.Variant;

/**
 * Genotype provider based on vcf files. One genotype provider should be used
 * per thread.
 *
 * @author Marc Vaudel
 */
public class GenotypeProvider {

    /**
     * Map of the vcf file readers indexed by file name.
     */
    private final HashMap<String, VCFFileReader> vcfFiles = new HashMap<>();
    /**
     * Cache for the samples list.
     */
    private List<String> samples = null;

    /**
     * Constructor.
     */
    public GenotypeProvider() {

    }

    /**
     * Returns the samples in the vcf files.
     *
     * @return the samples in the vcf files
     */
    public List<String> getSamples() {

        return samples;

    }

    /**
     * Adds a vcf file.
     *
     * @param vcfFile the vcf file
     * @param indexFile the index file
     */
    public void addVcfFile(File vcfFile, File indexFile) {

        String fileName = vcfFile.getName();
        VCFFileReader vcfFileReader = new VCFFileReader(vcfFile, indexFile);
        vcfFiles.put(fileName, vcfFileReader);
        
        if (samples == null) {

            samples = vcfFileReader.getFileHeader().getSampleNamesInOrder();
            
        } else {
            
            List<String> newSamples = vcfFileReader.getFileHeader().getSampleNamesInOrder();
            
            boolean sampleSamples = newSamples.size() == samples.size()
                    && IntStream.range(0, samples.size()).allMatch(i -> newSamples.get(i).equals(samples.get(i)));
            
            if (!sampleSamples) {
                
                throw new IllegalArgumentException("VCF files with different samples provided.");
                
            }
        }
    }

    /**
     * Returns the variant context of a given variant.
     *
     * @param vcfFile the name of the vcf file where to look for the variant
     * @param variant a variant
     *
     * @return the variant context of a given variant
     */
    public VariantContext getVariantContext(String vcfFile, Variant variant) {

        VCFFileReader vcfFileReader = vcfFiles.get(vcfFile);

        try (CloseableIterator<VariantContext> iterator = vcfFileReader.query(variant.chr, variant.bp, variant.bp)) {

            while (iterator.hasNext()) {

                VariantContext variantContext = iterator.next();

                if (variantContext.getID().equals(variant.id)) {

                    return variantContext;

                }
            }
        }

        return null;

    }

    /**
     * Returns the genotype of a given variant in a given sample.
     *
     * @param vcfFile the name of the vcf file where to look for the variant
     * @param variant a variant
     *
     * @return the genotype of a given variant in a given sample
     */
    public List<Allele> getAlleles(String vcfFile, Variant variant) {

        VariantContext variantContext = getVariantContext(vcfFile, variant);

        return variantContext == null ? null : variantContext.getAlleles();

    }
    
    /**
     * Closes the connection to the files.
     */
    public void close() {

        vcfFiles.values()
                .forEach(reader -> reader.close());

    }
}
