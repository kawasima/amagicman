package net.unit8.amagicman;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ModifierSet;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import net.unit8.amagicman.task.CopyTask;
import net.unit8.amagicman.task.JavaByTemplateTask;
import net.unit8.amagicman.task.PomTask;
import org.junit.Test;

import java.io.IOException;

/**
 * @author kawasima
 */
public class ControllerScaffoldingTest {
    private static class DaoVisitor extends VoidVisitorAdapter {
        @Override
        public void visit(NameExpr expr, Object arg) {
            if (expr.getName().equals("customerDao")) {
                expr.setName("pongeDao");
            }
        }

        @Override
        public  void visit(ReferenceType expr, Object arg) {
            ClassOrInterfaceType type = (ClassOrInterfaceType) expr.getType();
            if (type.getName().equals("CustomerDao")) {
                expr.setType(new ReferenceType(new ClassOrInterfaceType("PongeDao")));
            } else if (type.getName().equals("Customer")) {
                expr.setType(new ReferenceType(new ClassOrInterfaceType("Ponge")));
            }

        }
    }

    @Test
    public void test() throws IOException, ParseException {
        PathResolver pathResolver = new PathResolver(null, "test1", "target/test1");
        Generator crudApplication = new Generator()
                .setPathResolver(pathResolver)
                .writing("Controller", g ->
                        g.task(new JavaByTemplateTask("ControllerTemplate.java", "src/main/java/example/hoge/TestController.java", cu -> {
                            cu.setPackage(new PackageDeclaration(ASTHelper.createNameExpr("example.hoge")));

                            cu.getTypes().forEach(type -> {
                                type.setName("PongeController");

                                type.getMembers().stream()
                                        .filter(MethodDeclaration.class::isInstance)
                                        .map(MethodDeclaration.class::cast)
                                        .filter(method -> (method.getModifiers() & ModifierSet.PUBLIC) == 1)
                                        .forEach(method -> {
                                            method.getBody().accept(new DaoVisitor(), null);
                                        });
                            });
                })))
                .writing("Maven", g -> {
                    g.task(new CopyTask("pom-test1.xml", "pom.xml"));
                    g.task(new PomTask()
                            .addDependency("net.unit8.moshas", "moshas", "0.1.0"));
                });

        crudApplication.invoke();
    }
}
