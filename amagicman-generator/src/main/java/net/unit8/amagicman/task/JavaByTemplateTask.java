package net.unit8.amagicman.task;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import net.unit8.amagicman.GenTask;
import net.unit8.amagicman.PathResolver;
import net.unit8.amagicman.util.JavaNodeUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * A task that generates a new Java source file by parsing a template Java file
 * and applying AST transformations via a {@link JavaTemplateProcess}.
 *
 * <p>The template is parsed into a {@link CompilationUnit} using JavaParser, then
 * passed to the provided {@code process} for modification (e.g. renaming classes,
 * changing package declarations, adding annotations). The transformed AST is then
 * pretty-printed and written to the destination file.</p>
 *
 * <pre>{@code
 * new JavaByTemplateTask(
 *     "template/SampleController.java",
 *     "src/main/java/com/example/UserController.java",
 *     cu -> new ClassReplaceVisitor("com.example.sample", "com.example", "Sample", "User").visit(cu, null)
 * )
 * }</pre>
 *
 * @author kawasima
 */
public class JavaByTemplateTask implements GenTask {
    private final String source;
    private final String destination;
    private final JavaTemplateProcess process;

    /**
     * Constructs a {@code JavaByTemplateTask}.
     *
     * @param source      relative path of the template Java resource on the classpath
     * @param destination relative path of the output Java file
     * @param process     AST transformation applied to the parsed template
     */
    public JavaByTemplateTask(String source, String destination, JavaTemplateProcess process) {
        this.source = source;
        this.destination = destination;
        this.process = process;
    }

    /**
     * Parses the template, applies the transformation, and writes the result.
     *
     * @param pathResolver resolver for template input and destination output
     * @throws FileNotFoundException if the template resource is not found on the classpath
     * @throws Exception             if parsing or I/O fails
     */
    @Override
    public void execute(PathResolver pathResolver) throws Exception {
        CompilationUnit cu;
        try (InputStream is = pathResolver.templateAsStream(source)) {
            if (is == null) throw new FileNotFoundException(source);
            cu = StaticJavaParser.parse(is);
        }

        process.process(cu);

        try (Writer wtr = new OutputStreamWriter(pathResolver.destinationAsStream(destination))) {
            wtr.write(JavaNodeUtils.toString(cu));
        }
    }

    /**
     * Callback interface for transforming a parsed Java {@link CompilationUnit}.
     */
    public interface JavaTemplateProcess {

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
