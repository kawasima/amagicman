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
 * @author kawasima
 */
public class JavaByTemplateTask  implements GenTask {
    private final String source;
    private final String destination;
    private final JavaTemplateProcess process;

    public JavaByTemplateTask(String source, String destination, JavaTemplateProcess process) {
        this.source = source;
        this.destination = destination;
        this.process = process;
    }

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

    public interface JavaTemplateProcess {
        void process(CompilationUnit cu);
    }

    public String getDestinationPath() {
        return destination;
    }

}
