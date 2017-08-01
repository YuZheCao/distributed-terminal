package cn.esoule.distributed.terminal.core.nio.server;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author yuzhecao@foxmail.com
 */
public abstract class Dispatcher extends Thread {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected final Object gate = new Object();
    protected final Selector selector;

    public Dispatcher(String name) throws IOException {
        super(name);
        this.selector = SelectorProvider.provider().openSelector();
    }

    abstract protected void dispatch() throws IOException;

    @Override
    public void run() { // 经典服务器死循环
        while (true) {
            try {
                dispatch();
            } catch (IOException e) {
                logger.error("{} 调度出错! {}", getClass().getName(), e.getMessage());
            }
        }
    }

    public final Selector selector() {
        return this.selector;
    }
}
