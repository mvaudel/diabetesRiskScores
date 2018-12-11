package no.uib.drs.marc;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

        try (VCFFileReader reader = new VCFFileReader(new File("C:\\data\\partnersGrs.vcf.gz"))) {
        
        ArrayList<String> samples = reader.getFileHeader().getSampleNamesInOrder();

            try (CloseableIterator<VariantContext> iterator = reader.iterator()) {

                while (iterator.hasNext()) {

                    VariantContext variantContext = iterator.next();
                    
                    List<Allele> alleles = variantContext.getAlleles();
                        
                    System.out.println("Info " + variantContext.getID());
                    System.out.println(alleles.size());
                }
            }
        }
    }
}
