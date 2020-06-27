package net.unit8.amagicman.task;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import net.unit8.amagicman.PathResolverMock;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RewriteJavaSourceTaskTest {
    @Test
    void test() throws Exception {
        RewriteJavaSourceTask task = new RewriteJavaSourceTask("src/test/resources/test1/ControllerTemplate.java",
                cu -> cu.findFirst(ClassOrInterfaceDeclaration.class)
                        .ifPresent(classOrInterface -> classOrInterface.setName("CustomerController2")));
        PathResolverMock resolver = new PathResolverMock();
        task.execute(resolver);
        assertThat(resolver.getWrittenString())
                .contains("public class CustomerController2");
    }
}