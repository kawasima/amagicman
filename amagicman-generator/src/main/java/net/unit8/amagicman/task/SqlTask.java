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
 * @author kawasima
 */
public class SqlTask implements GenTask {
    private String destination;
    private String source;
    private SqlTemplateProcess process;

    public SqlTask (String source, String destination, SqlTemplateProcess process) {
        this.source = source;
        this.destination = destination;
        this.process = process;
    }

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

    @Override
    public String getDestinationPath() {
        return destination;
    }

    public interface SqlTemplateProcess {
        void process(Statement statement);
    }
}
