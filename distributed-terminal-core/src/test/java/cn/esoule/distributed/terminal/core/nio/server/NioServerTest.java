package cn.esoule.distributed.terminal.core.nio.server;

import junit.framework.TestCase;

/**
 *
 * @author yuzhecao@foxmail.com
 */
public class NioServerTest extends TestCase {

    public NioServerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of start method, of class NioServer.
     *
     * @throws java.lang.Throwable
     */
    public void testStart() throws Throwable {
        NioServer nioServer = NioServer.getInstance(null);
        nioServer.start();
    }

}
