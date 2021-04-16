package main;

import rmi.client.RMIClient;
import rmi.server.RMIServer;
import stopwatch.Stopwatch;
import stopwatch.VirtualStopwatch;
import ui.StopwatchView;

import javax.swing.*;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Enumeration;

public class ApplicationController {

    public static String indexServerIp = "";
    public static InstanceInfo instanceInfo;

    StopwatchView stopwatchView;
    public Stopwatch ownerStopwatchInstance;
    public RMIServer server;
    public RMIClient client;
    public boolean viewReady;

    public static ApplicationController context;

    public ApplicationController(String hostIp) {
        instanceInfo = new InstanceInfo(String.valueOf(System.currentTimeMillis()), hostIp);
        displayView();
    }

    void displayView() {
        viewReady = false;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                stopwatchView = new StopwatchView(context);
                ownerStopwatchInstance = stopwatchView.getOwnerStopwatch();

                viewReady = true;
                startServerAndClient();
            }
        });
    }

    public void startServerAndClient() {

        while (!viewReady) {
        }
        System.out.println("Starting Server");
        if(instanceInfo.getHostIP().length()==0) {
            String ip = "";
            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface iface = interfaces.nextElement();
                    // filters out 127.0.0.1 and inactive interfaces
                    if (iface.isLoopback() || !iface.isUp() || !iface.supportsMulticast())
                        continue;

                    Enumeration<InetAddress> addresses = iface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();

                        // *EDIT*
                        if (addr instanceof Inet6Address) continue;

                        ip = addr.getHostAddress();

                    }
                    break;
                }
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
            instanceInfo.setHostIP(ip);
        }
        System.out.println("Identifier: "+instanceInfo.getInstanceIdentifier());
        System.out.println("IP: " + instanceInfo.getHostIP());
        stopwatchView.displayIP(instanceInfo);
        System.setProperty("java.rmi.server.hostname", instanceInfo.getHostIP());
        System.setProperty("java.security.policy", "all.policy");
        System.setProperty("sun.rmi.transport.tcp.responseTimeout", "2000");

        if(System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());

        try {
            server = new RMIServer(context);
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(1099);
            } catch (RemoteException e) {
                registry = LocateRegistry.getRegistry(instanceInfo.getHostIP(), 1099);
            }
            registry.rebind(instanceInfo.getServerName(), server);
        } catch (RemoteException e) {
            System.out.println("Unable to start server");
        }

        try {
            client = new RMIClient(context);
            if (indexServerIp.length() > 0)
                startClientWithIndexServer(indexServerIp);
        } catch (RemoteException e) {
            System.out.println("Unable to create client");
        }

    }

    public void startClientWithIndexServer(String indexServerIp) {
        server.unRegisterAllClients();
        client.shutdown();
        client.startClient(indexServerIp);

    }

    public void addVirtualStopwatch(VirtualStopwatch virtualStopwatch, InstanceInfo serverInfo) {
        stopwatchView.addRemoteStopwatch(virtualStopwatch, serverInfo);
    }

    public void removeRemoteVirtualStopwatch(InstanceInfo serverInfo) {
        stopwatchView.removeRemoteVirtualStopwatch(serverInfo);
    }

    public void notifyServerStopwatchTimeChange(long time) {
        server.notifyClients(time);
    }

    public void notifyServerStartPauseResumePressed() {
        server.notifyStartPauseResumePressed();
    }

    public void notifyServerStartPauseResumePressed(InstanceInfo doNotBroadcastToClient) {
        server.notifyStartPauseResumePressed(doNotBroadcastToClient);
    }

    public void notifyServerStopPressed() {
        server.notifyStopPressed();
    }

    public void notifyServerStopPressed(InstanceInfo doNotBroadcastToClient) {
        server.notifyStopPressed(doNotBroadcastToClient);
    }

    public void notifyVirtualStopwatchTimeUpdated(long time, InstanceInfo serverInfo) {
        stopwatchView.notifyVirtualStopwatchTimeUpdated(time, serverInfo);
    }

    public void notifyVirtualStopwatchStartPressed(InstanceInfo serverInfo) {
        stopwatchView.notifyVirtualStopwatchStartPressed(serverInfo);
    }

    public void notifyVirtualStopwatchStopPressed(InstanceInfo serverInfo) {
        stopwatchView.notifyVirtualStopwatchStopPressed(serverInfo);
    }

    public void cleanUp() {
        server.shutdown();
        client.shutdown();
        System.out.println("Application Closed");
    }

    public static void main(String[] args) {
        System.out.println("Starting Stopwatch Please Wait....");
        String hostIp="";
        if (args.length == 1) {
            indexServerIp = args[0];
        }
        else if (args.length > 1) {
            indexServerIp = args[0];
            hostIp= args[1];
        }
        context = new ApplicationController(hostIp);
    }

}

