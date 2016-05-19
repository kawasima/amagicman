package net.unit8.amagicman.util;

import com.github.javaparser.ast.CompilationUnit;
import net.unit8.amagicman.helper.AmagicmanDumpVisitor;

/**
 * @author kawasima
 */
public class JavaNodeUtils {
    public static String toString(CompilationUnit cu) {
        AmagicmanDumpVisitor visitor = new AmagicmanDumpVisitor();
        visitor.visit(cu, null);
        return visitor.getSource();
    }
}
