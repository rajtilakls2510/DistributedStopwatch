package main;

import rmi.indexer.IndexServerImpl;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Indexer {

    /**
     * Indexer is the main class to start an Index Server in this Machine.
     */

    public static final Integer INDEXER_PORT = 1100;
    public static final String INDEXER_OBJECT_NAME = "Index";
    public static ExecutorService networkThreadPool;

    public static void main(String[] args) {
        String ip = "";


        /* CommandLine arguments
        If one argument is passed, it is taken as the localhost IP (Meant to be a backdoor to
        setting the localhost ip if the localhost ip detector is not working)
         */
        if (args.length > 0) {
            ip = args[0];
        } else {
            ip = detectLocalHostIp();
        }

        // Setting the configuration for the Server
        setConfiguration(ip);

        // Initializing the Thread Pool for Network calls
        networkThreadPool = Executors.newFixedThreadPool(25);

        // Starting the registry
        Registry registry = null;
        try {
            registry = LocateRegistry.createRegistry(INDEXER_PORT);
        } catch (RemoteException e) {
            try {
                registry = LocateRegistry.getRegistry(INDEXER_PORT);
            } catch (RemoteException remoteException) {
                System.out.println("Couldn't get registry.");
                System.exit(0);
            }
        }

        // Starting the Index Server
        try {
            IndexServerImpl indexServer = new IndexServerImpl();
            registry.rebind(INDEXER_OBJECT_NAME, indexServer);

            // Starting a thread which periodically looks for inactive peers and filters them out
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        indexServer.filterInactivePeers();
                        try {
                            Thread.sleep(6000);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }).start();
        } catch (RemoteException e) {
            System.out.println("Couldn't create index server");
        }
        System.out.println("Index Server Started");
        System.out.println("IP: " + ip);

        // Some monitoring stuff
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
                    System.out.println("\nProcess CPU Load: " + osBean.getProcessCpuLoad() * 100 + " %");
                    System.out.println("Process CPU Time: " + formatTime(osBean.getProcessCpuTime() / 1000000L));
                    System.out.println("System CPU Load: " + osBean.getSystemCpuLoad() * 100 + " %");
                    System.out.println("System Load Average: " + osBean.getSystemLoadAverage() + " %");
                    System.out.println("Number of running threads: " + ManagementFactory.getThreadMXBean().getThreadCount());
                    System.out.println("Available Processors: " + osBean.getAvailableProcessors());
                    System.out.println("System Architecture: " + osBean.getArch());
                    System.out.println("Committed Virtual Mem size: " + osBean.getCommittedVirtualMemorySize() / (1024 * 1024) + " MB");
                    System.out.println("Free Physical Mem size: " + osBean.getFreePhysicalMemorySize() / (1024 * 1024) + " MB");
                    System.out.println("Total Physical Mem size: " + osBean.getTotalPhysicalMemorySize() / (1024 * 1024) + " MB");
                    System.out.println("Free Swap Space size: " + osBean.getFreeSwapSpaceSize() / (1024 * 1024) + " MB");
                    System.out.println("Total Swap Space Size: " + osBean.getTotalSwapSpaceSize() / (1024 * 1024) + " MB");
                    System.out.println("Total Mem: " + Runtime.getRuntime().totalMemory() / (1024 * 1024) + " MB");
                    System.out.println("Free Mem: " + Runtime.getRuntime().freeMemory() / (1024 * 1024) + " MB");
                    System.out.println("Max Mem: " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " MB");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ignored) {
                    }

                    for (int i = 0; i < 16; i++) {
                        System.out.printf("\033[%dA", 1); // Move up
                        System.out.print("\033[2K"); // Erase line content
                    }
                }
            }
        }).start();
        */
    }

    /**
     * This method is responsible for detecting the current machine's IP address.
     * This method is capable of detecting one IP address if there are multiple network interfaces present.
     */
    static String detectLocalHostIp() {
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
                return ip;
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return ip;
    }

    static void setConfiguration(String ip) {
        // Setting the LocalHostIP as the hostname
        System.setProperty("java.rmi.server.hostname", ip);

        // Setting the Security Policy File
        System.setProperty("java.security.policy", Indexer.class.getResource("resources/all.policy").toString());

        // Setting a timeout for Remote calls so that we don't wait forever to get the response
        System.setProperty("sun.rmi.transport.tcp.responseTimeout", "2000");

        // Setting Security manager
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());
    }

    static String formatTime(long time) {
        int time1 = (int) time;
        int milis = 0, secs = 0, minutes = 0, hours = 0;
        String formattedTime = "";

        milis = time1 % 1000;
        time1 = time1 / 1000;
        secs = time1 % 60;
        time1 = time1 / 60;
        minutes = time1 % 60;
        time1 = time1 / 60;
        hours = time1 % 60;

        formattedTime = String.format("%02d", secs) + ":" + String.format("%03d", milis);
        if (minutes > 0)
            formattedTime = String.format("%02d", minutes) + ":" + formattedTime;
        if (hours > 0)
            formattedTime = String.format("%02d", hours) + ":" + formattedTime;
        return formattedTime;

    }
}
