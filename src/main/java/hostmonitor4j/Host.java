package hostmonitor4j;

/**
 * @author Konstantin Kosmachevskiy
 */
public interface Host {

    boolean isOnline();

    void onStateChanged(StateListener sl);

    void setTimeout(long timeInMillis);

    void setPollingInterval(long intervalInMillis);

    void enableMonitoring(boolean flag);

    boolean isEnable();

    enum State {
        ONLINE, OFFLINE;
    }

    @FunctionalInterface
    interface StateListener {
        void onStateChanged(Host.State state);
    }
}
