package net.unit8.amagicman.task;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.unit8.amagicman.PathResolverMock;
import org.junit.Test;

/**
 * @author kawasima
 */
public class JpaEntityTaskTest {
    @Test
    public void test() throws Exception{
        JpaEntityTask task = new JpaEntityTask("example/entity/User.java",
                (CreateTable) CCJSqlParserUtil.parse("CREATE TABLE USERS "
                        + "(id NUMBER NOT NULL, name VARCHAR2(255), PRIMARY KEY(id))"));
        PathResolverMock resolver = new PathResolverMock();
        task.execute(resolver);
        System.out.println(resolver.getWrittenString());
    }
}
