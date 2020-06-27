package net.unit8.amagicman.task;

import net.unit8.amagicman.PathResolverMock;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContentsReplaceTaskTest {
    @Test
    void test() throws Exception {
        ContentsReplaceTask task = new ContentsReplaceTask("test1/foo.txt",
                "dummy",
                s -> s.replaceAll("def", "123")
                );
        PathResolverMock resolver = new PathResolverMock();
        task.execute(resolver);
        assertThat(resolver.getWrittenString())
                .contains("123")
                .doesNotContain("def");
    }
}