package net.unit8.amagicman;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author kawasima
 */
public class Generator {
    private static final Logger LOG = LoggerFactory.getLogger(Generator.class);
    private List<MoldTask> tasks = new ArrayList<>();
    private PathResolver pathResolver;

    public void task(MoldTask task) {
        tasks.add(task);
    }

    public Generator writing(String section, Consumer<Generator> writing) {
        writing.accept(this);
        return this;
    }

    public Generator setPathResolver(PathResolver pathResolver) {
        this.pathResolver = pathResolver;
        return this;
    }

    public void invoke() {
        for (MoldTask task : tasks) {
            try {
                task.execute(pathResolver);
                LOG.info("create {}", task.getDestinationPath());
            } catch (Exception ex) {
                LOG.error("", ex);
            }
        }
    }
}
