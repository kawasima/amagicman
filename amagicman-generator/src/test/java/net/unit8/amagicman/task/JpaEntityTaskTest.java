package net.unit8.amagicman.task;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.unit8.amagicman.PathResolverMock;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author kawasima
 */
class JpaEntityTaskTest {
    @Test
     void test() throws Exception{
        JpaEntityTask task = new JpaEntityTask("example/entity/SystemUser.java",
                (CreateTable) CCJSqlParserUtil.parse("CREATE TABLE SYSTEM_USERS "
                        + "("
                        + "id BIGINT NOT NULL,"
                        + "name VARCHAR2(255),"
                        + "age INTEGER,"
                        + "created_at TIMESTAMP,"
                        + "birth_date DATE,"
                        + "PRIMARY KEY(id))"));
        PathResolverMock resolver = new PathResolverMock();
        task.execute(resolver);
        assertThat(resolver.getWrittenString()).contains("public class SystemUser");
    }
}
