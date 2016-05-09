package net.unit8.amagicman.task;

import net.unit8.amagicman.GenTask;
import net.unit8.amagicman.PathResolver;

import java.io.*;
import java.util.stream.Collectors;

/**
 * @author kawasima
 */
public class ContentsReplaceTask implements GenTask {
    private String destination;
    private String source;
    private ContentsReplaceProcessor processor;

    public ContentsReplaceTask(String source, String destination, ContentsReplaceProcessor processor) {
        this.source = source;
        this.destination = destination;
        this.processor = processor;
    }

    @Override
    public void execute(PathResolver pathResolver) throws Exception {
        String contents;
        try (InputStream is = pathResolver.templateAsStream(source);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            contents = reader.lines().collect(Collectors.joining("\n"));
        }

        contents = processor.process(contents);

        try (Writer wtr = new OutputStreamWriter(pathResolver.destinationAsStream(destination))) {
            wtr.write(contents);
        }

    }

    @Override
    public String getDestinationPath() {
        return destination;
    }

    public interface ContentsReplaceProcessor {
        String process(String contents);
    }
}
