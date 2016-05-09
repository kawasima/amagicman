package net.unit8.amagicman;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author kawasima
 */
public class PathResolverMock implements PathResolver {
    ByteArrayOutputStream os = new ByteArrayOutputStream();

    @Override
    public File project() {
        return null;
    }

    @Override
    public InputStream templateAsStream(String path) {
        return null;
    }

    @Override
    public OutputStream destinationAsStream(String path) throws IOException {
        return os;
    }

    @Override
    public File destinationAsFile(String path) throws IOException {
        return null;
    }

    public String getWrittenString() {
        return new String(os.toByteArray(), StandardCharsets.UTF_8);
    }
}
