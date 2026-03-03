package net.unit8.amagicman.listener;

import net.unit8.amagicman.GenTask;

/**
 * Listener interface for observing task execution events fired by {@link net.unit8.amagicman.Generator}.
 *
 * <p>Register an implementation via
 * {@link net.unit8.amagicman.Generator#addTaskListener(TaskListener)} to inject
 * cross-cutting concerns such as logging, progress reporting, or validation
 * around each task execution.</p>
 *
 * @author kawasima
 * @see net.unit8.amagicman.Generator#addTaskListener(TaskListener)
 */
public interface TaskListener {

    /**
     * Called immediately before a task is executed.
     *
     * @param task the task about to be executed
     */
    void beforeTask(GenTask task);

    /**
     * Called immediately after a task completes successfully.
     *
     * <p>This method is <em>not</em> called when the task throws an exception.</p>
     *
     * @param task the task that has just completed
     */
    void afterTask(GenTask task);
}
