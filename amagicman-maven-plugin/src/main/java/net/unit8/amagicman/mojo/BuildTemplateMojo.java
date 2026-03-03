package net.unit8.amagicman.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Maven Mojo that packages a set of template files into an amagicman template JAR.
 *
 * <p>A distribution list file specifies which files and directories to include.
 * Each non-empty, non-comment line is treated as a file or directory path.
 * All entries are added under the configured prefix inside the JAR so that they
 * can be resolved as classpath resources at runtime.</p>
 *
 * <p>Typical usage in a project's {@code pom.xml}:</p>
 * <pre>{@code
 * <plugin>
 *   <groupId>net.unit8.amagicman</groupId>
 *   <artifactId>amagicman-maven-plugin</artifactId>
 *   <configuration>
 *     <distribution>src/main/templates/distribution.txt</distribution>
 *     <name>my-template</name>
 *   </configuration>
 * </plugin>
 * }</pre>
 *
 * @author kawasima
 */
@Mojo(name = "buildTemplate", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true, requiresDependencyResolution = ResolutionScope.RUNTIME)
@Execute(lifecycle = "amagicman", phase = LifecyclePhase.PACKAGE, goal = "buildTemplate")
public class BuildTemplateMojo extends AbstractMojo {

    /**
     * Path to the distribution list file.
     * Each non-empty, non-comment line names a file or directory to include in the JAR.
     * Lines starting with {@code #} are treated as comments and ignored.
     */
    @Parameter(name = "distribution", required = true)
    protected File distribution;

    /**
     * Classpath prefix prepended to every entry path inside the JAR.
     * Defaults to {@code META-INF/amagicman/templates}.
     */
    @Parameter(name = "prefix", defaultValue = "META-INF/amagicman/templates")
    protected String prefix;

    /**
     * Base name of the output JAR file (without the {@code .jar} extension).
     */
    @Parameter(name = "name", required = true)
    protected String name;

    /**
     * Directory where the template JAR is written.
     * Defaults to {@code ${project.build.directory}}.
     */
    @Parameter(name = "outputDirectory", defaultValue = "${project.build.directory}", required = true)
    protected File outputDirectory;

    /**
     * Prepends the configured {@link #prefix} to the given entry name.
     *
     * @param name raw entry path (may use {@code /} or {@code \\} as separator)
     * @return entry path with the prefix applied
     */
    private String addPrefix(String name) {
        if (prefix != null) {
            name = prefix + (prefix.endsWith("/") ? "" : "/") + name;
        }
        return name;
    }

    /**
     * Recursively adds a file or directory to the JAR output stream.
     *
     * <p>For directories, a directory entry is written and then each child is added
     * recursively. For files, the content is streamed in 4 KiB chunks.</p>
     *
     * @param source the file or directory to add
     * @param target the open {@link JarOutputStream} to write to
     * @throws IOException if reading {@code source} or writing to {@code target} fails
     */
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

            for (File nestedFile : Optional.ofNullable(source.listFiles()).orElse(new File[0])) {
                add(nestedFile, target);
            }
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

    /**
     * Reads the distribution list and packages the listed files into a template JAR.
     *
     * @throws MojoExecutionException if the distribution list cannot be read or JAR creation fails
     */
    @Override
    public void execute() throws MojoExecutionException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        File outputJar = new File(outputDirectory, name + ".jar");
        try (JarOutputStream out = new JarOutputStream(
                new FileOutputStream(outputJar), manifest)) {
            for (String line : Files.readAllLines(distribution.toPath(), StandardCharsets.UTF_8)) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    add(new File(line), out);
                }
            }
            getLog().info("Built a template to " + outputJar);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to build template JAR: " + outputJar, e);
        }
    }
}
