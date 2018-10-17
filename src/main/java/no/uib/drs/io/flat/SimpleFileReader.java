package no.uib.drs.io.flat;

/**
 * Interface for file readers.
 *
 * @author Marc Vaudel
 */
public interface SimpleFileReader extends AutoCloseable {


    /**
     * Reads a line of the file.
     *
     * @return a line of the file
     */
    public String readLine();

    @Override
    public void close();
}
