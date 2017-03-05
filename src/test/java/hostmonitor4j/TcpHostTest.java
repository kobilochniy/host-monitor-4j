package hostmonitor4j;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

/**
 * Created by vlad on 3/5/2017.
 */
public class TcpHostTest {
    private Host tcpHost;
    private static final int PORT = 10826;
    private FakeHost fakeHost = new FakeHost();
    private Host.StateListener stateListener = System.out::println;

    @Before
    public void setUp() throws Exception {
        tcpHost = new TcpHost("127.0.0.1", PORT);
        tcpHost.onStateChanged(stateListener);
        stateListener = null;
    }


    @Test
    public void isEnableMonitoringTest() throws InterruptedException {
/*
        fakeHost.start();
        Thread.sleep(100);
        tcpHost.enableMonitoring(true);
        Thread.sleep(100);
        assertTrue(tcpHost.isEnable());
        assertTrue(tcpHost.isOnline());

        fakeHost.stop();
        tcpHost.enableMonitoring(false);
        assertFalse(tcpHost.isEnable());
        assertFalse(tcpHost.isOnline());
*/

        tcpHost.setUpdateInterval(1000);
        fakeHost.start();
        tcpHost.enableMonitoring(true);
        Thread.sleep(200);
        assertTrue(tcpHost.isOnline());

        fakeHost.stop();
        tcpHost.setUpdateInterval(5000);
        Thread.sleep(100);
        assertTrue(tcpHost.isOnline());
        Thread.sleep(2000);
        assertFalse(tcpHost.isOnline());

        fakeHost.start();
        Thread.sleep(100);
        assertFalse(tcpHost.isOnline());
        Thread.sleep(5000);
        assertTrue(tcpHost.isOnline());
    }


    class FakeHost implements Runnable {
        ServerSocket serverSocket;
        ExecutorService service;

        void start() {
            try {
                serverSocket = new ServerSocket(PORT);
                service = Executors.newSingleThreadExecutor();
                service.submit(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void stop() {
            service.shutdownNow();
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                Socket accept = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}