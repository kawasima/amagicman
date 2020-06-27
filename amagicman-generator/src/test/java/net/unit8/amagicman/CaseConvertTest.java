package net.unit8.amagicman;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author kawasima
 */
public class CaseConvertTest {
    @Test
    public void test() {
        String destination = "foo/bar/Name.java";
        String className = destination.replaceAll("(?:.*/|^)([^/]+)\\.(.*)$", "$1");
        assertThat(className).isEqualTo("Name");

        destination = "Name.java";
        className = destination.replaceAll("(?:.*/|^)([^/]+)\\.(.*)$", "$1");
        assertThat(className).isEqualTo("Name");

        destination = ".java";
        className = destination.replaceAll("(?:.*/|^)([^/]+)\\.(.*)$", "$1");
        assertThat(className).isEqualTo(".java");
    }
}
