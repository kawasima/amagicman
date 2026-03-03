package net.unit8.amagicman;

/**
 * Interface representing a single code or resource generation task.
 *
 * <p>Each task implements {@link #execute(PathResolver)} to perform file generation
 * or transformation. As a {@code @FunctionalInterface}, it can be expressed as a lambda.</p>
 *
 * <pre>{@code
 * generator.task(pathResolver -> {
 *     // custom generation logic
 * });
 * }</pre>
 *
 * @author kawasima
 * @see Generator
 * @see PathResolver
 */
@FunctionalInterface
public interface GenTask {

    /**
     * Executes the task.
     *
     * @param pathResolver resolver for locating template inputs and destination outputs
     * @throws Exception any exception thrown during task execution
     */
    void execute(PathResolver pathResolver) throws Exception;

    /**
     * Returns the destination path of the file produced by this task.
     *
     * <p>Used for logging and task identification. Defaults to {@code null}.</p>
     *
     * @return the destination path string, or {@code null} if not defined
     */
    default String getDestinationPath() {
        return null;
    }
}
