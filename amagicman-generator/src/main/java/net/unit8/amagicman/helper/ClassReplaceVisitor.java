package net.unit8.amagicman.helper;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.QualifiedNameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import net.unit8.amagicman.util.CaseConverter;

import java.util.Set;

/**
 * @author kawasima
 */
public class ClassReplaceVisitor extends VoidVisitorAdapter {
    private String destPascalCase;
    private String destCamelCase;
    private String sourcePascalCase;
    private String sourceCamelCase;

    private Set<String> replaceTargets;

    private String sourcePackageName;
    private String destPackageName;

    public ClassReplaceVisitor(String sourcePackageName, String destPackageName,
                               String sourceName, String destName) {
        this.sourcePackageName = sourcePackageName;
        this.destPackageName = destPackageName;
        this.sourcePascalCase = CaseConverter.pascalCase(sourceName);
        this.sourceCamelCase = CaseConverter.camelCase(sourceName);
        this.destPascalCase = CaseConverter.pascalCase(destName);
        this.destCamelCase = CaseConverter.camelCase(destName);
    }

    @Override
    public void visit(PackageDeclaration dec, Object arg) {
        dec.setName(ASTHelper.createNameExpr(destPackageName));
    }

    @Override
    public void visit(QualifiedNameExpr expr, Object arg) {
        String qualifier = expr.getQualifier().toStringWithoutComments();
        if (qualifier.startsWith(sourcePackageName)) {
            String regexp = sourcePackageName.replaceAll("\\.", "\\.");
            expr.setQualifier(ASTHelper.createNameExpr(
                    qualifier.replaceFirst(regexp, destPackageName)));
        }
        expr.setName(expr.getName().replaceFirst(sourcePascalCase, destPascalCase));
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration expr, Object arg) {
        expr.setName(expr.getName().replaceFirst(sourcePascalCase, destPascalCase));
        super.visit(expr, arg);
    }

    @Override
    public void visit(VariableDeclaratorId id, Object arg) {
        id.setName(id.getName().replaceFirst(sourceCamelCase, destCamelCase));
    }

    @Override
    public void visit(StringLiteralExpr expr, Object arg) {
        expr.setValue(expr.getValue().replaceFirst(sourceCamelCase, destCamelCase));
    }

    @Override
    public void visit(NameExpr expr, Object arg) {
        expr.setName(expr.getName().replaceFirst(sourceCamelCase, destCamelCase));
    }

    @Override
    public void visit(ClassOrInterfaceType expr, Object arg) {
        super.visit(expr, arg);
        expr.setName(expr.getName().replaceFirst(sourcePascalCase, destPascalCase));
    }
}
