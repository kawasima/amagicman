package net.unit8.amagicman.task;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.*;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.unit8.amagicman.GenTask;
import net.unit8.amagicman.JavaFileUtils;
import net.unit8.amagicman.PathResolver;
import net.unit8.amagicman.helper.JDBCTypeMapper;
import net.unit8.amagicman.util.CaseConverter;
import net.unit8.amagicman.util.JavaNodeUtils;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author kawasima
 */
public class JpaEntityTask implements GenTask {
    private final String destination;
    private final CreateTable createTable;

    public JpaEntityTask(String destination, CreateTable createTable) {
        this.destination = destination;
        this.createTable = createTable;
    }

    @Override
    public void execute(PathResolver pathResolver) throws Exception {
        JDBCTypeMapper jdbcTypeMapper = new JDBCTypeMapper();
        CompilationUnit cu = new CompilationUnit();

        String className = destination.replaceAll("(?:^.*/|^)([^/]+)\\.(.*)$", "$1");
        ClassOrInterfaceDeclaration entityClass = cu.addClass(className);
        entityClass.setModifiers(new NodeList<>(Modifier.publicModifier()));
        entityClass.setInterface(false);
        entityClass.addMarkerAnnotation("Entity");
        entityClass.addAnnotation(new NormalAnnotationExpr(
                new Name("Table"),
                NodeList.nodeList(new MemberValuePair("name", new StringLiteralExpr(createTable.getTable().getName())))
        ));

        Optional.ofNullable(JavaFileUtils.toPackageName(destination))
                .ifPresent(pkgName -> cu.setPackageDeclaration(new PackageDeclaration(new Name(pkgName))));

        Set<String> primaryKeys = createTable.getIndexes().stream()
                .filter(index -> index.getType().equalsIgnoreCase("PRIMARY KEY"))
                .flatMap(index -> index.getColumnsNames().stream())
                .collect(Collectors.toSet());

        NodeList<ImportDeclaration> imports = new NodeList<>(
                new ImportDeclaration(new Name("javax.persistence"), false, true)
        );
        cu.setImports(imports);
        createTable.getColumnDefinitions().forEach(column -> {
            String name = column.getColumnName();
            ColDataType colDataType = column.getColDataType();
            Class<?> fieldType = jdbcTypeMapper.getJavaType(colDataType.getDataType());
            FieldDeclaration columnField = entityClass.addField(fieldType, CaseConverter.camelCase(name), Modifier.Keyword.PRIVATE);
            if (primaryKeys.contains(name)) {
                columnField.addAnnotation(new MarkerAnnotationExpr(new Name("Id")));
            }
            columnField.addAnnotation(new NormalAnnotationExpr(
                    new Name("Column"),
                    NodeList.nodeList(new MemberValuePair("name", new StringLiteralExpr(name)))));
        });

        entityClass.getFields().forEach(field -> {
            field.createGetter();
            field.createSetter();
        });

        try (Writer writer = new OutputStreamWriter(pathResolver.destinationAsStream(destination))) {
            writer.write(JavaNodeUtils.toString(cu));
        }
    }

    @Override
    public String getDestinationPath() {
        return destination;
    }

}
