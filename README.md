# amagicman

**amagicman** is a Java-based scaffolding and code-generation library. It lets you define a sequence of generation tasks — copying files, applying text replacements, rewriting Java ASTs, generating JPA entities from SQL, or patching `pom.xml` — and run them all in one shot.

The key idea: **templates stay as ordinary Java source files, SQL files, XML files, etc.** There is no custom template language to learn and no separate template engine to maintain. Transformation logic is expressed as plain Java lambdas or visitor callbacks, so templates compile and run like any other code.

---

## Modules

| Module | Description |
|---|---|
| `amagicman-generator` | Core library: `Generator`, `PathResolver`, and all built-in tasks |
| `amagicman-maven-plugin` | Maven plugin for packaging a template JAR to distribute with your generator |

---

## Getting Started

### Add the dependency

```xml
<dependency>
    <groupId>net.unit8.amagicman</groupId>
    <artifactId>amagicman-generator</artifactId>
    <version>0.3.0</version>
</dependency>
```

### Configure a PathResolver

`PathResolverImpl` tells the generator where to find template resources and where to write output files.

```java
PathResolver resolver = new PathResolverImpl(
    "/path/to/project",           // project root (used by PomTask)
    "META-INF/amagicman/templates", // classpath prefix for templates
    "/path/to/output"             // root directory for generated files
);
```

### Build and run a Generator

```java
new Generator()
    .setPathResolver(resolver)
    .writing("controllers", g -> {
        g.task(new CopyTask(
            "com/example/SampleController.java",
            "src/main/java/com/example/UserController.java"));
    })
    .writing("pom", g -> {
        g.task(new PomTask()
            .addDependency("org.springframework.boot", "spring-boot-starter-web", "3.4.3"));
    })
    .invoke();
```

Each task runs in registration order. Errors in individual tasks are logged and skipped — execution always continues to the next task.

---

## Built-in Tasks

### `CopyTask` — binary file copy

Copies a template resource to a destination path byte-for-byte. Suitable for images, compiled resources, or any file that does not require transformation.

```java
new CopyTask(
    "assets/logo.png",
    "src/main/webapp/images/logo.png"
)
```

---

### `ContentsReplaceTask` — text substitution

Reads a template text file into memory, applies a string transformation supplied as a lambda, and writes the result to the destination.

```java
new ContentsReplaceTask(
    "template/config.properties",
    "src/main/resources/config.properties",
    contents -> contents
        .replace("{{appName}}", "myapp")
        .replace("{{port}}", "8080")
)
```

---

### `JavaByTemplateTask` — generate Java source from a template

Parses a template Java file into a JavaParser `CompilationUnit`, applies AST transformations, and writes the pretty-printed result. Use this to create a new Java class based on an existing template.

```java
new JavaByTemplateTask(
    "template/SampleController.java",
    "src/main/java/com/example/UserController.java",
    cu -> new ClassReplaceVisitor(
        "com.example.template", // original package
        "com.example",          // new package
        "Sample",               // original class name prefix
        "User"                  // new class name prefix
    ).visit(cu, null)
)
```

`ClassReplaceVisitor` (provided in the `helper` package) renames the class declaration, the package, and all import and type references in one pass.

---

### `RewriteJavaSourceTask` — modify an existing Java source file

Parses a Java file that already exists on disk, applies AST transformations, and overwrites it. Useful for incremental changes such as adding methods or annotations to already-generated sources.

```java
new RewriteJavaSourceTask(
    "src/main/java/com/example/UserController.java",
    cu -> cu.getClassByName("UserController").ifPresent(cls ->
        cls.addAnnotation("Transactional"))
)
```

---

### `JpaEntityTask` — generate a JPA entity from a CREATE TABLE statement

Takes a JSqlParser `CreateTable` AST and generates a full JPA entity class: `@Entity`, `@Table`, `@Column` per column, `@Id` on primary key fields, and getter/setter pairs. Column types are mapped to Java types via `JDBCTypeMapper`.

```java
CreateTable ct = (CreateTable) CCJSqlParserUtil.parse(
    "CREATE TABLE users (" +
    "  id BIGINT PRIMARY KEY," +
    "  user_name VARCHAR(255)," +
    "  created_at TIMESTAMP" +
    ")");

generator.task(new JpaEntityTask(
    "com/example/entity/Users.java",  // determines class name and package
    ct
));
```

Generated output (abbreviated):

```java
package com.example.entity;

import javax.persistence.*;

@Entity
@Table(name = "users")
public class Users {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "created_at")
    private Timestamp createdAt;

    // getters and setters ...
}
```

#### SQL type mapping

`JDBCTypeMapper` converts vendor SQL type names to Java classes. Common mappings:

| SQL type | Java type |
|---|---|
| `INT`, `INTEGER`, `INT4` | `Integer` |
| `BIGINT`, `INT8`, `IDENTITY` | `Long` |
| `VARCHAR`, `VARCHAR2`, `NVARCHAR` | `String` |
| `CHAR`, `NCHAR` | `String` |
| `BOOLEAN`, `BIT`, `BOOL` | `Boolean` |
| `DECIMAL`, `NUMERIC`, `NUMBER` | `BigDecimal` |
| `DOUBLE`, `FLOAT8` | `Double` |
| `FLOAT`, `REAL` | `Float` |
| `DATE` | `java.util.Date` |
| `TIME` | `java.sql.Time` |
| `TIMESTAMP`, `DATETIME` | `java.sql.Timestamp` |
| `BLOB`, `TINYBLOB`, `IMAGE` | `java.sql.Blob` |
| `CLOB`, `TEXT`, `LONGTEXT` | `java.sql.Clob` |
| `BINARY`, `VARBINARY`, `BYTEA` | `byte[]` |

Custom mappings can be added at runtime:

```java
JDBCTypeMapper mapper = new JDBCTypeMapper();
mapper.setVendorType("UUID", JDBCType.VARCHAR);
mapper.setJDBCType(JDBCType.VARCHAR, UUID.class);
```

---

### `SqlTask` — transform a SQL file

Parses a template SQL file via JSqlParser, applies a transformation callback, and writes the resulting SQL to the destination.

```java
new SqlTask(
    "template/create_table.sql",
    "src/main/resources/db/migration/V1__create_users.sql",
    stmt -> {
        CreateTable ct = (CreateTable) stmt;
        ct.getTable().setName("users");
    }
)
```

---

### `PomTask` — add Maven dependencies to pom.xml

Parses the project's `pom.xml` as a DOM document, inserts missing dependencies (duplicate detection by `groupId:artifactId:version`), and writes the result back as formatted XML.

```java
new PomTask()
    .addDependency("org.slf4j", "slf4j-api", "2.0.17")
    .addDependency("org.junit.jupiter", "junit-jupiter", "5.12.0", "test")
```

The `<dependencies>` element is created automatically if it does not exist.

---

## Utilities

### `CaseConverter`

Converts identifiers between naming conventions. Token boundaries are detected at whitespace characters (`-`, `_`, space), lower-to-upper transitions, digit boundaries, and consecutive-uppercase patterns (e.g. `XMLParser` → `["XML", "Parser"]`).

| Method | Input | Output |
|---|---|---|
| `camelCase(s)` | `"USER_NAME"` | `"userName"` |
| `pascalCase(s)` | `"user_name"` | `"UserName"` |
| `snakeCase(s)` | `"UserName"` | `"user_name"` |
| `screamingSnakeCase(s)` | `"userName"` | `"USER_NAME"` |

---

## Maven Plugin — packaging a template JAR

If you want to ship a reusable generator as a JAR that others can depend on, use `amagicman-maven-plugin`. The `build-template` goal packages your template files into a JAR so that `PathResolverImpl` can load them from the classpath at runtime.

```xml
<plugin>
    <groupId>net.unit8.amagicman</groupId>
    <artifactId>amagicman-maven-plugin</artifactId>
    <version>0.2.0</version>
    <executions>
        <execution>
            <goals>
                <goal>build-template</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Place template files under `src/main/resources/META-INF/amagicman/templates/`. They will be bundled into the output JAR and loadable via `PathResolverImpl` with `templatePrefix = "META-INF/amagicman/templates"`.

---

## Listening to task execution

Register a `TaskListener` to hook into task lifecycle events:

```java
generator.addTaskListener(new TaskListener() {
    @Override
    public void beforeTask(GenTask task) {
        System.out.println("Starting: " + task.getDestinationPath());
    }

    @Override
    public void afterTask(GenTask task) {
        System.out.println("Done: " + task.getDestinationPath());
    }
});
```

---

## License

MIT License — see [LICENSE](LICENSE) for details.
