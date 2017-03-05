package hostmonitor4j;

/**
 * @author Konstantin Kosmachevskiy
 */
public interface Host {

    boolean isOnline() throws MonitorException;

    State getState() throws MonitorException;

    void onStateChanged(StateListener sl);

    long getUpdateInterval();

    /**
     * Changes host state update interval
     *
     * @param intervalInMillis Interval in milliseconds
     */
    void setUpdateInterval(long intervalInMillis);

    /**
     * Turns on/off monitor
     *
     * @param flag Set <code>true</code> to enable or <code>false</code> to disable
     */
    void enableMonitoring(boolean flag);

    boolean isMonitoringEnable();

    enum State {
        ONLINE, OFFLINE
    }

    @FunctionalInterface
    interface StateListener {
        void onStateChanged(Host.State state);
    }

    class MonitorException extends RuntimeException {
        MonitorException() {
            super("Monitoring disabled. State not valid");
        }
    }
}
