package net.unit8.amagicman.task;

import net.unit8.amagicman.GenTask;
import net.unit8.amagicman.PathResolver;

import java.io.*;
import java.util.stream.Collectors;

/**
 * A task that reads a template text file, applies a string transformation, and writes
 * the result to a destination file.
 *
 * <p>The transformation logic is provided by a {@link ContentsReplaceProcessor}, which
 * can be supplied as a lambda. The entire template is loaded into memory as a single
 * string before processing.</p>
 *
 * <pre>{@code
 * new ContentsReplaceTask(
 *     "template/config.properties",
 *     "src/main/resources/config.properties",
 *     contents -> contents.replace("{{appName}}", "myapp")
 * )
 * }</pre>
 *
 * @author kawasima
 */
public class ContentsReplaceTask implements GenTask {
    private final String destination;
    private final String source;
    private final ContentsReplaceProcessor processor;

    /**
     * Constructs a {@code ContentsReplaceTask}.
     *
     * @param source      relative path of the source template resource
     * @param destination relative path of the output file
     * @param processor   transformation applied to the template contents
     */
    public ContentsReplaceTask(String source, String destination, ContentsReplaceProcessor processor) {
        this.source = source;
        this.destination = destination;
        this.processor = processor;
    }

    /**
     * Reads the template, applies the processor, and writes the result to the destination.
     *
     * @param pathResolver resolver for template input and destination output
     * @throws Exception if an I/O error occurs
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDestinationPath() {
        return destination;
    }

    /**
     * Functional interface that transforms the full text contents of a template file.
     */
    @FunctionalInterface
    public interface ContentsReplaceProcessor {

        /**
         * Transforms the given template contents and returns the result.
         *
         * @param contents the full text of the template
         * @return the transformed text to be written to the destination
         */
        String process(String contents);
    }
}
