package cn.esoule.distributed.terminal.core.nio.server;

import java.util.Properties;

/**
 *
 * @author yuzhecao@foxmail.com
 */
public class Configure {

    public static final String DEFAULT_CLUSTER_NAME = "distributed-terminal";
    public static final String DEFAULT_ADDRESSES = "0.0.0.0:8800";
    public static final String ADDRESSES_DELIMITER = ",";
    public static final String ADDRESS_DELIMITER = ":";
    public static final String DEFAULT_BLOCK = "false";
    public static final String DEFAULT_IO_THREAD_NUM = "4";
    public static final String DEFAULT_READ_BUFFER_SIZE = "8192";
    public static final String DEFAULT_WRITE_BUFFER_SIZE = "8192";

    private String clusterName;
    private String addresses;
    private boolean block;
    private int ioThreadNum;
    private int readBufferSize;
    private int writeBufferSize;

    private static Configure configure;
    private static Object lock = new Object();

    private Configure() {
    }

    public static Configure getInstance(Properties properties) {
        if (null == Configure.configure) {
            synchronized (lock) {
                if (null == Configure.configure) {
                    if (null == properties) {
                        properties = new Properties();
                    }
                    Configure configure = new Configure();
                    configure.clusterName = properties.getProperty("cluster.name", DEFAULT_CLUSTER_NAME);
                    configure.addresses = properties.getProperty("network.addresses", DEFAULT_ADDRESSES);
                    configure.block = Boolean.valueOf(properties.getProperty("network.block", DEFAULT_BLOCK));
                    configure.ioThreadNum = Integer.valueOf(properties.getProperty("network.io.thread.num", DEFAULT_IO_THREAD_NUM));
                    configure.readBufferSize = Integer.valueOf(properties.getProperty("network.io.read.buffer.size", DEFAULT_READ_BUFFER_SIZE));
                    configure.writeBufferSize = Integer.valueOf(properties.getProperty("network.io.write.buffer.size", DEFAULT_WRITE_BUFFER_SIZE));
                    Configure.configure = configure;
                }
            }
        }
        return Configure.configure;
    }

    public String getClusterName() {
        return clusterName;
    }

    public String getAddresses() {
        return addresses;
    }

    public boolean isBlock() {
        return block;
    }

    public int getIoThreadNum() {
        return ioThreadNum;
    }

    public int getReadBufferSize() {
        return readBufferSize;
    }

    public int getWriteBufferSize() {
        return writeBufferSize;
    }

    public static Object getLock() {
        return lock;
    }

}
