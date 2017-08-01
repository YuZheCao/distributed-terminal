/**
 * 欢迎浏览和修改代码，有任何想法可以email我
 */
package cn.esoule.distributed.terminal.core.nio.server;

/**
 * 断开连接任务(例如，备份)
 *
 * @author 510655387@qq.com
 */
public class DisconnectionTask implements Runnable {

    private final Session connection;

    public DisconnectionTask(Session connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        connection.onDisconnect();
    }
}
