package net.unit8.amagicman;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author kawasima
 */
public class JavaFileUtilsTest {
    @Test
    public void test() {
        assertThat(JavaFileUtils.toPackageName("abc/def/VwXyZ.java"))
                .isEqualTo("abc.def");
        assertThat(JavaFileUtils.toPackageName("VwXyZ.java"))
                .isNull();
    }
}
