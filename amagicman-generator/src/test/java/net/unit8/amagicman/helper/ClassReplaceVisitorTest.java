package net.unit8.amagicman.helper;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author kawasima
 */
public class ClassReplaceVisitorTest {
    @Test
    public void test() {
        CompilationUnit cu = StaticJavaParser.parse(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("test1/UserDao.java")));
        cu.accept(new ClassReplaceVisitor<>("scaffold.crud.", "foo.bar.", "user", "product"), null);
        assertThat(cu.toString()).contains("package foo.bar.dao");
    }
}
