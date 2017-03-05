package hostmonitor4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by vlad on 3/5/2017.
 */
public class TcpHost implements Host {
    public static final long DEFAULT_UPDATE_INTERVAL = 1000;
    private static final Logger logger = LoggerFactory.getLogger(TcpHost.class);
    private Set<StateListener> listeners = new HashSet<>();
    private InetAddress ip;
    private int port;
    private Socket socket;
    private ExecutorService service;

    private State currentState = State.OFFLINE;
    private State previousState = currentState;

    private Thread checker;

    private volatile boolean enable;
    private volatile long interval = DEFAULT_UPDATE_INTERVAL;
    private volatile boolean online;


    public TcpHost(String ip, int port) {
        this.port = port;
        try {
            this.ip = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public boolean isOnline() {
        if (isMonitoringEnable())
            return online;
        else
            throw new MonitorException();
    }

    /**
     * Update <code>isOnline</code> flag
     * and <code>getState</code> value
     *
     * @param state new state
     */
    private void updateOnlineStatus(boolean state) {
        online = state;
        currentState = isOnline() ? State.ONLINE : State.OFFLINE;

        if (currentState != previousState) {
            previousState = currentState;
            listeners.stream().forEach(
                    stateListener -> stateListener.onStateChanged(currentState));
        }
    }

    public State getState() throws MonitorException {
        if (isMonitoringEnable())
            return currentState;
        else
            throw new MonitorException();
    }

    public void onStateChanged(StateListener sl) {
        listeners.add(sl);
    }

    public long getUpdateInterval() {
        return interval;
    }

    public void setUpdateInterval(long interval) {
        this.interval = interval;
    }

    public void enableMonitoring(boolean flag) {
        if (enable == flag)
            return;

        this.enable = flag;

        if (enable) {
            service = Executors.newSingleThreadExecutor();
            checker = new Checker();
            service.submit(checker);
        } else {
            if (service != null)
                service.shutdownNow();
            if (socket != null)
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

        }
    }

    public boolean isMonitoringEnable() {
        return enable;
    }

    private class Checker extends Thread {
        @Override
        public void run() {
            logger.debug(this.getClass().getSimpleName() + " monitor enabled.");
            try {
                while (enable) {
                    logger.debug("Checking " + ip.getHostAddress() + ":" + port);
                    try {
                        socket = new Socket(ip, port);

                        updateOnlineStatus(socket.isConnected());
                    } catch (IOException e) {
                        updateOnlineStatus(false);

                        if (!e.getMessage().contains("Connection refused"))
                            logger.debug("Socket general failure. " + e.getMessage());
                    }
                    Thread.sleep(interval);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            logger.debug(this.getClass().getSimpleName() + " monitor disabled.");
        }
    }

}
