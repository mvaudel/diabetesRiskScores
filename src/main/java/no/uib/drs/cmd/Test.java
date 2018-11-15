package no.uib.drs.cmd;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import java.io.File;
import java.util.Map;

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

                while (iterator.hasNext()) {

                    VariantContext variantContext = iterator.next();
                    Map<String, Object> attribute = variantContext.getGenotype(0).getExtendedAttributes();
                    
                    if (attribute.size() > 2) {
                        
                    System.out.println("Attributes Umich");
                    System.out.println(attribute);
                    break;

                    }
                }
            }
        }
        
        cpt = 0;

        try (VCFFileReader reader = new VCFFileReader(new File("/mnt/archive/mody/gw/vcf/tmp/22-dbsnp-unique-common.vcf.gz"))) {

            try (CloseableIterator<VariantContext> iterator = reader.iterator()) {

                while (iterator.hasNext() && cpt++ < 1) {

                    VariantContext variantContext = iterator.next();
//                    System.out.println("Attributes Sanger");
//                    System.out.println(variantContext.getCommonInfo().getAttributes());

                }
            }
        }
    }
}
