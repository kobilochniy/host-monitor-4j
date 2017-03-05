package hostmonitor4j;

import javax.jnlp.ExtendedService;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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

    private Set<StateListener> listeners = new HashSet<>();
    private InetAddress ip;
    private int port;
    private boolean enable;
    private Socket socket;
    private ExecutorService service;
    private State currentState = State.OFFLINE;
    private State previousState = currentState;

    private long interval;

    private boolean online;


    public TcpHost(String ip, int port) {
        this.port = port;
        try {
            this.ip = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public boolean isOnline() {
        return online;
    }


    public void onStateChanged(StateListener sl) {
       listeners.add(sl);
    }

    public void setUpdateInterval(long interval) {
        this.interval = interval;
    }


    public void enableMonitoring(boolean flag) {
        this.enable = flag;
        if (enable) {
            service = Executors.newSingleThreadExecutor();
            service.submit(() -> {
                try {
                    while (this.enable) {
                        try {
                            socket = new Socket(ip, port);
                            online = true;
                            updateState();
                        } catch (IOException e) {
                            e.printStackTrace();
                            online = false;
                            updateState();
                        }
                        Thread.sleep(interval);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });


        } else {
            online = false;
            updateState();
            if (service != null) {
                service.shutdownNow();
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public boolean isEnable() {
        return enable;
    }

    private void updateState(){
        if(isOnline()) currentState = State.ONLINE;
        else currentState = State.OFFLINE;

        if(currentState!=previousState){
            previousState = currentState;
            for (StateListener listener : listeners) {
                listener.onStateChanged(currentState);
            }
        }
    }

}
