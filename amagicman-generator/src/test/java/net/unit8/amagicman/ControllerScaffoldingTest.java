package net.unit8.amagicman;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.unit8.amagicman.listener.TaskListener;
import net.unit8.amagicman.task.CopyTask;
import net.unit8.amagicman.task.JavaByTemplateTask;
import net.unit8.amagicman.task.PomTask;
import net.unit8.amagicman.task.SqlTask;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author kawasima
 */
public class ControllerScaffoldingTest {
    private static class DaoVisitor<A> extends VoidVisitorAdapter<A> {
        @Override
        public void visit(NameExpr expr, A arg) {
            if (expr.getName().getIdentifier().equals("customerDao")) {
                expr.setName("pongeDao");
            }
        }

        @Override
        public  void visit(ClassOrInterfaceType expr, A arg) {
            if (expr.getName().getIdentifier().equals("CustomerDao")) {
                expr.setName("PongeDao");
            } else if (expr.getName().getIdentifier().equals("Customer")) {
                expr.setName("Ponge");
            }

        }
    }

    @Test
    public void test() {
        PathResolver pathResolver = new PathResolverImpl(null, "test1", "target/test1");
        Generator crudApplication = new Generator()
                .setPathResolver(pathResolver)
                .addTaskListener(new TaskListener() {
                    @Override
                    public void beforeTask(GenTask task) {
                        assertThat(task).isNotNull();
                    }

                    @Override
                    public void afterTask(GenTask task) {
                        assertThat(task).isNotNull();
                    }
                })
                .writing("Controller", g ->
                        g.task(new JavaByTemplateTask("ControllerTemplate.java", "src/main/java/example/hoge/TestController.java", cu -> {
                            cu.setPackageDeclaration(new PackageDeclaration(new Name("example.hoge")));

                            cu.getTypes().forEach(type -> {
                                type.setName("PongeController");

                                type.getMembers().stream()
                                        .filter(MethodDeclaration.class::isInstance)
                                        .map(MethodDeclaration.class::cast)
                                        .filter(method -> method.getModifiers()
                                                .stream()
                                                .anyMatch(modifier -> modifier.equals(Modifier.publicModifier())))
                                        .forEach(method -> method.getBody().ifPresent(body -> {
                                            new DaoVisitor<>();
                                        }));
                            });
                })))
                .writing("Sql", g ->
                        g.task(new SqlTask("selectById.sql", "src/main/resources/example/hoge/Orders/selectById.sql", stmt -> {
                            PlainSelect plainSelect = ((Select) stmt).getPlainSelect();
                            plainSelect.setFromItem(new Table("orders"));
                        }))
                )
                .writing("Maven", g -> {
                    g.task(new CopyTask("pom-test1.xml", "pom.xml"));
                    g.task(new PomTask()
                            .addDependency("net.unit8.moshas", "moshas", "0.3.0"));
                });

        crudApplication.invoke();
    }

    @Test
    public void generatorChaining() {
        Generator generator = new Generator();
        Generator result = generator.writing("Test", g -> {});
        assertThat(result).isSameAs(generator);
    }

    @Test
    public void taskExecutionInOrder() {
        Generator generator = new Generator();
        List<String> executionOrder = new ArrayList<>();

        generator.task(new GenTask() {
            @Override
            public void execute(PathResolver resolver) {
                executionOrder.add("task1");
            }

            @Override
            public String getDestinationPath() {
                return "path1";
            }
        });

        generator.task(new GenTask() {
            @Override
            public void execute(PathResolver resolver) {
                executionOrder.add("task2");
            }

            @Override
            public String getDestinationPath() {
                return "path2";
            }
        });

        generator.invoke();
        assertThat(executionOrder).containsExactly("task1", "task2");
    }

    @Test
    public void taskListenerNotification() {
        Generator generator = new Generator();
        List<String> events = new ArrayList<>();

        TaskListener listener = new TaskListener() {
            @Override
            public void beforeTask(GenTask task) {
                events.add("before:" + task.getDestinationPath());
            }

            @Override
            public void afterTask(GenTask task) {
                events.add("after:" + task.getDestinationPath());
            }
        };

        generator.addTaskListener(listener);

        generator.task(new GenTask() {
            @Override
            public void execute(PathResolver resolver) {}

            @Override
            public String getDestinationPath() {
                return "task1";
            }
        });

        generator.invoke();
        assertThat(events).containsExactly("before:task1", "after:task1");
    }

    @Test
    public void taskExecutionContinuesAfterException() {
        Generator generator = new Generator();
        List<String> executed = new ArrayList<>();

        generator.task(new GenTask() {
            @Override
            public void execute(PathResolver resolver) {
                throw new RuntimeException("Task failed");
            }

            @Override
            public String getDestinationPath() {
                return "failed-task";
            }
        });

        generator.task(new GenTask() {
            @Override
            public void execute(PathResolver resolver) {
                executed.add("success-task");
            }

            @Override
            public String getDestinationPath() {
                return "success-task";
            }
        });

        generator.invoke();
        assertThat(executed).containsExactly("success-task");
    }

    @Test
    public void pathResolverPassedToTasks() {
        PathResolver resolver = new PathResolverImpl(null, "test", "target/test");
        Generator generator = new Generator();

        AtomicReference<PathResolver> passedResolver = new AtomicReference<>();
        generator.setPathResolver(resolver);

        generator.task(new GenTask() {
            @Override
            public void execute(PathResolver resolver) {
                passedResolver.set(resolver);
            }

            @Override
            public String getDestinationPath() {
                return "test-path";
            }
        });

        generator.invoke();
        assertThat(passedResolver.get()).isSameAs(resolver);
    }
}
