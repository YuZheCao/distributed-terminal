/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.esoule.distributed.terminal.core.nio.server;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author caoxin1
 */
public abstract class Dispatcher extends Thread {
    
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

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
                synchronized (gate) {
                }
            } catch (IOException e) {
                logger.error("Dispatcher error! " + e.getMessage());
            }
        }
    }

    public final Selector selector() {
        return this.selector;
    }
}
