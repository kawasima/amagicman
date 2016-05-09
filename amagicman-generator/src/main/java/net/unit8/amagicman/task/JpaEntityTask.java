package net.unit8.amagicman.task;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.ModifierSet;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.type.ReferenceType;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.unit8.amagicman.JavaFileUtils;
import net.unit8.amagicman.GenTask;
import net.unit8.amagicman.PathResolver;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author kawasima
 */
public class JpaEntityTask implements GenTask {
    private String destination;
    private CreateTable createTable;

    public JpaEntityTask(String destination, CreateTable createTable) {
        this.destination = destination;
        this.createTable = createTable;
    }

    @Override
    public void execute(PathResolver pathResolver) throws Exception {
        CompilationUnit cu = new CompilationUnit();

        String className = destination.replaceAll("(?:^.*/|^)([^/]+)\\.(.*)$", "$1");
        ClassOrInterfaceDeclaration entityClass = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, className);
        entityClass.setAnnotations(Collections.singletonList(new MarkerAnnotationExpr(ASTHelper.createNameExpr("Entity"))));
        ASTHelper.addTypeDeclaration(cu, entityClass);

        Optional.ofNullable(JavaFileUtils.toPackageName(destination))
                .ifPresent(pkgName -> cu.setPackage(new PackageDeclaration(ASTHelper.createNameExpr(pkgName))));

        ReferenceType stringClass = ASTHelper.createReferenceType("String", 0);

        Set<String> primaryKeys = createTable.getIndexes().stream()
                .filter(index -> index.getType().equalsIgnoreCase("PRIMARY KEY"))
                .flatMap(index -> index.getColumnsNames().stream())
                .collect(Collectors.toSet());

        List<ImportDeclaration> imports = new ArrayList<>();
        imports.add(new ImportDeclaration(ASTHelper.createNameExpr("javax.persistence"), false, true));
        cu.setImports(imports);
        createTable.getColumnDefinitions().stream().forEach(column -> {
            String name = column.getColumnName();
            ColDataType colDataType = column.getColDataType();
            List<AnnotationExpr> annotations = new ArrayList<>();
            if (primaryKeys.contains(name)) {
                annotations.add(new MarkerAnnotationExpr(ASTHelper.createNameExpr("Id")));
            }
            ASTHelper.addMember(entityClass, new FieldDeclaration(ModifierSet.PRIVATE, annotations, stringClass, ASTHelper.createVariableDeclarationExpr(stringClass, name).getVars()));
        });

        try (Writer writer = new OutputStreamWriter(pathResolver.destinationAsStream(destination))) {
            writer.write(cu.toString());
        }
    }

    @Override
    public String getDestinationPath() {
        return destination;
    }

}
