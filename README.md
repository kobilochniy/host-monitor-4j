**Simple and easy-of-use host monitor for Java**

To run TCP host monitoring:

    Host host = new TcpHost("192.168.1.100", 80);
    
    host.enableMonitoring(true);
    host.setUpdateInterval(1000);
    
    host.onStateChanged(state -> System.out.println("State changed to: " + state));
    
    System.out.println("Monitor state: "  + host.isMonitoringEnable());
    System.out.println("Host status: "  + host.isOnline());
    System.out.println("Current update interval: "  + host.getUpdateInterval());
    
    host.enableMonitoring(false);
