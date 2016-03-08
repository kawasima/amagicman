package net.unit8.amagicman;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class JavaFileUtilsTest {
    @Test
    public void test() {
        assertEquals("abc.def", JavaFileUtils.toPackageName("abc/def/VwXyZ.java"));
        assertNull(JavaFileUtils.toPackageName("VwXyZ.java"));
    }
}
