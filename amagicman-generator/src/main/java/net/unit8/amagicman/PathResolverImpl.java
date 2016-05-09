package net.unit8.amagicman;

import java.io.*;
import java.nio.file.Files;
import java.util.Optional;

/**
 * @author kawasima
 */
public class PathResolverImpl implements PathResolver {
    private File projectDir;
    private String templatePrefix;
    private File destinationDir;

    public PathResolverImpl() {
    }

    public PathResolverImpl(String projectPath, String templatePrefix, String destinationPath) {
        if (projectDir != null) {
            this.projectDir = new File(projectPath);
        }

        this.templatePrefix = templatePrefix;

        if (destinationPath != null) {
            this.destinationDir = new File(destinationPath);
        }
    }

    @Override
    public File project() {
        return Optional.ofNullable(projectDir).orElse(new File("."));
    }

    @Override
    public InputStream templateAsStream(String path) {
        String prefix = Optional.ofNullable(templatePrefix).orElse("");
        prefix = prefix.replaceAll("^/", "").replaceAll("([^/])$", "$1/");
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(prefix + path);
    }

    @Override
    public File destinationAsFile(String path) throws IOException {
        File dest = new File(Optional.ofNullable(destinationDir)
                .orElse(new File(".")), path);

        Files.createDirectories(dest.toPath().getParent());
        return dest;
    }

}
