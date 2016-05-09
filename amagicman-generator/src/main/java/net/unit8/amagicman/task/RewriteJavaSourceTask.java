package net.unit8.amagicman.task;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import net.unit8.amagicman.GenTask;
import net.unit8.amagicman.PathResolver;

import java.io.*;

/**
 * @author kawasima
 */
public class RewriteJavaSourceTask implements GenTask {
    private String destination;
    private RewriteJavaSourceProcess process;

    public RewriteJavaSourceTask(String destination, RewriteJavaSourceProcess process) {
        this.destination = destination;
        this.process = process;
    }

    @Override
    public void execute(PathResolver pathResolver) throws Exception {
        CompilationUnit cu;
        try (InputStream is = new FileInputStream(pathResolver.destinationAsFile(destination))) {
            cu = JavaParser.parse(is);
        }

        process.process(cu);

        try (Writer wtr = new OutputStreamWriter(pathResolver.destinationAsStream(destination))) {
            wtr.write(cu.toString());
        }
    }

    public interface RewriteJavaSourceProcess {
        void process(CompilationUnit cu);
    }

    public String getDestinationPath() {
        return destination;
    }
}
