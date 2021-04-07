package rmi.client;

import rmi.shared.Client;
import rmi.shared.Server;
import rmi.shared.VirtualStopwatch;
import ui.ApplicationController;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class RMIClient implements Client {

    transient ApplicationController context;
    public String name;
    public String identifier;
    transient ArrayList<String> localhosts;
    transient ArrayList<String> serverIdentifiers;
    transient ArrayList<ServerDecorator> servers;
    transient boolean shutdown, threadShutdown;
    transient Thread receivingThread;
    private DatagramSocket datagramSocket;

    public RMIClient(String identifier, ApplicationController context) throws RemoteException {

        serverIdentifiers = new ArrayList<>();
        servers = new ArrayList<>();
        this.context = context;
        shutdown = false;
        threadShutdown = false;
        this.localhosts = new ArrayList<>();

        name = ApplicationController.CLIENT_NAME;
        this.identifier = identifier;
        UnicastRemoteObject.exportObject(this, 0);

    }

    public void startClient() throws RemoteException, NotBoundException {
        detectBroadcasts();
    }

    void detectBroadcasts() {
        if (!shutdown) {
            try {
                datagramSocket = new DatagramSocket(11001);
                datagramSocket.setSoTimeout(10);

                byte[] receiveData = new byte[254];
                DatagramPacket receivePacket = new DatagramPacket(receiveData,
                        receiveData.length);
                receivingThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (!shutdown) {
                            try {
                                datagramSocket.receive(receivePacket);
                                String remoteIp = new String(receivePacket.getData(), 0,
                                        receivePacket.getLength());
                                System.out.println("Remote IP: "+remoteIp);

                                addServer(remoteIp);

                            } catch (IOException e) {
                            }
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        datagramSocket.close();
                        threadShutdown = true;
                    }

                });
                receivingThread.start();

            } catch (IOException e) {
            }
        }
    }

    void addServer(String ip) throws RemoteException {
        if (!shutdown) {
            Registry registry = LocateRegistry.getRegistry(ip, 1099);

            try {
                Server server = (Server) registry.lookup(ApplicationController.SERVER_NAME);

                String serverIdentifier = server.getIdentifier();

                if(serverIdentifier.equals(identifier))
                {
                    throw new RemoteException();
                }
                for (String si : serverIdentifiers) {
                    if (si.equals(serverIdentifier)) {
                        throw new RemoteException();
                    }
                }

                ServerDecorator serverDecorator = new ServerDecorator(server, ApplicationController.SERVER_NAME, serverIdentifier);

                VirtualStopwatch virtualStopwatch = null;
                String state = "";

                try {
                    virtualStopwatch = server.getOwnerStopwatchInstance();
                    state = server.getOwnerStopwatchState();
                    VirtualStopwatchClientImpl virtualStopwatchClientImpl = new VirtualStopwatchClientImpl(virtualStopwatch, state, name);
                    context.addVirtualStopwatch(virtualStopwatchClientImpl, serverDecorator.getIdentifier());
                    servers.add(serverDecorator);
                    serverIdentifiers.add(serverIdentifier);
                    server.registerClient(this, identifier);
                } catch (RemoteException e) {
                    System.out.println("Couldn't get remote virtual stopwatch: " + ip);
                }

            } catch (RemoteException | NotBoundException e) {
                e.printStackTrace();
            }
        }

    }

    public void setMyIps(ArrayList<String> localhosts) {
        this.localhosts = localhosts;
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
            receivingThread.join();
        } catch (InterruptedException e) {

        }
        for (ServerDecorator server : servers) {
            try {
                server.getServer().unRegisterClient(identifier);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
