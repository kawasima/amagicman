package net.unit8.amagicman.task;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.unit8.amagicman.GenTask;
import net.unit8.amagicman.PathResolver;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * @author kawasima
 */
public class TableTask implements GenTask {
    private String createTable;
    private DataSource dataSource;

    protected void createTable() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute(createTable);
            conn.commit();
        }
    }


    @Override
    public void execute(PathResolver pathResolver) throws Exception {
        Statement stmt = CCJSqlParserUtil.parse(createTable);
        String tableName = Optional.of(stmt)
                .filter(CreateTable.class::isInstance)
                .map(CreateTable.class::cast)
                .map(CreateTable::getTable)
                .map(Table::getName).orElseThrow(() -> new IllegalArgumentException(createTable));

        createTable();
    }

    @Override
    public String getDestinationPath() {
        return null;
    }


}
