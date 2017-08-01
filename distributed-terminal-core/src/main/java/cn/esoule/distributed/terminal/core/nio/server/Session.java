package cn.esoule.distributed.terminal.core.nio.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author yuzhecao@foxmail.com
 */
public class Session {

    private static final Logger logger = LoggerFactory.getLogger(Session.class);
    protected boolean closed; // 关闭套接字管道
    protected final Object guard = new Object();
    private IODispatcher ioDispatcher;
    private final String ip;
    protected boolean isForcedClosing;
    private SelectionKey key;
    private boolean locked = false;
    protected boolean pendingClose;
    public final ByteBuffer readBuffer;
    private final SocketChannel socketChannel;
    public final ByteBuffer writeBuffer;
    private Object object;

    public Session(SocketChannel socketChannel, IODispatcher ioDispatcher, Configure configure) {
        this.socketChannel = socketChannel;
        this.ioDispatcher = ioDispatcher;
        writeBuffer = ByteBuffer.allocate(configure.getWriteBufferSize());
        writeBuffer.flip();
        writeBuffer.order(ByteOrder.LITTLE_ENDIAN);
        readBuffer = ByteBuffer.allocate(configure.getReadBufferSize());
        readBuffer.order(ByteOrder.LITTLE_ENDIAN);
        this.ip = socketChannel.socket().getInetAddress().getHostAddress();
    }

    /**
     * 关闭连接
     *
     * @param forced
     */
    public final void close(boolean forced) {
        synchronized (guard) {
            if (isWriteDisabled()) {
                return;
            }
            isForcedClosing = forced;
            getIODispatcher().closeConnection(this);
        }
    }

    /**
     * 关闭套接字管道
     */
    public final boolean closeSocketChannel() {
        synchronized (guard) {
            if (closed) {
                return false;
            }
            try {
                if (socketChannel.isOpen()) {
                    socketChannel.close();
                    key.attach(null);
                    key.cancel();
                }
                closed = true;
            } catch (IOException ignored) {
            }
        }
        return true;
    }

    protected final void enableWriteInterest() {
        if (key.isValid()) {
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
            key.selector().wakeup();
        }
    }

    public long getDisconnectionDelay() {
        return 1;
    }

    private IODispatcher getIODispatcher() {
        return ioDispatcher;
    }

    public final String getIP() {
        return ip;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    protected final boolean isPendingClose() {
        return pendingClose && !closed;
    }

    protected final boolean isWriteDisabled() {
        return pendingClose || closed;
    }

    public void onDisconnect() {

    }

    public void onServerClose() {

    }

    public boolean processData(ByteBuffer data) {
        logger.info(new String(data.array()));
        return true;
    }

    public final void setKey(SelectionKey key) {
        this.key = key;
    }

    public boolean tryLockConnection() {
        if (locked) {
            return false;
        }
        return locked = true;
    }

    public void unlockConnection() {
        locked = false;
    }

    public boolean writeData(ByteBuffer data) {
        return true;
    }

    public <T extends Object> void setObject(T object) {
        this.object = object;
    }

    public <T extends Object> T getObject() {
        return (T) this.object;
    }
}
