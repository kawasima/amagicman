package net.unit8.amagicman.listener;

import net.unit8.amagicman.GenTask;

/**
 * @author kawasima
 */
public interface TaskListener {
    void beforeTask(GenTask task);
    void afterTask(GenTask task);
}
