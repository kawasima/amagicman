package net.unit8.amagicman;

import java.io.*;
import java.nio.file.Files;
import java.util.Optional;

/**
 * @author kawasima
 */
public interface PathResolver {
    File project();

    InputStream templateAsStream(String path);

    OutputStream destinationAsStream(String path) throws IOException;
}
