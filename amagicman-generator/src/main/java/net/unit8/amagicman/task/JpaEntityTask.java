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
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A task that generates a JPA entity class from a parsed {@code CREATE TABLE} SQL statement.
 *
 * <p>The generated class is annotated with {@code @Entity} and {@code @Table}, and each
 * column is mapped to a private field annotated with {@code @Column}. Primary key columns
 * receive an additional {@code @Id} annotation. Getter and setter methods are generated
 * for every field. SQL column types are mapped to Java types via {@link JDBCTypeMapper}.</p>
 *
 * <p>The destination file path determines both the class name (from the filename) and the
 * package name (from the parent directory path).</p>
 *
 * <pre>{@code
 * CreateTable createTable = (CreateTable) CCJSqlParserUtil.parse(
 *     "CREATE TABLE users (id BIGINT PRIMARY KEY, name VARCHAR(255))");
 * generator.task(new JpaEntityTask("com/example/entity/Users.java", createTable));
 * }</pre>
 *
 * @author kawasima
 */
public class JpaEntityTask implements GenTask {
    private final String destination;
    private final CreateTable createTable;

    /**
     * Constructs a {@code JpaEntityTask}.
     *
     * @param destination relative path of the output Java file; the file name (without extension)
     *                    is used as the class name, and the parent path is used as the package name
     * @param createTable parsed {@code CREATE TABLE} statement that describes the entity's schema
     */
    public JpaEntityTask(String destination, CreateTable createTable) {
        this.destination = destination;
        this.createTable = createTable;
    }

    /**
     * Generates the JPA entity class and writes it to the destination file.
     *
     * @param pathResolver resolver for the destination output
     * @throws Exception if code generation or I/O fails
     */
    @Override
    public void execute(PathResolver pathResolver) throws Exception {
        JDBCTypeMapper jdbcTypeMapper = new JDBCTypeMapper();
        CompilationUnit cu = new CompilationUnit();

        String fileName = Path.of(destination).getFileName().toString();
        String className = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDestinationPath() {
        return destination;
    }

}
