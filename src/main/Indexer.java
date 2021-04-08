package main;

import rmi.indexer.IndexServerImpl;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Indexer {

    public static final Integer INDEXER_PORT = 1100;
    public static final String INDEXER_OBJECT_NAME = "Index";

    public static void main(String[] args) {
        String ip = "192.168.29.153";

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
        while(true){}
    }
}
