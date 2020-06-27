package net.unit8.amagicman.util;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.PrettyPrintVisitor;
import com.github.javaparser.printer.PrettyPrinterConfiguration;

/**
 * @author kawasima
 */
public class JavaNodeUtils {
    public static String toString(CompilationUnit cu) {
        final PrettyPrinterConfiguration config = new PrettyPrinterConfiguration();
        config.setIndentSize(4);
        PrettyPrintVisitor visitor = new PrettyPrintVisitor(config);
        visitor.visit(cu, null);
        return visitor.toString();
    }
}
