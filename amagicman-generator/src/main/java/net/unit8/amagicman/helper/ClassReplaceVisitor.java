package net.unit8.amagicman.helper;

import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import net.unit8.amagicman.util.CaseConverter;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * @author kawasima
 */
public class ClassReplaceVisitor<A> extends VoidVisitorAdapter<A> {
    private final String destPascalCase;
    private final String destCamelCase;
    private final String sourcePascalCase;
    private final String sourceCamelCase;

    private final Pattern sourcePackagePattern;
    private final String destPackageName;

    public ClassReplaceVisitor(String sourcePackageName, String destPackageName,
                               String sourceName, String destName) {
        if (sourcePackageName.endsWith(".")) {
            sourcePackageName = sourcePackageName.substring(0, sourcePackageName.length() - 1);
        }
        this.sourcePackagePattern = Pattern.compile(
                sourcePackageName.replaceAll("\\.", "\\.") + "(\\.|$)");
        this.destPackageName = destPackageName;
        this.sourcePascalCase = CaseConverter.pascalCase(sourceName);
        this.sourceCamelCase = CaseConverter.camelCase(sourceName);
        this.destPascalCase = CaseConverter.pascalCase(destName);
        this.destCamelCase = CaseConverter.camelCase(destName);
    }

    @Override
    public void visit(PackageDeclaration dec, A arg) {
        String templatePackageName = dec.getName().asString();
        dec.setName(new Name(
                sourcePackagePattern.matcher(templatePackageName).replaceFirst(destPackageName)));
    }

    @Override
    public void visit(Name expr, A arg) {
        expr.getQualifier().ifPresent(
                qualifier -> {
                    if (sourcePackagePattern.matcher(qualifier.asString()).find()) {
                        qualifier.setQualifier(new Name(destPackageName));
                    }
                }
        );
        //expr.setName(expr.getName().replaceFirst(sourcePascalCase, destPascalCase));
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration expr, A arg) {
        expr.getName().replace(new SimpleName(destPascalCase));
        super.visit(expr, arg);
    }

    @Override
    public void visit(StringLiteralExpr expr, A arg) {
        expr.setValue(expr.getValue().replaceFirst(sourceCamelCase, destCamelCase));
    }

    @Override
    public void visit(NameExpr expr, A arg) {
        expr.replace(new NameExpr(sourceCamelCase),
                new NameExpr(destCamelCase));
    }

    @Override
    public void visit(ClassOrInterfaceType expr, A arg) {
        super.visit(expr, arg);
        Optional.of(expr.getName())
                .filter(name -> name.getIdentifier().equals(sourcePascalCase))
                .ifPresent(name -> name.replace(new SimpleName(destPascalCase)));
    }
}
