package net.unit8.amagicman;

import java.io.*;

/**
 * Abstraction for resolving the source location of templates and the
 * destination location of generated files.
 *
 * <p>Tasks read templates and write output exclusively through this interface,
 * enabling test implementations to inject mocks without touching the real
 * filesystem.</p>
 *
 * @author kawasima
 * @see PathResolverImpl
 */
public interface PathResolver {

    /**
     * Returns the root directory of the project.
     *
     * <p>Typically used to locate {@code pom.xml} or other files at the project root.</p>
     *
     * @return the project root {@link File}
     */
    File project();

    /**
     * Returns an {@link InputStream} for the template resource at the given path.
     *
     * <p>Templates are managed as classpath resources.</p>
     *
     * @param path relative path of the template resource
     * @return input stream for the template, or {@code null} if the resource does not exist
     */
    InputStream templateAsStream(String path);

    /**
     * Returns an {@link OutputStream} for writing to the destination at the given path.
     *
     * <p>The default implementation creates a {@link FileOutputStream} from
     * {@link #destinationAsFile(String)}.</p>
     *
     * @param path relative path of the destination file
     * @return output stream for the destination
     * @throws IOException if the destination file cannot be opened
     */
    default OutputStream destinationAsStream(String path) throws IOException {
        return new FileOutputStream(destinationAsFile(path));
    }

    /**
     * Returns the destination {@link File} for the given relative path.
     *
     * <p>Implementations are expected to create any missing parent directories.</p>
     *
     * @param path relative path of the destination file
     * @return the destination {@link File}
     * @throws IOException if directory creation or other I/O operations fail
     */
    File destinationAsFile(String path) throws IOException;
}
