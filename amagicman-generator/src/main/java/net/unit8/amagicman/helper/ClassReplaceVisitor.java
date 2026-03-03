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
 * JavaParser AST visitor that replaces a source class name and package with a destination
 * class name and package throughout a compilation unit.
 *
 * <p>Both the PascalCase form (used for type names) and the camelCase form (used for
 * variable names and string literals) of the source and destination names are computed
 * once at construction time via {@link CaseConverter}.</p>
 *
 * <p>The following AST node types are rewritten:</p>
 * <ul>
 *   <li>{@code PackageDeclaration} — package name is updated by regex replacement</li>
 *   <li>{@code Name} — import qualifiers matching the source package are replaced</li>
 *   <li>{@code ClassOrInterfaceDeclaration} — the class/interface name is replaced</li>
 *   <li>{@code StringLiteralExpr} — first occurrence of the camelCase source name is replaced</li>
 *   <li>{@code NameExpr} — references matching the camelCase source name are replaced</li>
 *   <li>{@code ClassOrInterfaceType} — type references matching the PascalCase source name are replaced</li>
 * </ul>
 *
 * @param <A> argument type passed through the visitor (typically unused; pass {@code null})
 * @author kawasima
 */
public class ClassReplaceVisitor<A> extends VoidVisitorAdapter<A> {
    private final String destPascalCase;
    private final String destCamelCase;
    private final String sourcePascalCase;
    private final String sourceCamelCase;

    private final Pattern sourcePackagePattern;
    private final String destPackageName;

    /**
     * Constructs a {@code ClassReplaceVisitor}.
     *
     * @param sourcePackageName fully-qualified source package name
     *                          (a trailing {@code .} is stripped automatically)
     * @param destPackageName   fully-qualified destination package name to substitute in
     * @param sourceName        source class base name in any recognised case
     *                          (e.g. {@code "user_account"}, {@code "UserAccount"})
     * @param destName          destination class base name in any recognised case
     */
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

    /**
     * Replaces the package name if it matches the source package pattern.
     *
     * @param dec the package declaration node
     * @param arg visitor argument (unused)
     */
    @Override
    public void visit(PackageDeclaration dec, A arg) {
        String templatePackageName = dec.getName().asString();
        dec.setName(new Name(
                sourcePackagePattern.matcher(templatePackageName).replaceFirst(destPackageName)));
    }

    /**
     * Replaces import qualifiers that match the source package.
     *
     * @param expr the name node
     * @param arg  visitor argument (unused)
     */
    @Override
    public void visit(Name expr, A arg) {
        expr.getQualifier().ifPresent(
                qualifier -> {
                    if (sourcePackagePattern.matcher(qualifier.asString()).find()) {
                        qualifier.setQualifier(new Name(destPackageName));
                    }
                }
        );
    }

    /**
     * Renames the class or interface declaration to the destination PascalCase name,
     * then continues visiting child nodes.
     *
     * @param expr the class or interface declaration node
     * @param arg  visitor argument (unused)
     */
    @Override
    public void visit(ClassOrInterfaceDeclaration expr, A arg) {
        expr.getName().replace(new SimpleName(destPascalCase));
        super.visit(expr, arg);
    }

    /**
     * Replaces the first occurrence of the source camelCase name inside a string literal.
     *
     * @param expr the string literal node
     * @param arg  visitor argument (unused)
     */
    @Override
    public void visit(StringLiteralExpr expr, A arg) {
        expr.setValue(expr.getValue().replaceFirst(sourceCamelCase, destCamelCase));
    }

    /**
     * Replaces a name expression that exactly matches the source camelCase name.
     *
     * @param expr the name expression node
     * @param arg  visitor argument (unused)
     */
    @Override
    public void visit(NameExpr expr, A arg) {
        expr.replace(new NameExpr(sourceCamelCase),
                new NameExpr(destCamelCase));
    }

    /**
     * Replaces a class or interface type reference whose identifier matches the source PascalCase name.
     *
     * @param expr the class or interface type node
     * @param arg  visitor argument (unused)
     */
    @Override
    public void visit(ClassOrInterfaceType expr, A arg) {
        super.visit(expr, arg);
        Optional.of(expr.getName())
                .filter(name -> name.getIdentifier().equals(sourcePascalCase))
                .ifPresent(name -> name.replace(new SimpleName(destPascalCase)));
    }
}
