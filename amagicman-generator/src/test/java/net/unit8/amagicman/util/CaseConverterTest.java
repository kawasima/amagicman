package net.unit8.amagicman.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class CaseConverterTest {
    @Test
    public void test() {
        assertEquals("camelCase", CaseConverter.camelCase("CAMEL_CASE"));
        assertEquals("PascalCase", CaseConverter.pascalCase("PASCAL_CASE"));
        assertEquals("snake_case", CaseConverter.snakeCase("snakeCase"));
        assertEquals("SCREAMING_SNAKE_CASE", CaseConverter.screamingSnakeCase("screaming snakeCase"));
    }

    @Test
    public void oneChar() {
        assertEquals("c", CaseConverter.camelCase("C"));
        assertEquals("P", CaseConverter.pascalCase("p"));
        assertEquals("s", CaseConverter.snakeCase("s"));
        assertEquals("S", CaseConverter.screamingSnakeCase("s"));
    }

    @Test
    public void containsNumber() {
        assertEquals("c3P0", CaseConverter.camelCase("C3P0"));
        assertEquals("C3P0", CaseConverter.pascalCase("C_3_P_0"));
        assertEquals("s_3_key", CaseConverter.snakeCase("s3-key"));
        assertEquals("c_3p_0", CaseConverter.snakeCase("c3p0"));
    }

    @Test
    public void irregularCase() {
        assertEquals("", CaseConverter.camelCase(""));
        assertEquals("", CaseConverter.pascalCase(""));
        assertEquals("", CaseConverter.snakeCase(""));
        assertEquals("", CaseConverter.screamingSnakeCase(""));

        assertNull(CaseConverter.camelCase(null));
        assertNull(CaseConverter.pascalCase(null));
        assertNull(CaseConverter.snakeCase(null));
        assertNull(CaseConverter.screamingSnakeCase(null));
    }
}
