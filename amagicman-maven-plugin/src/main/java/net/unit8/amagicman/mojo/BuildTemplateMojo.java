package net.unit8.amagicman.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Build an amagicman template.
 *
 * @author kawasima
 */
@Mojo(name = "buildTemplate", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class BuildTemplateMojo extends AbstractMojo {
    /**
     * A file name for a distribution.
     */
    @Parameter(name = "distribution", required = true)
    protected File distribution;

    /**
     * The prefix for a template path.
     */
    @Parameter(name ="prefix", defaultValue = "META-INF/amagicman/templates")
    protected String prefix;

    /**
     * The name.
     */
    @Parameter(name = "name", required = true)
    protected String name;

    /**
     * The output directory.
     */
    @Parameter(name = "outputDirectory", defaultValue = "${project.build.directory}", required = true)
    protected File outputDirectory;

    private String addPrefix(String name) {
        if (prefix != null) {
            name = prefix + (prefix.endsWith("/") ? "" : "/") + name;
        }

        return name;
    }

    private void add(File source, JarOutputStream target) throws IOException {

        if (source.isDirectory()) {
            String name = addPrefix(source.getPath().replace("\\", "/"));
            if (!name.isEmpty()) {
                if (!name.endsWith("/"))
                    name += "/";
                JarEntry entry = new JarEntry(name);
                entry.setTime(source.lastModified());
                target.putNextEntry(entry);
                target.closeEntry();
            }
            for (File nestedFile: source.listFiles())
                add(nestedFile, target);
            return;
        }

        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(source))) {
            JarEntry entry = new JarEntry(addPrefix(source.getPath().replace("\\", "/")));
            entry.setTime(source.lastModified());
            target.putNextEntry(entry);

            byte[] buffer = new byte[4096];
            while (true) {
                int count = in.read(buffer);
                if (count == -1)
                    break;
                target.write(buffer, 0, count);
            }
            target.closeEntry();
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        File outputJar = new File(outputDirectory, name + ".jar");
        try (JarOutputStream out = new JarOutputStream(
                new FileOutputStream(outputJar), manifest)) {
            Files.readAllLines(distribution.toPath(), StandardCharsets.UTF_8).stream()
                    .map(line -> line.trim())
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .forEach(line -> {
                        try { add(new File(line), out); }
                        catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
            getLog().info("Built a template to " + outputJar);
        } catch (IOException e) {
            throw new MojoExecutionException("", e);
        }
    }
}
