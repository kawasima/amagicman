package net.unit8.amagicman.task;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.unit8.amagicman.GenTask;
import net.unit8.amagicman.PathResolver;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * A task that generates a SQL file by parsing a template SQL file and applying
 * transformations via a {@link SqlTemplateProcess}.
 *
 * <p>The template SQL is parsed into a JSqlParser {@link Statement}, then passed
 * to the provided {@code process} for modification (e.g. renaming tables or
 * adjusting column definitions). The transformed statement is serialised back to
 * SQL text and written to the destination file.</p>
 *
 * @author kawasima
 */
public class SqlTask implements GenTask {
    private final String destination;
    private final String source;
    private final SqlTemplateProcess process;

    /**
     * Constructs a {@code SqlTask}.
     *
     * @param source      relative path of the template SQL resource on the classpath
     * @param destination relative path of the output SQL file
     * @param process     transformation applied to the parsed SQL statement
     */
    public SqlTask(String source, String destination, SqlTemplateProcess process) {
        this.source = source;
        this.destination = destination;
        this.process = process;
    }

    /**
     * Parses the template SQL, applies the transformation, and writes the result.
     *
     * @param pathResolver resolver for template input and destination output
     * @throws FileNotFoundException if the template resource is not found on the classpath
     * @throws Exception             if SQL parsing or I/O fails
     */
    @Override
    public void execute(PathResolver pathResolver) throws Exception {
        Statement stmt;

        try (InputStream is = pathResolver.templateAsStream(source)) {
            if (is == null) throw new FileNotFoundException(source);
            stmt = CCJSqlParserUtil.parse(is);
        }

        process.process(stmt);

        try (Writer wtr = new OutputStreamWriter(pathResolver.destinationAsStream(destination))) {
            wtr.write(stmt.toString());
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
     * Callback interface for transforming a parsed SQL {@link Statement}.
     */
    public interface SqlTemplateProcess {

        /**
         * Applies in-place modifications to the given SQL statement.
         *
         * @param statement the parsed SQL statement to transform
         */
        void process(Statement statement);
    }
}
