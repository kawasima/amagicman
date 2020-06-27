package net.unit8.amagicman;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author kawasima
 */
public class PathResolverMock implements PathResolver {
    final ByteArrayOutputStream os = new ByteArrayOutputStream();

    @Override
    public File project() {
        return null;
    }

    @Override
    public InputStream templateAsStream(String path) {
        return Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(path);
    }

    @Override
    public OutputStream destinationAsStream(String path) {
        return os;
    }

    @Override
    public File destinationAsFile(String path) {
        return new File(path);
    }

    public String getWrittenString() {
        return new String(os.toByteArray(), StandardCharsets.UTF_8);
    }
}
