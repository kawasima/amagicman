package net.unit8.amagicman;

import java.io.File;

/**
 * @author kawasima
 */
public class JavaFileUtils {
    public static String toPackageName(String path) {
        String parentPath = new File(path).getParent();
        if (parentPath == null) return null;
        return parentPath.replaceAll("/", ".");
    }
}
