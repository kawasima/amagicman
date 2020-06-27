package net.unit8.amagicman.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author kawasima
 */
public class CaseConverterTest {
    @Test
    public void test() {
        assertThat(CaseConverter.camelCase("CAMEL_CASE")).isEqualTo("camelCase");
        assertThat(CaseConverter.pascalCase("PASCAL_CASE")).isEqualTo("PascalCase");
        assertThat(CaseConverter.snakeCase("snakeCase")).isEqualTo("snake_case");
        assertThat(CaseConverter.screamingSnakeCase("screaming snakeCase")).isEqualTo("SCREAMING_SNAKE_CASE");
    }

    @Test
    public void oneChar() {
        assertThat(CaseConverter.camelCase("C")).isEqualTo("c");
        assertThat(CaseConverter.pascalCase("p")).isEqualTo("P");
        assertThat(CaseConverter.snakeCase("s")).isEqualTo("s");
        assertThat(CaseConverter.screamingSnakeCase("s")).isEqualTo("S");
    }

    @Test
    public void containsNumber() {
        assertThat(CaseConverter.camelCase("C3P0")).isEqualTo("c3P0");
        assertThat(CaseConverter.pascalCase("C_3_P_0")).isEqualTo("C3P0");
        assertThat(CaseConverter.snakeCase("s3-key")).isEqualTo("s_3_key");
        assertThat(CaseConverter.snakeCase("c3p0")).isEqualTo("c_3p_0");
    }

    @Test
    public void irregularCase() {
        assertThat(CaseConverter.camelCase("")).isEmpty();
        assertThat(CaseConverter.pascalCase("")).isEmpty();
        assertThat(CaseConverter.snakeCase("")).isEmpty();
        assertThat(CaseConverter.screamingSnakeCase("")).isEmpty();

        assertThat(CaseConverter.camelCase(null)).isNull();
        assertThat(CaseConverter.pascalCase(null)).isNull();
        assertThat(CaseConverter.snakeCase(null)).isNull();
        assertThat(CaseConverter.screamingSnakeCase(null)).isNull();
    }
}
