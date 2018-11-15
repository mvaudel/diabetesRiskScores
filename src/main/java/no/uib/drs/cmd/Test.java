package no.uib.drs.cmd;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import java.io.File;

/**
 *
 * @author Marc Vaudel
 */
public class Test {

    /**
     * Main method.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        int cpt = 0;

        try (VCFFileReader reader = new VCFFileReader(new File("/mnt/archive/mody/gw/vcf/tmp/mody_22.vcf.gz"))) {

            try (CloseableIterator<VariantContext> iterator = reader.iterator()) {

                while (iterator.hasNext() && cpt++ < 1) {

                    VariantContext variantContext = iterator.next();
                    System.out.println("Attributes Umich");
                    System.out.println(variantContext.getCommonInfo().getAttributes());
                    System.out.println("logp Umich");
                    System.out.println(variantContext.getCommonInfo().getLog10PError());
                    System.out.println("filters Umich");
                    System.out.println(variantContext.getCommonInfo().getFilters());

                }
            }
        }
        
        cpt = 0;

        try (VCFFileReader reader = new VCFFileReader(new File("/mnt/archive/mody/gw/vcf/tmp/22-dbsnp-unique-common.vcf.gz"))) {

            try (CloseableIterator<VariantContext> iterator = reader.iterator()) {

                while (iterator.hasNext() && cpt++ < 1) {

                    VariantContext variantContext = iterator.next();
                    System.out.println("Attributes Sanger");
                    System.out.println(variantContext.getCommonInfo().getAttributes());
                    System.out.println("logp Sanger");
                    System.out.println(variantContext.getCommonInfo().getLog10PError());
                    System.out.println("filters Sanger");
                    System.out.println(variantContext.getCommonInfo().getFilters());

                }
            }
        }
    }
}
