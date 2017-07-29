/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.esoule.distributed.terminal.core.nio.constant;

/**
 *
 * @author caoxin1
 */
public class Configure {

    public static final String DEFAULT_CLUSTER_NAME = "distributed-terminal";
    public static final String DEFAULT_ADDRESSES = "0.0.0.0:8800";
    public static final String ADDRESSES_DELIMITER = ",";
    public static final String ADDRESS_DELIMITER = ":";
    public static final boolean DEFAULT_BLOCK = false;
    public static final int DEFAULT_IO_THREAD_NUM = 4;
    public static final int DEFAULT_READ_BUFFER_SIZE = 8192 * 2;
    public static final int DEFAULT_WRITE_BUFFER_SIZE = 8192 * 2;

}
