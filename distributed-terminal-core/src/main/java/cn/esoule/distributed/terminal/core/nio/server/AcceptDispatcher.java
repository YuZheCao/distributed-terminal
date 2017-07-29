/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.esoule.distributed.terminal.core.nio.server;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 *
 * @author caoxin1
 */
public class AcceptDispatcher extends Dispatcher {

    private int readBufferSize;
    private int writeBufferSize;
    private boolean block;
    private NioServer nioServer;

    public AcceptDispatcher(String name, int readBufferSize, int writeBufferSize, boolean block, NioServer nioServer) throws IOException {
        super(name);
        this.readBufferSize = readBufferSize;
        this.writeBufferSize = writeBufferSize;
        this.block = block;
        this.nioServer = nioServer;
    }

    public final void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(block);
        IODispatcher ioDispatcher = nioServer.getIODispatcher();
        Connection connection = new Connection(socketChannel, ioDispatcher, readBufferSize, writeBufferSize);
        ioDispatcher.register(socketChannel, SelectionKey.OP_READ, connection);
    }

    @Override
    protected void dispatch() throws IOException {
        if (selector.select() <= 0) {
            return;
        }
        Iterator<SelectionKey> selectedKeys = this.selector.selectedKeys().iterator();
        while (selectedKeys.hasNext()) {
            SelectionKey key = selectedKeys.next();
            selectedKeys.remove();
            if (key.isValid()) {
                this.accept(key);
            }
        }
    }

    public final SelectionKey register(SelectableChannel ch, int ops) throws IOException {
        synchronized (gate) {
            selector.wakeup();
            return ch.register(selector, ops);
        }
    }
}
