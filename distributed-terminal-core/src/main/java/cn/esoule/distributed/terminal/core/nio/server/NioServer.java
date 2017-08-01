package cn.esoule.distributed.terminal.core.nio.server;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author yuzhecao@foxmail.com
 */
public class NioServer {

    private static final Logger logger = LoggerFactory.getLogger(NioServer.class);
    private Server[] servers;
    private AcceptDispatcher acceptDispatcher;
    private List<SelectionKey> serverChannelKeys = new ArrayList<SelectionKey>();
    private List<IODispatcher> ioDispatcheres = new ArrayList<IODispatcher>();
    private ScheduledExecutorService disconnectionThreadPool = Executors.newScheduledThreadPool(1);
    private ExecutorService workersThreadPool =  Executors.newFixedThreadPool(10);
    private static NioServer nioServer;
    private static Object lock = new Object();
    private Configure configure;

    public static NioServer getInstance (Properties properties) {
          if (null == nioServer) {
            synchronized (lock) {
                if (null == nioServer) {
                    NioServer nioServer = new NioServer();
                    nioServer.configure = Configure.getInstance(properties);
                    String[] addresses = nioServer.configure.getAddresses().split(Configure.ADDRESSES_DELIMITER);
                    int len = addresses.length;
                    nioServer.servers = new Server[len];
                    for (int index = 0; index < len; index++) {
                        String address = addresses[index];
                        nioServer.servers[index] = new Server(Configure.DEFAULT_CLUSTER_NAME, address.split(Configure.ADDRESS_DELIMITER)[0], Short.valueOf(address.split(Configure.ADDRESS_DELIMITER)[1]));
                    }
                    NioServer.nioServer = nioServer;
                }
            }
         }
          return NioServer.nioServer;
    }

    private NioServer() {}

    public void start() throws Throwable {
        logger.info("连接调度器启动!");
        acceptDispatcher = new AcceptDispatcher("Accept-Dispatcher-Thread", configure, this);
        acceptDispatcher.start();
        logger.info("IO调度器启动!");
        for (int index = 1; index <= configure.getIoThreadNum(); index++) {
            IODispatcher ioDispatcher = new IODispatcher("IO-Dispatcher-Thread-" + index, disconnectionThreadPool, workersThreadPool);
            ioDispatcheres.add(ioDispatcher);
            ioDispatcher.start();
        }
        for (Server server : servers) {
            logger.info("监听地址 : {}", server.toString());
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(configure.isBlock());
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
        return ioDispatcheres.get(Math.abs((currentIODispatcherIndex++) % configure.getIoThreadNum()));
    }
}
