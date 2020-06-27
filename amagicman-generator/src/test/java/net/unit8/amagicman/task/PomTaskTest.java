package net.unit8.amagicman.task;

import net.unit8.amagicman.PathResolverMock;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class PomTaskTest {
    @Test
    void test() throws Exception {
        PomTask task = new PomTask("src/test/resources/test1/pom-test1.xml");
        task.addDependency("net.unit8", "foobar", "0.0.0");
        PathResolverMock resolver = new PathResolverMock();
        task.execute(resolver);
        assertThat(resolver.getWrittenString())
                .contains("<groupId>net.unit8</groupId>")
                .contains("<artifactId>foobar</artifactId>")
                .contains("<version>0.0.0</version>");

    }

    @Test
    void testWithScope() throws Exception {
        PomTask task = new PomTask();
        task.setPomFile(new File("src/test/resources/test1/pom-test1.xml"));
        task.addDependency("net.unit8", "foobar", "0.0.0", "runtime");
        PathResolverMock resolver = new PathResolverMock();
        task.execute(resolver);
        assertThat(resolver.getWrittenString())
                .contains("<groupId>net.unit8</groupId>")
                .contains("<artifactId>foobar</artifactId>")
                .contains("<version>0.0.0</version>")
                .contains("<scope>runtime</scope>");

    }

}