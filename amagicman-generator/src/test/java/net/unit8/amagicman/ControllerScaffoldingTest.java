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
                            PlainSelect plainSelect = (PlainSelect) ((Select) stmt).getSelectBody();
                            plainSelect.setFromItem(new Table("orders"));
                        }))
                )
                .writing("Maven", g -> {
                    g.task(new CopyTask("pom-test1.xml", "pom.xml"));
                    g.task(new PomTask()
                            .addDependency("net.unit8.moshas", "moshas", "0.1.0"));
                });

        crudApplication.invoke();
    }
}
