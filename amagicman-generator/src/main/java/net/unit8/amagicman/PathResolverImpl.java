package net.unit8.amagicman;

import java.io.*;
import java.nio.file.Files;
import java.util.Optional;

/**
 * Default implementation of {@link PathResolver}.
 *
 * <p>Templates are loaded from classpath resources using the context class loader,
 * optionally prefixed with a configurable path prefix.
 * Generated files are written to a configurable destination directory on the filesystem.
 * Missing parent directories are created automatically.</p>
 *
 * @author kawasima
 */
public class PathResolverImpl implements PathResolver {
    private File projectDir;
    private final String templatePrefix;
    private File destinationDir;

    /**
     * Constructs a new {@code PathResolverImpl}.
     *
     * @param projectPath     path to the project root directory;
     *                        {@code null} falls back to the current directory ({@code "."})
     * @param templatePrefix  classpath prefix prepended to every template path,
     *                        e.g. {@code "META-INF/amagicman/templates"};
     *                        may be {@code null} or empty
     * @param destinationPath root directory for generated output files;
     *                        {@code null} falls back to the current directory
     */
    public PathResolverImpl(String projectPath, String templatePrefix, String destinationPath) {
        if (projectPath != null) {
            this.projectDir = new File(projectPath);
        }

        this.templatePrefix = templatePrefix;

        if (destinationPath != null) {
            this.destinationDir = new File(destinationPath);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns the current directory if no {@code projectPath} was provided at construction time.</p>
     */
    @Override
    public File project() {
        return Optional.ofNullable(projectDir).orElse(new File("."));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Prepends the configured {@code templatePrefix} to {@code path} and loads the resource
     * via the context class loader.</p>
     */
    @Override
    public InputStream templateAsStream(String path) {
        String prefix = Optional.ofNullable(templatePrefix).orElse("");
        prefix = prefix.replaceAll("^/", "").replaceAll("([^/])$", "$1/");
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(prefix + path);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Creates all missing parent directories using {@link Files#createDirectories} before
     * returning the destination file.</p>
     */
    @Override
    public File destinationAsFile(String path) throws IOException {
        File dest = new File(Optional.ofNullable(destinationDir)
                .orElse(new File(".")), path);

        Files.createDirectories(dest.toPath().getParent());
        return dest;
    }

}
