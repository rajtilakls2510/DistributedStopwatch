package ui;

import rmi.client.RMIClient;
import rmi.client.VirtualStopwatchClient;
import rmi.server.RMIServer;
import stopwatch.Stopwatch;

import javax.swing.*;
import java.net.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ApplicationController {

    public static String SERVER_NAME, CLIENT_NAME;

    StopwatchView stopwatchView;
    public Stopwatch ownerStopwatchInstance;
    public RMIServer server;
    public RMIClient client;
    public boolean viewReady;
    public String localhostName;

    public static ApplicationController context;

    public ApplicationController() {
        SERVER_NAME = "Server";
        CLIENT_NAME = "Client";
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

    void startServerAndClient() {

        while (!viewReady) {
        }
        System.out.println("Starting Server");

////        byte[] ip;
        try {
            localhostName = InetAddress.getLocalHost().getHostName();
//            localhost = InetAddress.getByAddress(ip).toString().substring(1);
            System.out.println("Localhost: " + localhostName);
        } catch (UnknownHostException e) {
            System.out.println("Localhost not found");
            System.exit(0);
        }

//        try {
//            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
//            while (interfaces.hasMoreElements()) {
//                NetworkInterface iface = interfaces.nextElement();
//                // filters out 127.0.0.1 and inactive interfaces
//                if (iface.isLoopback() || !iface.isUp() || !iface.supportsMulticast())
//                    continue;
//
//                Enumeration<InetAddress> addresses = iface.getInetAddresses();
//                while(addresses.hasMoreElements()) {
//                    InetAddress addr = addresses.nextElement();
//
//                    // *EDIT*
//                    if (addr instanceof Inet6Address) continue;
//
//                    localhost = addr.getHostAddress();
//                    System.out.println("Localhost " + localhost);
//                }
//            }
//        } catch (SocketException e) {
//            throw new RuntimeException(e);
//        }

        try {
            server = new RMIServer(localhostName, context);
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(1099);
            } catch (RemoteException e) {
                registry = LocateRegistry.getRegistry(1099);
            }
            registry.rebind(ApplicationController.SERVER_NAME, server);
        } catch (RemoteException e) {
            System.out.println("Unable to start server");
        }

        try {
            client = new RMIClient(localhostName, context);
            client.startClient();
        } catch (RemoteException e) {
            System.out.println("Unable to create client");
        } catch (NotBoundException e) {
            System.out.println("Unable to start client");
        }

    }


    public void addVirtualStopwatch(VirtualStopwatchClient virtualStopwatch, String serverIdentifier) {
        stopwatchView.addRemoteStopwatch(virtualStopwatch, serverIdentifier);
    }

    public void removeRemoteVirtualStopwatch(String identifier) {
        stopwatchView.removeRemoteVirtualStopwatch(identifier);
    }

    public void notifyServerStopwatchTimeChange(long time) {
        server.notifyClients(time);
    }

    public static void main(String[] args) {
        System.out.println("Starting Stopwatch Please Wait....");
        context = new ApplicationController();
    }

    public void notifyServerStartPauseResumePressed() {
        server.notifyStartPauseResumePressed();
    }

    public void notifyServerStartPauseResumePressed(String doNotBroadcastToClient) {
        server.notifyStartPauseResumePressed(doNotBroadcastToClient);
    }

    public void notifyServerStopPressed() {
        server.notifyStopPressed();
    }

    public void notifyServerStopPressed(String doNotBroadcastToClient) {
        server.notifyStopPressed(doNotBroadcastToClient);
    }

    public void notifyVirtualStopwatchTimeUpdated(long time, String serverIdentifier) {
        stopwatchView.notifyVirtualStopwatchTimeUpdated(time, serverIdentifier);
    }

    public void notifyVirtualStopwatchStartPressed(String serverIdentifier) {
        stopwatchView.notifyVirtualStopwatchStartPressed(serverIdentifier);
    }

    public void notifyVirtualStopwatchStopPressed(String serverIdentifier) {
        stopwatchView.notifyVirtualStopwatchStopPressed(serverIdentifier);
    }

    public void cleanUp() {
        server.shutdown();
        System.out.println("Server Shutdown. Shutting down Client");
        client.shutdown();
        System.out.println("Application Closed");
    }

}

