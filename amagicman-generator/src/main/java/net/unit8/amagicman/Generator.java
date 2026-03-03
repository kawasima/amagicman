package net.unit8.amagicman;

import net.unit8.amagicman.listener.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Orchestrates the execution of a sequence of {@link GenTask} instances.
 *
 * <p>Tasks are registered via {@link #task(GenTask)} or grouped into named sections
 * via {@link #writing(String, Consumer)}. Calling {@link #invoke()} runs all
 * registered tasks in registration order. Errors in individual tasks are logged
 * and execution continues with the next task.</p>
 *
 * <pre>{@code
 * new Generator()
 *     .setPathResolver(new PathResolverImpl(...))
 *     .writing("controllers", g -> {
 *         g.task(new CopyTask("template/Controller.java", "src/main/java/MyController.java"));
 *     })
 *     .invoke();
 * }</pre>
 *
 * @author kawasima
 */
public class Generator {
    private static final Logger LOG = LoggerFactory.getLogger(Generator.class);

    /** Task listeners notified before and after each task execution. */
    private final List<TaskListener> listeners = new ArrayList<>();

    /** Ordered list of tasks to be executed on {@link #invoke()}. */
    private final List<GenTask> tasks = new ArrayList<>();

    /** Resolver used to locate templates and write output files. */
    private PathResolver pathResolver;

    /**
     * Registers a task to be executed during {@link #invoke()}.
     *
     * @param task the task to add
     */
    public void task(GenTask task) {
        tasks.add(task);
    }

    /**
     * Groups a set of task registrations under a named section.
     *
     * <p>The section name is logged at DEBUG level; no other behavioural difference
     * exists compared to calling {@link #task(GenTask)} directly.</p>
     *
     * @param section  descriptive name for the group of tasks
     * @param writing  consumer that registers tasks on this generator
     * @return this generator instance for method chaining
     */
    public Generator writing(String section, Consumer<Generator> writing) {
        LOG.debug("writing {}", section);
        writing.accept(this);
        return this;
    }

    /**
     * Sets the path resolver used by all tasks to locate templates and output files.
     *
     * @param pathResolver the path resolver to use
     * @return this generator instance for method chaining
     */
    public Generator setPathResolver(PathResolver pathResolver) {
        this.pathResolver = pathResolver;
        return this;
    }

    /**
     * Adds a task listener that receives notifications before and after each task.
     *
     * @param listener the listener to register
     * @return this generator instance for method chaining
     */
    public Generator addTaskListener(TaskListener listener) {
        listeners.add(listener);
        return this;
    }

    /**
     * Executes all registered tasks in the order they were added.
     *
     * <p>Each task execution is bracketed by
     * {@link TaskListener#beforeTask(GenTask)} and
     * {@link TaskListener#afterTask(GenTask)} calls on all registered listeners.
     * If a task throws an exception, the error is logged and execution continues
     * with the next task.</p>
     */
    public void invoke() {
        for (GenTask task : tasks) {
            try {
                listeners.forEach(l -> l.beforeTask(task));
                task.execute(pathResolver);
                listeners.forEach(l -> l.afterTask(task));
                LOG.info("create {}", task.getDestinationPath());
            } catch (Exception ex) {
                LOG.error("Failed to execute task: {}", task.getDestinationPath(), ex);
            }
        }
    }
}
