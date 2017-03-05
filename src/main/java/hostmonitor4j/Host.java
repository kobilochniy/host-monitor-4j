package hostmonitor4j;

/**
 * @author Konstantin Kosmachevskiy
 */
public interface Host {

    boolean isOnline();

    void onStateChanged(StateListener sl);


    /**
     * Changes host state update interval
     * @param interval Interval in milliseconds
     */
    void setUpdateInterval(long interval);

    /**
     * Turns on/off monitor
     * @param flag Set <code>true</code> to enable or <code>false</code> to disable
     */
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
