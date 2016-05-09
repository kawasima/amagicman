package net.unit8.amagicman.helper;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class ClassReplaceVisitorTest {
    @Test
    public void test() throws ParseException {
        CompilationUnit cu = JavaParser.parse(ClassLoader.getSystemResourceAsStream("test1/UserDao.java"));
        cu.accept(new ClassReplaceVisitor("scaffold.crud.", "foo.bar.", "user", "product"), null);
        assertTrue(cu.toString().contains("package foo.bar.dao"));
    }
}
