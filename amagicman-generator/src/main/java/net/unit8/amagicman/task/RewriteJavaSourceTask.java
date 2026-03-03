package net.unit8.amagicman.task;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import net.unit8.amagicman.GenTask;
import net.unit8.amagicman.PathResolver;
import net.unit8.amagicman.util.JavaNodeUtils;

import java.io.*;

/**
 * A task that modifies an existing Java source file in-place by parsing it,
 * applying AST transformations, and overwriting the file with the result.
 *
 * <p>Unlike {@link JavaByTemplateTask}, this task reads from an existing destination
 * file rather than a classpath template. It is suited for incremental code changes
 * such as adding methods or annotations to already-generated sources.</p>
 *
 * <pre>{@code
 * new RewriteJavaSourceTask(
 *     "src/main/java/com/example/UserController.java",
 *     cu -> cu.getClassByName("UserController").ifPresent(cls ->
 *         cls.addAnnotation("Transactional"))
 * )
 * }</pre>
 *
 * @author kawasima
 */
public class RewriteJavaSourceTask implements GenTask {
    private final String destination;
    private final RewriteJavaSourceProcess process;

    /**
     * Constructs a {@code RewriteJavaSourceTask}.
     *
     * @param destination relative path of the Java file to read and overwrite
     * @param process     AST transformation applied to the parsed file
     */
    public RewriteJavaSourceTask(String destination, RewriteJavaSourceProcess process) {
        this.destination = destination;
        this.process = process;
    }

    /**
     * Parses the existing file, applies the transformation, and overwrites the file.
     *
     * @param pathResolver resolver for locating the destination file
     * @throws Exception if parsing or I/O fails
     */
    @Override
    public void execute(PathResolver pathResolver) throws Exception {
        CompilationUnit cu;
        try (InputStream is = new FileInputStream(pathResolver.destinationAsFile(destination))) {
            cu = StaticJavaParser.parse(is);
        }

        process.process(cu);

        try (Writer wtr = new OutputStreamWriter(pathResolver.destinationAsStream(destination))) {
            wtr.write(JavaNodeUtils.toString(cu));
        }
    }

    /**
     * Functional interface for transforming a parsed Java {@link CompilationUnit}.
     */
    @FunctionalInterface
    public interface RewriteJavaSourceProcess {

        /**
         * Applies in-place modifications to the given compilation unit.
         *
         * @param cu the parsed Java AST to transform
         */
        void process(CompilationUnit cu);
    }

    /**
     * {@inheritDoc}
     */
    public String getDestinationPath() {
        return destination;
    }
}
