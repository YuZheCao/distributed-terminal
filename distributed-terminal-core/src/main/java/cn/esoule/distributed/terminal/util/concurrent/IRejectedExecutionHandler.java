package cn.esoule.distributed.terminal.util.concurrent;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  A handler for tasks that cannot be executed by a {@link ThreadPoolExecutor}
 * 
 * @author
 */
public final class IRejectedExecutionHandler implements RejectedExecutionHandler {

    private static final Logger logger = LoggerFactory.getLogger(IRejectedExecutionHandler.class);

    /**
     * 当 executor 不能接受某个任务时，可以由 ThreadPoolExecutor 调用的方法
     * 
     * @param r
     * @param executor 
     */
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (executor != null && !executor.isShutdown()) {
            executor.execute(r);
            return;
        }
        logger.warn(r + " from " + executor, new RejectedExecutionException());
        if (Thread.currentThread().getPriority() > Thread.NORM_PRIORITY) {
            new Thread(r).start(); // start()可以协调系统的资源
        } else {
            r.run();
        }
    }
}