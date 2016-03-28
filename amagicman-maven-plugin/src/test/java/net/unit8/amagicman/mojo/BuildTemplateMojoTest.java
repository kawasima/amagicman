package net.unit8.amagicman.mojo;

import org.junit.Test;

import java.io.File;

/**
 * @author kawasima
 */
public class BuildTemplateMojoTest {
    @Test
    public void test() throws Exception {
        BuildTemplateMojo mojo = new BuildTemplateMojo();
        mojo.distribution = new File(getClass().getResource("/distro.txt").toURI());
        mojo.name = "test";
        mojo.prefix = "META-INF/amagicman/templates";
        mojo.execute();
    }
}
