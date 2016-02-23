package net.unit8.amagicman;

/**
 * @author kawasima
 */
public interface MoldTask {
    void execute(PathResolver pathResolver) throws Exception;
    String getDestinationPath();
}
