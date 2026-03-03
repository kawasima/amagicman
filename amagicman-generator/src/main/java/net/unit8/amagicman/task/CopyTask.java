package net.unit8.amagicman.task;

import net.unit8.amagicman.GenTask;
import net.unit8.amagicman.PathResolver;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * A task that copies a template file to a destination path as-is (binary copy).
 *
 * <p>Reads the template resource as an {@link InputStream} and writes it to the
 * destination {@link OutputStream} in 4 KiB chunks. Suitable for any file type
 * including images and compiled resources.</p>
 *
 * @author kawasima
 */
public class CopyTask implements GenTask {
    private static final int BUF_SIZE = 4096;
    private final String source;
    private final String destination;

    /**
     * Constructs a {@code CopyTask}.
     *
     * @param source      relative path of the source template resource
     * @param destination relative path of the output file
     */
    public CopyTask(String source, String destination) {
        this.source = source;
        this.destination = destination;
    }

    /**
     * Copies the template resource to the destination.
     *
     * @param pathResolver resolver for template input and destination output
     * @throws Exception if an I/O error occurs during copying
     */
    @Override
    public void execute(PathResolver pathResolver) throws Exception {
        final byte[] buffer = new byte[BUF_SIZE];
        try (InputStream is = pathResolver.templateAsStream(source);
             OutputStream os = pathResolver.destinationAsStream(destination)) {
            int n;
            while (-1 != (n = is.read(buffer))) {
                os.write(buffer, 0, n);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getDestinationPath() {
        return destination;
    }
}
