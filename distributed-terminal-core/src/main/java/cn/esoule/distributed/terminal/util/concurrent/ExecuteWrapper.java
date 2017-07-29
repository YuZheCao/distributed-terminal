package cn.esoule.distributed.terminal.util.concurrent;

import cn.esoule.distributed.terminal.util.tool.RunnableStatsManager;
import java.util.concurrent.TimeUnit;
import javolution.text.TextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 当线程执行花费时间过长时，进行通知（使用RunnableStatsManager进行记录）
 * 
 * @author caoxin
 */
public class ExecuteWrapper implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ExecuteWrapper.class);
    private final Runnable runnable;

    public ExecuteWrapper(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public final void run() {
        ExecuteWrapper.execute(runnable, getMaximumRuntimeInMillisecWithoutWarning());
    }

    protected long getMaximumRuntimeInMillisecWithoutWarning() {
        return Long.MAX_VALUE;
    }

    public static void execute(Runnable runnable) {
        execute(runnable, Long.MAX_VALUE);
    }

    public static void execute(Runnable runnable, long maximumRuntimeInMillisecWithoutWarning) {
        long begin = System.nanoTime();
        try {
            runnable.run();
        } catch (RuntimeException e) {
            logger.warn("Exception in a Runnable execution:", e);
        } finally {
            long runtimeInNanosec = System.nanoTime() - begin;
            Class<? extends Runnable> clazz = runnable.getClass();
            RunnableStatsManager.handleStats(clazz, runtimeInNanosec);
            long runtimeInMillisec = TimeUnit.NANOSECONDS.toMillis(runtimeInNanosec);
            if (runtimeInMillisec > maximumRuntimeInMillisecWithoutWarning) {
                TextBuilder tb = TextBuilder.newInstance();
                tb.append(clazz);
                tb.append(" - execution time: ");
                tb.append(runtimeInMillisec);
                tb.append("msec");
                logger.warn(tb.toString());
                TextBuilder.recycle(tb);
            }
        }
    }
    
    private static class TestRunabled implements Runnable {
    
        private final int sleepTime = 30 * 1000;

        @Override
        public void run() {
            try {
                Thread.sleep(sleepTime);
                logger.info("hello world!");
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage());
            }
        }
    }
    
    public static void main(String ...args) {
        ExecuteWrapper executeWrapper = new ExecuteWrapper(new TestRunabled());
        new Thread(executeWrapper, "executeWrapper").start();
    }
}