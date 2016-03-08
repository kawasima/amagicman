package net.unit8.amagicman;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class CaseConvertTest {
    @Test
    public void test() {
        String destination = "foo/bar/Name.java";
        String className = destination.replaceAll("(?:.*/|^)([^/]+)\\.(.*)$", "$1");
        assertEquals("Name", className);

        destination = "Name.java";
        className = destination.replaceAll("(?:.*/|^)([^/]+)\\.(.*)$", "$1");
        assertEquals("Name", className);

        destination = ".java";
        className = destination.replaceAll("(?:.*/|^)([^/]+)\\.(.*)$", "$1");
        assertEquals(".java", className);
    }
}
