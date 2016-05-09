package net.unit8.amagicman;

/**
 * @author kawasima
 */
@FunctionalInterface
public interface GenTask {
    void execute(PathResolver pathResolver) throws Exception;
    default String getDestinationPath() {
        return null;
    }
}
