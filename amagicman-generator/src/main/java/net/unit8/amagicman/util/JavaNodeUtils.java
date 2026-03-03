package net.unit8.amagicman.util;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.printer.DefaultPrettyPrinterVisitor;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.printer.configuration.PrinterConfiguration;

/**
 * Utility class for converting a JavaParser AST node to formatted source code.
 *
 * @author kawasima
 */
public class JavaNodeUtils {

    /**
     * Converts a {@link CompilationUnit} to a formatted Java source string using
     * JavaParser's default pretty-printer.
     *
     * @param cu the compilation unit to format
     * @return formatted Java source code
     */
    public static String toString(CompilationUnit cu) {
        final PrinterConfiguration config = new DefaultPrinterConfiguration();
        VoidVisitor<?> visitor = new DefaultPrettyPrinterVisitor(config);
        visitor.visit(cu, null);
        return visitor.toString();
    }
}
