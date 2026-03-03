package net.unit8.amagicman;

import java.io.File;

/**
 * Utility class for Java source file path operations.
 *
 * @author kawasima
 */
public class JavaFileUtils {

    /**
     * Derives a Java package name from a relative source file path.
     *
     * <p>Example: {@code "com/example/foo/Bar.java"} → {@code "com.example.foo"}</p>
     *
     * <p>Both the OS-specific file separator ({@link File#separatorChar}) and {@code /}
     * are replaced with {@code .}, ensuring correct behaviour on both Windows and Unix.</p>
     *
     * @param path relative path to a Java source file
     * @return the package name, or {@code null} if the file has no parent directory
     *         (i.e. it belongs to the default package)
     */
    public static String toPackageName(String path) {
        String parentPath = new File(path).getParent();
        if (parentPath == null) return null;
        return parentPath.replace(File.separatorChar, '.').replace('/', '.');
    }
}
