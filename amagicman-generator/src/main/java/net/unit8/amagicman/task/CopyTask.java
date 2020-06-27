package net.unit8.amagicman.task;

import net.unit8.amagicman.GenTask;
import net.unit8.amagicman.PathResolver;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * A task to copy file.
 *
 * @author kawasima
 */
public class CopyTask implements GenTask {
    private static final int BUF_SIZE = 4096;
    private final String source;
    private final String destination;

    public CopyTask(String source, String destination) {
        this.source = source;
        this.destination = destination;
    }

    @Override
    public void execute(PathResolver pathResolver) throws Exception {
        final byte[] buffer = new byte[BUF_SIZE];
        try(InputStream is = pathResolver.templateAsStream(source);
            OutputStream os = pathResolver.destinationAsStream(destination)) {
            int count = 0;
            int n;
            while (-1 != (n = is.read(buffer))) {
                os.write(buffer, 0, n);
                count += n;
            }
        }
    }

    public String getDestinationPath() {
        return destination;
    }
}
