package net.unit8.amagicman.task;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import net.unit8.amagicman.GenTask;
import net.unit8.amagicman.PathResolver;
import net.unit8.amagicman.util.JavaNodeUtils;

import java.io.*;

/**
 * @author kawasima
 */
public class RewriteJavaSourceTask implements GenTask {
    private final String destination;
    private final RewriteJavaSourceProcess process;

    public RewriteJavaSourceTask(String destination, RewriteJavaSourceProcess process) {
        this.destination = destination;
        this.process = process;
    }

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

    @FunctionalInterface
    public interface RewriteJavaSourceProcess {
        void process(CompilationUnit cu);
    }

    public String getDestinationPath() {
        return destination;
    }
}
