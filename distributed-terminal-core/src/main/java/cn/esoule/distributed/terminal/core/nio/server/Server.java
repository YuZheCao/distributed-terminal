package cn.esoule.distributed.terminal.core.nio.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 *
 * @author yuzhecao@foxmail.com
 */
public class Server {

    private String cluster;
    private String name;
    private String ip;
    private short port;

    public Server(String cluster, String ip, short port) {
        this.cluster = cluster;
        this.name = String.format("%s-%s-%s", cluster, ip, port);
        this.ip = ip;
        this.port = port;
    }

    public String getCluster() {
        return this.cluster;
    }

    public String getName() {
        return this.name;
    }

    public String getIp() {
        return this.ip;
    }

    public short getPort() {
        return this.port;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this, SerializerFeature.DisableCircularReferenceDetect);
    }
}
