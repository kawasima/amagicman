package net.unit8.amagicman;

import net.unit8.amagicman.listener.TaskListener;
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

    private final List<TaskListener> listeners = new ArrayList<>();
    private final List<GenTask> tasks = new ArrayList<>();
    private PathResolver pathResolver;

    public void task(GenTask task) {
        tasks.add(task);
    }

    public Generator writing(String section, Consumer<Generator> writing) {
        LOG.debug("writing {}", section);
        writing.accept(this);
        return this;
    }

    public Generator setPathResolver(PathResolver pathResolver) {
        this.pathResolver = pathResolver;
        return this;
    }

    public Generator addTaskListener(TaskListener listener) {
        listeners.add(listener);
        return this;
    }

    public void invoke() {
        for (GenTask task : tasks) {
            try {
                listeners.forEach(l -> l.beforeTask(task));
                task.execute(pathResolver);
                listeners.forEach(l -> l.afterTask(task));
                LOG.info("create {}", task.getDestinationPath());
            } catch (Exception ex) {
                LOG.error("", ex);
            }
        }
    }
}
