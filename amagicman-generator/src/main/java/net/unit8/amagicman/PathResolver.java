package net.unit8.amagicman;

import java.io.*;

/**
 * @author kawasima
 */
public interface PathResolver {
    File project();

    InputStream templateAsStream(String path);

    default OutputStream destinationAsStream(String path) throws IOException {
        return new FileOutputStream(destinationAsFile(path));
    }

    File destinationAsFile(String path) throws IOException;
}
