package cn.esoule.distributed.terminal.core.nio.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author yuzhecao@foxmail.com
 */
public class IODispatcher extends Dispatcher {

    private final ScheduledExecutorService dcPool;
    private final ExecutorService workersThreadPool;
    private final List<Session> pendingClose = new ArrayList<Session>();

    public IODispatcher(String name, ScheduledExecutorService dcPool, ExecutorService workersThreadPool) throws IOException {
        super(name);
        this.dcPool = dcPool;
        this.workersThreadPool = workersThreadPool;
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
            if (!key.isValid()) {
                continue;
            }
            switch (key.readyOps()) {
                case SelectionKey.OP_READ:
                    this.read(key);
                    break;
                case SelectionKey.OP_WRITE:
                    this.write(key);
                    break;
                case SelectionKey.OP_READ | SelectionKey.OP_WRITE:
                    this.read(key);
                    if (key.isValid()) {
                        this.write(key);
                    }
                    break;
            }
        }
    }

    /**
     * 注册套接字管道的IO监听事件
     *
     * @param ch
     * @param ops
     * @param att
     * @throws IOException
     */
    public final void register(SelectableChannel ch, int ops, Session att)
            throws IOException {
        synchronized (gate) {
            selector.wakeup();
            ch.register(selector, ops, att);
        }
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        Session session = (Session) key.attachment();
        ByteBuffer rb = session.readBuffer;
        int numRead;
        try {
            numRead = socketChannel.read(rb);
        } catch (IOException e) {
            socketChannel.close();
            return;
        }
        if (numRead == -1) {
            socketChannel.close();
            return;
        } else if (numRead == 0) {
            return;
        }
        rb.flip();
        rb.mark();
        while (rb.remaining() > 2 && rb.remaining() >= rb.getShort(rb.position())) { // 读取是否为一个整包（也可能大于一个整包（多个包），因此这里会使用循环）
            if (!parse(session, rb)) { // 判断包是否合法
                socketChannel.close();
                return;
            }
        }
        if (rb.hasRemaining()) {
            session.readBuffer.compact(); // 将缓冲区的当前位置和界限之间的字节复制到缓冲区的开始处（为下一个包准备）
        } else {
            rb.clear();
        }
    }

    private boolean parse(Session session, ByteBuffer buf) {
        short sz = 0;
        try {
            buf.reset();
            sz = buf.getShort();
            if (sz > 1) {
                sz -= 2;
            }
            ByteBuffer b = (ByteBuffer) buf.slice().limit(sz); // 创建新的缓冲区
            b.order(ByteOrder.LITTLE_ENDIAN); // 小端模式，高字节存储在高地址
            buf.position(buf.position() + sz); // 写一个包数据开始处
            return session.processData(b);
        } catch (IllegalArgumentException e) {
            logger.warn("Error on parsing input from client - account: " + session + " packet size: " + sz + " real size:" + buf.remaining() + e.getMessage());
            return false;
        }
    }

    private void write(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        Session session = (Session) key.attachment();
        int numWrite;
        ByteBuffer wb = session.writeBuffer;
        if (wb.hasRemaining()) {
            try {
                numWrite = socketChannel.write(wb);
            } catch (IOException e) {
                closeConnectionImpl(session);
                return;
            }
            if (numWrite == 0) {
                logger.info("Write " + numWrite + " ip: " + session.getIP());
                return;
            }
            if (wb.hasRemaining()) { // 不能被写的数据
                return;
            }
        }
        while (true) {
            wb.clear();
            boolean writeFailed = !session.writeData(wb);
            if (writeFailed) {
                wb.limit(0);
                break;
            }
            try {
                numWrite = socketChannel.write(wb);
            } catch (IOException e) {
                closeConnectionImpl(session);
                return;
            }
            if (numWrite == 0) {
                logger.info("Write " + numWrite + " ip: " + session.getIP());
                return;
            }
            if (wb.hasRemaining()) { // 不能被写的数据
                return;
            }
        }
        key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
        if (session.isPendingClose()) {
            closeConnectionImpl(session);
        }
    }

    /**
     * 关闭连接
     *
     * @param con
     */
    public void closeConnection(Session con) {
        synchronized (pendingClose) {
            pendingClose.add(con);
        }
    }

    private void closeConnectionImpl(Session con) {
        if (con.closeSocketChannel()) {
            dcPool.schedule(new DisconnectionTask(con), con.getDisconnectionDelay(), TimeUnit.MICROSECONDS);
        }
    }

}
