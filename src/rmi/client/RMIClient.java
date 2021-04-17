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

    /**
     * RMIClient is the main client class which is used to communicate with remote instances.
     * <p>
     * This class is responsible for a couple of things:
     * <p>
     *      - binding to an Index Server from where it gets all the information about other instances
     * <p>
     *      - directly communicating with remote instances to get their Stopwatch and pass it to view so that the users
     * can see them.
     * <p>
     *      - handle start/pause/resume press on remote instances
     */

    // Holds the inder server instance
    transient IndexServer indexServer;

    // Holds the current application context
    transient ApplicationController context;

    // Holds the current instance information
    public InstanceInfo instanceInfo;

    // List of Servers to which it is registered to
    transient ArrayList<ServerDecorator> servers;

    // Boolean to indicate whether the client is shutting down
    transient boolean shutdown;

    public RMIClient(ApplicationController context) throws RemoteException {

        this.context = context;
        this.instanceInfo = ApplicationController.instanceInfo;
        UnicastRemoteObject.exportObject(this, 0);
        initializeClient();
    }

    /**
     * This method initializes the client by discarding the old servers list (if existed)
     */
    public void initializeClient() {
        servers = new ArrayList<>();
        shutdown = false;
    }

    /**
     * This method starts the client with a new Index Server using it's IP.
     *
     * Note: The client needs to be shutdown if it was previously started to register to some other index server
     * @param indexServerIp
     */
    public void startClient(String indexServerIp) {

        try {
            Registry indexRegistry = LocateRegistry.getRegistry(indexServerIp, Indexer.INDEXER_PORT);

            indexServer = (IndexServer) indexRegistry.lookup(Indexer.INDEXER_OBJECT_NAME);

            indexServer.registerPeer(this, instanceInfo);
            List<InstanceInfo> allPeersInfos = indexServer.getAllPeers();
            for (InstanceInfo peerInfo : allPeersInfos)
                addServer(peerInfo);
            System.out.println("Client Started with new Index Server: "+indexServerIp);

        } catch (RemoteException e) {
            System.out.println("Couldn't get index server registry: "+indexServerIp);
            e.printStackTrace();
        } catch (NotBoundException e) {
            System.out.println("Couldn't get index server object: "+indexServerIp);
        }

    }

    /**
     * This method finds the remote server instance and registers the client to it.
     * @param instanceInfo
     * @throws RemoteException
     */
    void addServer(InstanceInfo instanceInfo) throws RemoteException {
        if (!shutdown) {
            Registry registry = LocateRegistry.getRegistry(instanceInfo.getHostIP(), 1099);

            try {
                // Getting the remote server object
                Server server = (Server) registry.lookup(instanceInfo.getServerName());

                // Getting the instance information of the server
                InstanceInfo serverInfo = server.getInstanceInfo();

                // If it is its own server, do not add
                if (serverInfo.getInstanceIdentifier().equals(ApplicationController.instanceInfo.getInstanceIdentifier())) {
                    throw new RemoteException();
                }

                // If the client is already registered to this server, do not add
                for (ServerDecorator server_ : servers) {
                    if (server_.getInstanceInfo().getInstanceIdentifier().equals(serverInfo.getInstanceIdentifier())) {
                        throw new RemoteException();
                    }
                }

                ServerDecorator serverDecorator = new ServerDecorator(server, serverInfo);

                VirtualStopwatch virtualStopwatch = null;
                String state = "";

                try {
                    // Getting the remote stopwatch of this server
                    virtualStopwatch = server.getOwnerStopwatchInstance();

                    // Getting the state of the remote stopwatch
                    state = server.getOwnerStopwatchState();

                    // Decorating the remote stopwatch object received
                    RemoteStopwatch remoteStopwatch = new RemoteStopwatch(virtualStopwatch, state, serverDecorator.getInstanceInfo());

                    // Asking the Application Controller to add this stopwatch to the GUI
                    context.addVirtualStopwatch(remoteStopwatch, serverDecorator.getInstanceInfo());

                    // Storing the server object for future uses
                    servers.add(serverDecorator);

                    // Registering this client to the server
                    server.registerClient(this, ApplicationController.instanceInfo);
                } catch (RemoteException e) {
                    System.out.println("Couldn't get remote virtual stopwatch: " + instanceInfo);
                }

            } catch (RemoteException | NotBoundException e) {
            }
        }

    }

    // <------------------------------- Interface methods ---------------------------->

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

    /**
     * This method shuts down the client by unregistering from the index server and from all other instances.
     * This method also initializes the client and makes it ready to be started again with a new index server.
     */
    public void shutdown() {

        shutdown = true;

        // If it is registered to an Index Server, unregister from it.
        if (indexServer != null) {
            try {
                indexServer.unRegisterPeer(instanceInfo);
            } catch (RemoteException e) {
            } catch (Exception e) {
            }

            // Remove all the remote virtual stopwatches from the GUI
            for (ServerDecorator server : servers) {
                context.removeRemoteVirtualStopwatch(server.getInstanceInfo());
            }
        }

        // Unregister from all remote instances on separate threads

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
            shutdownThreads.get(shutdownThreads.size() - 1).start();
        }

        // Wait for all threads to shutdown
        for (Thread shutdownThread : shutdownThreads) {
            try {
                shutdownThread.join();
            } catch (InterruptedException e) {
            }
        }

        // Initialize the client
        initializeClient();
        System.out.println("Client Shutdown");
    }
}
