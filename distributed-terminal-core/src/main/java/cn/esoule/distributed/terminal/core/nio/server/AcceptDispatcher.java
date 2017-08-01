package cn.esoule.distributed.terminal.core.nio.server;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 *
 * @author yuzhecao@foxmail.com
 */
public class AcceptDispatcher extends Dispatcher {

    private NioServer nioServer;
    private Configure configure;

    public AcceptDispatcher(String name, Configure configure, NioServer nioServer) throws IOException {
        super(name);
        this.configure = configure;
        this.nioServer = nioServer;
    }

    public final void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(configure.isBlock());
        IODispatcher ioDispatcher = nioServer.getIODispatcher();
        Session session = new Session(socketChannel, ioDispatcher, configure);
        ioDispatcher.register(socketChannel, SelectionKey.OP_READ, session);
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
