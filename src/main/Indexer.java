package main;

import rmi.indexer.IndexServerImpl;

import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Enumeration;
import java.util.Iterator;

public class Indexer {

    public static final Integer INDEXER_PORT = 1100;
    public static final String INDEXER_OBJECT_NAME = "Index";

    public static void main(String[] args) {
        String ip = "192.168.29.153";
        Enumeration<NetworkInterface> networkInterfaces
                                    = null;

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp() || !iface.supportsMulticast())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    // *EDIT*
                    if (addr instanceof Inet6Address) continue;

                    ip = addr.getHostAddress();
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }


        System.setProperty("java.rmi.server.hostname", ip);

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

        try {
            IndexServerImpl indexServer = new IndexServerImpl();
            registry.rebind(INDEXER_OBJECT_NAME, indexServer);
        } catch (RemoteException e) {
            System.out.println("Couldn't create index server");
        }
        System.out.println("Index Server Started");
        System.out.println("IP: "+ip);
        while(true){}
    }
}
