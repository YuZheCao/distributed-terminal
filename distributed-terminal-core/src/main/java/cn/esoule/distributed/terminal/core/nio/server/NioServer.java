/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.esoule.distributed.terminal.core.nio.server;

import cn.esoule.distributed.terminal.core.nio.constant.Configure;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author caoxin1
 */
public class NioServer {

    private static final Logger logger = LoggerFactory.getLogger(NioServer.class);
    private Server[] servers;
    private boolean block;
    private int ioThreadNum;
    private int readBufferSize;
    private int writeBufferSize;
    private AcceptDispatcher acceptDispatcher;
    private List<SelectionKey> serverChannelKeys = new ArrayList<SelectionKey>();
    private List<IODispatcher> ioDispatcheres = new ArrayList<IODispatcher>();
    private ScheduledExecutorService disconnectionThreadPool = Executors.newScheduledThreadPool(1);
    private ExecutorService workersThreadPool =  Executors.newFixedThreadPool(10);

    public NioServer() {
        String[] addresses = Configure.DEFAULT_ADDRESSES.split(Configure.ADDRESSES_DELIMITER);
        int len = addresses.length;
        servers = new Server[len];
        for (int index = 0; index < len; index++) {
            String address = addresses[index];
            servers[index] = new Server(Configure.DEFAULT_CLUSTER_NAME, address.split(Configure.ADDRESS_DELIMITER)[0], Short.valueOf(address.split(Configure.ADDRESS_DELIMITER)[1]));
        }
        block = Configure.DEFAULT_BLOCK;
        ioThreadNum = Configure.DEFAULT_IO_THREAD_NUM;
        readBufferSize = Configure.DEFAULT_READ_BUFFER_SIZE;
        writeBufferSize = Configure.DEFAULT_WRITE_BUFFER_SIZE;
    }

    public void start() throws Throwable {
        logger.info("连接调度器启动!");
        acceptDispatcher = new AcceptDispatcher("Accept-Dispatcher-Thread", readBufferSize, writeBufferSize, block, this);
        acceptDispatcher.start();
        logger.info("IO调度器启动!");
        for (int index = 1; index <= ioThreadNum; index++) {
            IODispatcher ioDispatcher = new IODispatcher("IO-Dispatcher-Thread-" + index, disconnectionThreadPool, workersThreadPool);
            ioDispatcheres.add(ioDispatcher);
            ioDispatcher.start();
        }
        for (Server server : servers) {
            logger.info("监听地址 : {}", server.toString());
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(block);
            InetSocketAddress isa;
            if ("*".equals(server.getIp())) {
                isa = new InetSocketAddress(server.getPort());
            } else {
                isa = new InetSocketAddress(server.getIp(), server.getPort());
            }
            serverChannel.socket().bind(isa);
            SelectionKey acceptKey = acceptDispatcher.register(serverChannel, SelectionKey.OP_ACCEPT);
            serverChannelKeys.add(acceptKey);
        }

    }

    private int currentIODispatcherIndex = 1;

    public IODispatcher getIODispatcher() {
        return ioDispatcheres.get(Math.abs((currentIODispatcherIndex++) % ioThreadNum));
    }

    public static void main(String... args) throws Throwable {
        NioServer nioServer = new NioServer();
        nioServer.start();
    }
}
