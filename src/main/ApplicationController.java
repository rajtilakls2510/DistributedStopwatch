package main;

import rmi.client.RMIClient;
import rmi.server.RMIServer;
import stopwatch.Stopwatch;
import stopwatch.VirtualStopwatch;
import ui.StopwatchView;

import javax.swing.*;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
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

    public ApplicationController() {
        instanceInfo = new InstanceInfo(String.valueOf(System.currentTimeMillis()));
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

                    instanceInfo.setHostIP(addr.getHostAddress());
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Identifier: "+instanceInfo.getInstanceIdentifier());
        System.out.println("IP: " + instanceInfo.getHostIP());
        stopwatchView.displayIP(instanceInfo);
        System.setProperty("java.rmi.server.hostname", instanceInfo.getHostIP());
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
        System.out.println("Do not broadcast context: "+doNotBroadcastToClient.getInstanceIdentifier());
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
        if (args.length > 0) {
            indexServerIp = args[0];
        }
        context = new ApplicationController();
    }

}

