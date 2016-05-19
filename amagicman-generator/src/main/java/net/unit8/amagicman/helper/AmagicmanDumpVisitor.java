package net.unit8.amagicman.helper;

import com.github.javaparser.ast.TypeParameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.visitor.DumpVisitor;

import java.lang.reflect.Field;
import java.util.Iterator;

/**
 * JavaParser v2.3.0 (also 2.4.0) has a bug in DumpVisitor that prints unneeded type parameters.
 * So it causes a compile error.
 *
 * This class is a patch for the bug.
 *
 * @author kawasima
 */
public class AmagicmanDumpVisitor extends DumpVisitor {
    private final Field printerField;

    public AmagicmanDumpVisitor() {
        try {
            printerField = DumpVisitor.class.getDeclaredField("printer");
            printerField.setAccessible(true);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("It's a product bug", ex);
        }
    }

    private SourcePrinter printer() {
        try {
            return (SourcePrinter) printerField.get(this);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("It's a product bug", ex);
        }
    }
    @Override
    public void visit(MethodReferenceExpr n, Object arg) {
        Expression scope = n.getScope();
        String identifier = n.getIdentifier();
        if (scope != null) {
            n.getScope().accept(this, arg);
        }

        printer().print("::");
        if (!n.getTypeParameters().isEmpty()) {
            printer().print("<");
            for (Iterator<TypeParameter> i = n.getTypeParameters().iterator(); i
                    .hasNext();) {
                TypeParameter p = i.next();
                p.accept(this, arg);
                if (i.hasNext()) {
                    printer().print(", ");
                }
            }
            printer().print(">");
        }
        if (identifier != null) {
            printer().print(identifier);
        }

    }

}
