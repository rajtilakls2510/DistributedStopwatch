package rmi.client;

import main.ApplicationController;
import main.Indexer;
import rmi.shared.Client;
import rmi.shared.IndexServer;
import rmi.shared.Server;
import rmi.shared.VirtualStopwatch;

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
    transient boolean shutdown;

    public RMIClient(String identifier, ApplicationController context) throws RemoteException {

        servers = new ArrayList<>();
        this.context = context;
        shutdown = false;

        name = ApplicationController.CLIENT_NAME;
        this.identifier = identifier;
        UnicastRemoteObject.exportObject(this, 0);

    }

    public void startClient(String indexServerIp) {
        if (!shutdown) {
            try {
                Registry indexRegistry = LocateRegistry.getRegistry(indexServerIp, Indexer.INDEXER_PORT);

                indexServer = (IndexServer) indexRegistry.lookup(Indexer.INDEXER_OBJECT_NAME);

                indexServer.registerPeer(this, identifier);
                List<String> allPeersIps = indexServer.getAllPeers();
                for (String peerIp : allPeersIps)
                    addServer(peerIp);

            } catch (RemoteException e) {
                System.out.println("Couldn't get index server registry.");
            } catch (NotBoundException e) {
                System.out.println("Couldn't get index server object.");
            }
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
                    VirtualStopwatchClientImpl virtualStopwatchClientImpl = new VirtualStopwatchClientImpl(virtualStopwatch, state, serverDecorator.getIdentifier());
                    context.addVirtualStopwatch(virtualStopwatchClientImpl, serverDecorator.getIdentifier());
                    servers.add(serverDecorator);
                    server.registerClient(this, identifier);
                } catch (RemoteException e) {
                    System.out.println("Couldn't get remote virtual stopwatch: " + ip);
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
        try {
            indexServer.unRegisterPeer(identifier);
        } catch (RemoteException e) {
        } catch (Exception e) {
        }
        for (ServerDecorator server : servers) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        server.getServer().unRegisterClient(identifier);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }

    }
}
