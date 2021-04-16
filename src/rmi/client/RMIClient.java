package rmi.client;

import main.ApplicationController;
import main.Indexer;
import main.InstanceInfo;
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
    public InstanceInfo instanceInfo;
    transient ArrayList<ServerDecorator> servers;
    transient boolean shutdown;

    public RMIClient(ApplicationController context) throws RemoteException {

        this.context = context;
        this.instanceInfo = ApplicationController.instanceInfo;
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

            indexServer.registerPeer(this, instanceInfo);
            List<InstanceInfo> allPeersInfos = indexServer.getAllPeers();
            for (InstanceInfo peerInfo : allPeersInfos)
                addServer(peerInfo);
            System.out.println("Client Started");

        } catch (RemoteException e) {
            System.out.println("Couldn't get index server registry.");
            e.printStackTrace();
        } catch (NotBoundException e) {
            System.out.println("Couldn't get index server object.");
        }

    }

    void addServer(InstanceInfo instanceInfo) throws RemoteException {
        if (!shutdown) {
            Registry registry = LocateRegistry.getRegistry(instanceInfo.getHostIP(), 1099);

            try {
                Server server = (Server) registry.lookup(instanceInfo.getServerName());

                InstanceInfo serverInfo = server.getInstanceInfo();

                if (serverInfo.getInstanceIdentifier().equals(ApplicationController.instanceInfo.getInstanceIdentifier())) {
                    throw new RemoteException();
                }
                for (ServerDecorator server_ : servers) {
                    if (server_.getInstanceInfo().getInstanceIdentifier().equals(serverInfo.getInstanceIdentifier())) {
                        throw new RemoteException();
                    }
                }

                ServerDecorator serverDecorator = new ServerDecorator(server, serverInfo);

                VirtualStopwatch virtualStopwatch = null;
                String state = "";

                try {
                    virtualStopwatch = server.getOwnerStopwatchInstance();
                    state = server.getOwnerStopwatchState();
                    RemoteStopwatch remoteStopwatch = new RemoteStopwatch(virtualStopwatch, state, serverDecorator.getInstanceInfo());
                    context.addVirtualStopwatch(remoteStopwatch, serverDecorator.getInstanceInfo());
                    servers.add(serverDecorator);
                    server.registerClient(this, ApplicationController.instanceInfo);
                } catch (RemoteException e) {
                    System.out.println("Couldn't get remote virtual stopwatch: " + instanceInfo);
                }

            } catch (RemoteException | NotBoundException e) {
            }
        }

    }

    @Override
    public void onNewPeer(InstanceInfo peerInfo) throws RemoteException {
        addServer(peerInfo);
    }

    @Override
    public void onPeerClose(InstanceInfo peerInfo) throws RemoteException {
        context.removeRemoteVirtualStopwatch(peerInfo);
    }

    @Override
    public void onTimeUpdated(long time, InstanceInfo serverInfo) throws RemoteException {
        context.notifyVirtualStopwatchTimeUpdated(time, serverInfo);
    }

    @Override
    public void onStartPauseResumePressed(InstanceInfo serverInfo) throws RemoteException {
        context.notifyVirtualStopwatchStartPressed(serverInfo);
    }

    @Override
    public void onStopPressed(InstanceInfo serverInfo) throws RemoteException {
        context.notifyVirtualStopwatchStopPressed(serverInfo);
    }

    @Override
    public void onServerShutdown(InstanceInfo serverInfo) throws RemoteException {
        servers.removeIf(server -> server.getInstanceInfo().getInstanceIdentifier().equals(serverInfo.getInstanceIdentifier()));
        context.removeRemoteVirtualStopwatch(serverInfo);
    }

    @Override
    public void onPing() throws RemoteException {
    }

    public void shutdown() {

        shutdown = true;
        if (indexServer != null) {
            try {
                indexServer.unRegisterPeer(instanceInfo);
            } catch (RemoteException e) {
            } catch (Exception e) {
            }
            for (ServerDecorator server : servers) {
                context.removeRemoteVirtualStopwatch(server.getInstanceInfo());
            }
        }
        ArrayList<Thread> shutdownThreads = new ArrayList<>();
        for (ServerDecorator server : servers) {

            if (servers.get(servers.size() - 1).equals(server)) {

                shutdownThreads.add(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            server.getServer().unRegisterClient(instanceInfo);

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
                            server.getServer().unRegisterClient(instanceInfo);
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
