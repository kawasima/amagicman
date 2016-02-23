package net.unit8.amagicman.task;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import net.unit8.amagicman.MoldTask;
import net.unit8.amagicman.PathResolver;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * @author kawasima
 */
public class JavaByTemplateTask  implements MoldTask {
    private String source;
    private String destination;
    private JavaTemplateProcess process;

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
            cu = JavaParser.parse(is);
        }

        process.process(cu);

        try (Writer wtr = new OutputStreamWriter(pathResolver.destinationAsStream(destination))) {
            wtr.write(cu.toString());
        }
    }

    public interface JavaTemplateProcess {
        void process(CompilationUnit cu);
    }

    public String getDestinationPath() {
        return destination;
    }

}
