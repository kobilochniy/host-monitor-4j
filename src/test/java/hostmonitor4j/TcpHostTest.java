package hostmonitor4j;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by vlad on 3/5/2017.
 */
public class TcpHostTest {
    private static final Logger logger = LoggerFactory.getLogger(TcpHostTest.class);
    private static final int PORT = 10826;
    private Host tcpHostMonitor;
    private FakeHost fakeHost;
    private List<Host.State> onlineEvents = new ArrayList<>();
    private List<Host.State> offlineEvents = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        tcpHostMonitor = new TcpHost("127.0.0.1", PORT);
        fakeHost = new FakeHost();
        onlineEvents.clear();
        offlineEvents.clear();

        tcpHostMonitor.onStateChanged(state -> {
            if (state == Host.State.ONLINE)
                onlineEvents.add(state);
            if (state == Host.State.OFFLINE)
                offlineEvents.add(state);
        });
    }

    @Test
    public void youCanEnableMonitoringAndCheckHostState() throws InterruptedException {
        Assert.assertFalse("Monitor should be disable by default.", tcpHostMonitor.isMonitoringEnable());

        tcpHostMonitor.enableMonitoring(true);
        Assert.assertTrue("And now it should be enable.", tcpHostMonitor.isMonitoringEnable());

        // Wait during update period and check state
        Thread.sleep(TcpHost.DEFAULT_UPDATE_INTERVAL);
        Assert.assertFalse("It should be offline.", tcpHostMonitor.isOnline());
        Assert.assertEquals(Host.State.OFFLINE, tcpHostMonitor.getState());

        //And now lets start host and check again
        fakeHost.start();
        Thread.sleep(TcpHost.DEFAULT_UPDATE_INTERVAL);
        Assert.assertTrue("It should be online.", tcpHostMonitor.isOnline());
        Assert.assertEquals(Host.State.ONLINE, tcpHostMonitor.getState());
        Assert.assertEquals("Online event should be generated.", 1, onlineEvents.size());

        //Shutdown host, wait and check state
        fakeHost.stop();
        Assert.assertTrue("It should be online until next checking.", tcpHostMonitor.isOnline());
        Thread.sleep(TcpHost.DEFAULT_UPDATE_INTERVAL);
        Assert.assertFalse("And now it should be offline.", tcpHostMonitor.isOnline());
        Assert.assertEquals("Offline event should be generated.", 1, offlineEvents.size());

        tcpHostMonitor.enableMonitoring(false);
        Assert.assertFalse("Monitor should be disabled.", tcpHostMonitor.isMonitoringEnable());
    }

    @Test
    public void youCanChangeUpdateInterval() throws Exception {
        tcpHostMonitor.setUpdateInterval(800);
        tcpHostMonitor.enableMonitoring(true);

        fakeHost.start();
        Thread.sleep(800);
        Assert.assertTrue("Should be online.", tcpHostMonitor.isOnline());

        fakeHost.stop();
        Thread.sleep(800);
        Assert.assertFalse("Should be offline.", tcpHostMonitor.isOnline());

        //You can change interval during monitoring but it will come into force on next cycle
        tcpHostMonitor.setUpdateInterval(600);
        fakeHost.start();
        Thread.sleep(600);
        Assert.assertTrue("Should be online.", tcpHostMonitor.isOnline());
        fakeHost.stop();
        Thread.sleep(600);
        Assert.assertFalse("Should be offline.", tcpHostMonitor.isOnline());

        Assert.assertEquals("It should be two events registered", 2, onlineEvents.size());
        Assert.assertEquals("It should be two events registered", 2, offlineEvents.size());

        tcpHostMonitor.enableMonitoring(false);
    }

    @Test
    public void youCanGetExceptionDuringGettingStateInCaseOfMonitorDisabled() {
        try {
            tcpHostMonitor.isOnline();
            Assert.fail();
        } catch (Host.MonitorException e) {
            Assert.assertEquals(e.getMessage(), "Monitoring disabled. State not valid");
        }

        try {
            tcpHostMonitor.getState();
            Assert.fail();
        } catch (Host.MonitorException e) {
            Assert.assertEquals(e.getMessage(), "Monitoring disabled. State not valid");
        }

        tcpHostMonitor.enableMonitoring(true);

        try {
            tcpHostMonitor.isOnline();
        } catch (Host.MonitorException e) {
            Assert.fail();
        }

        try {
            tcpHostMonitor.getState();
        } catch (Host.MonitorException e) {
            Assert.fail();
        }

        tcpHostMonitor.enableMonitoring(false);
    }

    private class FakeHost implements Runnable {
        private ServerSocket serverSocket;
        private ExecutorService service;

        void start() {
            try {
                serverSocket = new ServerSocket(PORT);
                service = Executors.newSingleThreadExecutor();
                service.submit(this);
            } catch (IOException e) {
                logger.debug(this.getClass().getSimpleName() + " starting failure. " + e.getMessage());
            }
        }

        void stop() {
            if (service != null && serverSocket != null) {
                service.shutdownNow();
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    logger.debug(this.getClass().getSimpleName() + " stopping failure.");
                }
            }
        }

        public void run() {
            logger.debug(this.getClass().getSimpleName() + " started.");
            try {
                while (!serverSocket.isClosed())
                    serverSocket.accept();
            } catch (IOException e) {
                if (!e.getMessage().contains("Socket closed")) {
                    logger.debug(this.getClass().getSimpleName() + " general failure. " + e.getMessage());
                }
            }
            logger.debug(this.getClass().getSimpleName() + " stopped.");
        }
    }
}