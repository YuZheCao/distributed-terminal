///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package cn.esoule.distributed.terminal.core.nio.client;
//
//import cn.esoule.distributed.terminal.core.nio.constant.Configure;
//import java.io.IOException;
//import java.net.InetSocketAddress;
//import java.net.SocketAddress;
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//import java.nio.channels.SelectableChannel;
//import java.nio.channels.SelectionKey;
//import java.nio.channels.Selector;
//import java.nio.channels.SocketChannel;
//import java.nio.channels.spi.SelectorProvider;
//import java.util.Iterator;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// *
// * @author caoxin1
// */
//public class Cmd {
//
//    private static final Logger logger = LoggerFactory.getLogger(Cmd.class);
//
//    public static void main(String... args) throws IOException {
//        SocketAddress sa = new InetSocketAddress("127.0.0.1", 8800);
//        SocketChannel socketChannel = SocketChannel.open(sa);
//    }
//
//    public static class IOWorker extends Thread {
//
//        private Selector selector;
//
//        public IOWorker() throws IOException {
//            this.selector = SelectorProvider.provider().openSelector();
//        }
//
//        @Override
//        public void run() {
//            try {
//                if (selector.select() <= 0) {
//                    return;
//                }
//                Iterator<SelectionKey> selectedKeys = this.selector.selectedKeys().iterator();
//                while (selectedKeys.hasNext()) {
//                    SelectionKey key = selectedKeys.next();
//                    selectedKeys.remove();
//                    if (!key.isValid()) {
//                        continue;
//                    }
//                    switch (key.readyOps()) {
//                        case SelectionKey.OP_READ:
//                            this.read(key);
//                            break;
//                        case SelectionKey.OP_WRITE:
//                            this.write(key);
//                            break;
//                        case SelectionKey.OP_READ | SelectionKey.OP_WRITE:
//                            this.read(key);
//                            if (key.isValid()) {
//                                this.write(key);
//                            }
//                            break;
//                    }
//                }
//            } catch (Throwable e) {
//                logger.error(e.getMessage());
//            }
//        }
//
//        public final void register(SelectableChannel ch, int ops, Connection att) throws IOException {
//            synchronized (this) {
//                selector.wakeup();
//                ch.register(selector, ops, att);
//            }
//        }
//
//        public void read(SelectionKey key) throws IOException {
//            SocketChannel socketChannel = (SocketChannel) key.channel();
//            Connection con = (Connection) key.attachment();
//            ByteBuffer rb = con.getReadBuffer();
//            int numRead;
//            try {
//                numRead = socketChannel.read(rb);
//            } catch (IOException e) {
//                socketChannel.close();
//                return;
//            }
//            if (numRead == -1) {
//                socketChannel.close();
//                return;
//            } else if (numRead == 0) {
//                return;
//            }
//            rb.flip();
//            rb.mark();
//            while (rb.remaining() > 2 && rb.remaining() >= rb.getShort(rb.position())) { // 读取是否为一个整包（也可能大于一个整包（多个包），因此这里会使用循环）
//                if (!parse(con, rb)) { // 判断包是否合法
//                    socketChannel.close();
//                    return;
//                }
//            }
//            if (rb.hasRemaining()) {
//                rb.compact(); // 将缓冲区的当前位置和界限之间的字节复制到缓冲区的开始处（为下一个包准备）
//            } else {
//                rb.clear();
//            }
//        }
//
//       private void write(SelectionKey key) {
//        SocketChannel socketChannel = (SocketChannel) key.channel();
//        Connection con = (Connection) key.attachment();
//        int numWrite;
//        ByteBuffer wb = con.getWriteBuffer();
//        if (wb.hasRemaining()) {
//            try {
//                numWrite = socketChannel.write(wb);
//            } catch (IOException e) {
//                return;
//            }
//            if (numWrite == 0) {
//                return;
//            }
//            if (wb.hasRemaining()) { // 不能被写的数据
//                return;
//            }
//        }
//        while (true) {
//            wb.clear();
//            boolean writeFailed = !con.writeData(wb);
//            if (writeFailed) {
//                wb.limit(0);
//                break;
//            }
//            try {
//                numWrite = socketChannel.write(wb);
//            } catch (IOException e) {
//                socketChannel.close();
//                return;
//            }
//            if (numWrite == 0) {
//                logger.info("Write " + numWrite + " ip: " + con.getIP());
//                return;
//            }
//            if (wb.hasRemaining()) { // 不能被写的数据
//                return;
//            }
//        }
//        key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
//        if (con.isPendingClose()) {
//            closeConnectionImpl(con);
//        }
//    }
//
//        private boolean parse(Connection con, ByteBuffer buf) {
//            short sz = 0;
//            try {
//                buf.reset();
//                sz = buf.getShort();
//                if (sz > 1) {
//                    sz -= 2;
//                }
//                ByteBuffer b = (ByteBuffer) buf.slice().limit(sz); // 创建新的缓冲区
//                b.order(ByteOrder.LITTLE_ENDIAN); // 小端模式，高字节存储在高地址
//                buf.position(buf.position() + sz); // 写一个包数据开始处
//                return handler(b);
//            } catch (IllegalArgumentException e) {
//                logger.warn("Error on parsing input from client - account: " + con + " packet size: " + sz + " real size:" + buf.remaining() + e.getMessage());
//                return false;
//            }
//        }
//
//        public boolean handler(ByteBuffer b) {
//            logger.info(new String(b.array()));
//            return true;
//        }
//    }
//
//    public static class Connection {
//
//        private ByteBuffer writeBuffer;
//        private ByteBuffer readBuffer; 
//
//        public Connection() {
//            writeBuffer = ByteBuffer.allocate(Configure.DEFAULT_WRITE_BUFFER_SIZE);
//            writeBuffer.flip();
//            writeBuffer.order(ByteOrder.LITTLE_ENDIAN);
//            readBuffer = ByteBuffer.allocate(Configure.DEFAULT_READ_BUFFER_SIZE);
//            readBuffer.order(ByteOrder.LITTLE_ENDIAN);
//        }
//
//        public ByteBuffer getWriteBuffer() {
//            return this.writeBuffer;
//        }
//
//        public ByteBuffer getReadBuffer() {
//            return this.readBuffer;
//        }
//
//        public boolean invoke (String content) {
//            ByteBuffer bb = ByteBuffer.allocate(content.length() + 2);
//            bb.putShort((short)content.length());
//            bb.put(content.getBytes());
//            bb.flip();
//            
//        }
//
//        public boolean request (ByteBuffer wb) {
//            
//        }
//    }
//}
