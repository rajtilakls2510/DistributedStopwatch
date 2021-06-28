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
import java.security.Policy;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApplicationController {

    /**
     * ApplicationController is the main class of this application. Run this class to start the application.
     * This class is injected to the view and the stopwatch classes to maintain the context of the
     * application and helps them communicate with the server and client
     */

    // Static variable to store the IP of the index server
    public static String indexServerIp = "";

    // Static variable to store the instanceInfo
    public static InstanceInfo instanceInfo;

    // Static variable to store the thread pool
    public static ExecutorService networkThreadPool;

    // Stores own object to be injected
    public static ApplicationController context;

    // The View object of this application
    StopwatchView stopwatchView;

    // The Stopwatch object of this application
    public Stopwatch ownerStopwatchInstance;

    // The Server and Client for this application
    public RMIServer server;
    public RMIClient client;

    public static void main(String[] args) {
        System.out.println("Starting Stopwatch Please Wait....");


        /* Commandline arguments
        If one argument is passed, it is taken as the Index Server IP
        If 2 arguments are passed, they are taken as Index Server IP and Localhost IP (Meant to be a backdoor to
        setting the localhost ip if the localhost ip detector is not working)
        */
        String hostIp = "";
        if (args.length == 1) {
            indexServerIp = args[0];
        } else if (args.length > 1) {
            indexServerIp = args[0];
            hostIp = args[1];
        }

        context = new ApplicationController(hostIp);
    }

    public ApplicationController(String hostIp) {

        // Generating the Instance Information (Setting the timestamp to be the Instance ID)
        instanceInfo = new InstanceInfo(String.valueOf(System.currentTimeMillis()), hostIp);

        // Creating the Thread Pool
        networkThreadPool = Executors.newFixedThreadPool(25);

        displayView();
    }

    void displayView() {
        // Displaying the view

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                stopwatchView = new StopwatchView(context);
                ownerStopwatchInstance = stopwatchView.getOwnerStopwatch();

                // After View is displayed, start the server and client
                startServerAndClient();
            }
        });
    }

    /**
     * This method is responsible for detecting the current machine's IP address.
     * This method is capable of detecting one IP address if there are multiple network interfaces present.
     */
    String detectLocalHostIp() {
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

                    // Filter IPv6 addresses
                    if (addr instanceof Inet6Address) continue;

                    ip = addr.getHostAddress();

                }
                return ip;
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return ip;
    }

    void setConfiguration() {
        // Setting the LocalHostIP as the hostname
        System.setProperty("java.rmi.server.hostname", instanceInfo.getHostIP());

        // Setting the Security Policy File
        System.setProperty("java.security.policy", ApplicationController.class.getResource("resources/all.policy").toString());

        // Setting a timeout for Remote calls so that we don't wait forever to get the response
        System.setProperty("sun.rmi.transport.tcp.responseTimeout", "2000");

        // Setting Security manager
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());
    }

    public void startServerAndClient() {

        System.out.println("Starting Server");

        // If the localhost IP was not already provided, detect the Localhost IP
        if (instanceInfo.getHostIP().length() == 0) {
            instanceInfo.setHostIP(detectLocalHostIp());
        }

        // Display the Instance Information.
        System.out.println("Identifier: " + instanceInfo.getInstanceIdentifier());
        System.out.println("IP: " + instanceInfo.getHostIP());
        stopwatchView.displayInstanceInfo(instanceInfo);

        // Set RMI configuration and Security
        setConfiguration();

        // Starting the Server
        try {
            server = new RMIServer(context);
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(InstanceInfo.RMI_PORT);
            } catch (RemoteException e) {
                registry = LocateRegistry.getRegistry(instanceInfo.getHostIP(), InstanceInfo.RMI_PORT);
            }
            registry.rebind(instanceInfo.getServerName(), server);
        } catch (RemoteException e) {
            System.out.println("Unable to start server");
        }

        // Initializing client
        try {
            client = new RMIClient(context);

            // If index server IP was given as command line argument, then start the client with that Index Server IP
            if (indexServerIp.length() > 0)
                startClientWithIndexServer(indexServerIp);
        } catch (RemoteException e) {
            System.out.println("Unable to create client");
        }

    }

    /**
     * This method is used for registering the Application to a new Index Server
     *
     * @param indexServerIp
     */
    public void startClientWithIndexServer(String indexServerIp) {

        // First unregister all remote clients from the server
        server.unRegisterAllClients();

        // Shutdown the clients thereby unregistering from all remote servers
        client.shutdown();

        // Restart the client with new Index Server
        client.startClient(indexServerIp);

    }

    /**
     * This method runs the clean up tasks when this application is closed. This method shuts downt he server and client.
     */
    public void cleanUp() {
        server.shutdown();
        client.shutdown();
        System.out.println("Application Closed");
    }

    // <------------------------ Object forwarding methods ------------------------->

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

}

