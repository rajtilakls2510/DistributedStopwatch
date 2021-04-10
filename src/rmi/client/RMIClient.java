package rmi.client;

import main.ApplicationController;
import main.Indexer;
import rmi.indexer.IndexServer;
import rmi.server.Server;
import rmi.virtualstopwatch.RemoteStopwatch;
import stopwatch.VirtualStopwatch;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class RMIClient implements Client {

    transient IndexServer indexServer;
    transient ApplicationController context;
    public String name;
    public String identifier;
    transient ArrayList<ServerDecorator> servers;
    transient boolean shutdown, shutdownComplete;

    public RMIClient(String identifier, ApplicationController context) throws RemoteException {

        this.context = context;
        name = ApplicationController.CLIENT_NAME;
        this.identifier = identifier;
        UnicastRemoteObject.exportObject(this, 0);
        initializeClient();
    }

    public void initializeClient() {
        servers = new ArrayList<>();
        shutdown = false;
    }

    public void startClient(String indexServerIp) {

        try {
            Registry indexRegistry = LocateRegistry.getRegistry(indexServerIp, Indexer.INDEXER_PORT);

            indexServer = (IndexServer) indexRegistry.lookup(Indexer.INDEXER_OBJECT_NAME);

            indexServer.registerPeer(this, identifier);
            List<String> allPeersIps = indexServer.getAllPeers();
            for (String peerIp : allPeersIps)
                addServer(peerIp);
            System.out.println("Client Started");

        } catch (RemoteException e) {
            System.out.println("Couldn't get index server registry.");
        } catch (NotBoundException e) {
            System.out.println("Couldn't get index server object.");
        }

    }

    void addServer(String ip) throws RemoteException {
        if (!shutdown) {
            Registry registry = LocateRegistry.getRegistry(ip, 1099);

            try {
                Server server = (Server) registry.lookup(ApplicationController.SERVER_NAME);

                String serverIdentifier = server.getIdentifier();

                if (serverIdentifier.equals(identifier)) {
                    throw new RemoteException();
                }
                for (ServerDecorator server_ : servers) {
                    if (server_.getIdentifier().equals(serverIdentifier)) {
                        throw new RemoteException();
                    }
                }

                ServerDecorator serverDecorator = new ServerDecorator(server, ApplicationController.SERVER_NAME, serverIdentifier);

                VirtualStopwatch virtualStopwatch = null;
                String state = "";

                try {
                    virtualStopwatch = server.getOwnerStopwatchInstance();
                    state = server.getOwnerStopwatchState();
                    RemoteStopwatch remoteStopwatch = new RemoteStopwatch(virtualStopwatch, state, serverDecorator.getIdentifier());
                    context.addVirtualStopwatch(remoteStopwatch, serverDecorator.getIdentifier());
                    servers.add(serverDecorator);
                    server.registerClient(this, identifier);
                } catch (RemoteException e) {

                    System.out.println("Couldn't get remote virtual stopwatch: " + ip);
                    e.printStackTrace();
                }

            } catch (RemoteException | NotBoundException e) {
            }
        }

    }

    @Override
    public void onNewPeer(String ip) throws RemoteException {
        addServer(ip);
    }

    @Override
    public void onTimeUpdated(long time, String serverIdentifier) throws RemoteException {
        context.notifyVirtualStopwatchTimeUpdated(time, serverIdentifier);
    }

    @Override
    public void onStartPauseResumePressed(String serverIdentifier) throws RemoteException {
        context.notifyVirtualStopwatchStartPressed(serverIdentifier);
    }

    @Override
    public void onStopPressed(String serverIdentifier) throws RemoteException {
        context.notifyVirtualStopwatchStopPressed(serverIdentifier);
    }

    @Override
    public void onServerShutdown(String identifier) throws RemoteException {
        servers.removeIf(server -> server.getIdentifier().equals(identifier));
        context.removeRemoteVirtualStopwatch(identifier);
    }

    public void shutdown() {

        shutdown = true;
        if (indexServer != null) {
            try {
                indexServer.unRegisterPeer(identifier);
            } catch (RemoteException e) {
            } catch (Exception e) {
            }
            for (ServerDecorator server : servers) {
                context.removeRemoteVirtualStopwatch(server.getIdentifier());
            }
        }
        ArrayList<Thread> shutdownThreads = new ArrayList<>();
        for (ServerDecorator server : servers) {

            if (servers.get(servers.size() - 1).equals(server)) {

                shutdownThreads.add(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            server.getServer().unRegisterClient(identifier);

                        } catch (RemoteException e) {
                        }
                        System.out.println("ShutDown Complete");
                    }
                }));
            } else {
                shutdownThreads.add(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            server.getServer().unRegisterClient(identifier);
                        } catch (RemoteException e) {
                        }
                    }
                }));
            }
            shutdownThreads.get(shutdownThreads.size()-1).start();
        }

        for(Thread shutdownThread: shutdownThreads)
        {
            try {
                shutdownThread.join();
            } catch (InterruptedException e) {
            }
        }

        initializeClient();
        System.out.println("Client Shutdown");
    }
}
