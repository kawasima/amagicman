package net.unit8.amagicman.task;

import net.unit8.amagicman.MoldTask;
import net.unit8.amagicman.PathResolver;

/**
 * @author kawasima
 */
public class TableAndEntityTask implements MoldTask {
    private String createTable;
    private String destination;

    @Override
    public void execute(PathResolver pathResolver) throws Exception {
    }

    public String getDestinationPath() {
        return destination;
    }

}
