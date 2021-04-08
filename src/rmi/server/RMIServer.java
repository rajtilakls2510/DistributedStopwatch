package rmi.server;

import rmi.shared.Client;
import rmi.shared.Server;
import rmi.shared.VirtualStopwatch;
import ui.ApplicationController;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

public class RMIServer implements Server {

    transient ArrayList<ClientDecorator> clients;
    public String name;
    public String identifier;
    transient ApplicationController context;
    transient boolean shutdown, threadShutdown;
    transient Thread broadcastThread;
    transient DatagramSocket datagramSocket;

    public RMIServer(String identifier, ApplicationController context) throws RemoteException {
        name = ApplicationController.SERVER_NAME;
        this.identifier = identifier;
        this.context = context;
        clients = new ArrayList<>();
        shutdown = false;
        threadShutdown = false;
        UnicastRemoteObject.exportObject(this, 0);
//        broadcastExistance();
    }

//    public void broadcastExistance() {
//
//        try {
//            datagramSocket = new DatagramSocket(11000);
//            datagramSocket.setSoTimeout(20);
//            broadcastThread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    byte[] buffer;
//                    while (!shutdown) {
//                        try {
//                            Enumeration<NetworkInterface> networkInterfaces
//                                    = NetworkInterface.getNetworkInterfaces();
//                            while (networkInterfaces.hasMoreElements()) {
//                                NetworkInterface networkInterface = networkInterfaces.nextElement();
//                                if (networkInterface.isLoopback() || !networkInterface.isUp() || !networkInterface.supportsMulticast())
//                                    continue;
//
//                                Iterator<InterfaceAddress> iterator = networkInterface.getInterfaceAddresses().iterator();
//                                while (iterator.hasNext()) {
//                                    InterfaceAddress iaddress = iterator.next();
//                                    InetAddress broadcastAddress = iaddress.getBroadcast();
//                                    if (broadcastAddress != null) {
//                                        InetAddress localAddress = iaddress.getAddress();
//                                        buffer = identifier.getBytes(StandardCharsets.UTF_8);
//                                        try {
//                                            DatagramPacket packet = new DatagramPacket(
//                                                    buffer, buffer.length, broadcastAddress, 11001
//                                            );
//                                            datagramSocket.send(packet);
//
//                                        } catch (IOException e) {
//                                            e.printStackTrace();
//
//                                        }
//                                    }
//                                }
//
//                            }
//                        } catch (SocketException ignored) {
//                        }
//
//                        try {
//                            Thread.sleep(500);
//                        } catch (InterruptedException ignored) {
//                        }
//                    }
//                    datagramSocket.close();
//                    threadShutdown = true;
//
//                }
//            });
//            broadcastThread.start();
//
//        } catch (SocketException e) {
//            System.out.println("Broadcast Socket Creation exception");
//        }
//    }

    @Override
    public String getIdentifier() throws RemoteException {
        return identifier;
    }

    @Override
    public void registerClient(Client client, String clientIdentifier) throws RemoteException {
        clients.add(new ClientDecorator(client, ApplicationController.CLIENT_NAME, clientIdentifier));
    }

    @Override
    public void unRegisterClient(String clientIdentifier) throws RemoteException {
        clients.removeIf(client -> client.getIdentifier().equals(clientIdentifier));

    }

    @Override
    public VirtualStopwatch getOwnerStopwatchInstance() throws RemoteException {
        return new VirtualStopwatchServer(context.ownerStopwatchInstance);
    }

    @Override
    public String getOwnerStopwatchState() throws RemoteException {
        return context.ownerStopwatchInstance.getPreviousStateName();
    }

    public void notifyClients(long time) {
        try {
            for (ClientDecorator client : clients) {
                try {
                    client.getClient().onTimeUpdated(time, identifier);
                } catch (RemoteException e) {
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void notifyStartPauseResumePressed() {
        try {
            for (ClientDecorator client : clients) {
                try {
                    client.getClient().onStartPauseResumePressed(identifier);
                } catch (RemoteException e) {
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void notifyStartPauseResumePressed(String doNotBroadcastToClient) {
        try {
            for (ClientDecorator client : clients) {

                String clientIdentifier = client.getIdentifier();

                if (!clientIdentifier.equals(doNotBroadcastToClient)) {
                    try {
                        client.getClient().onStartPauseResumePressed(identifier);
                    } catch (RemoteException e) {
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void notifyStopPressed() {
        try {
            for (ClientDecorator client : clients) {
                try {
                    client.getClient().onStopPressed(identifier);
                } catch (RemoteException e) {
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void notifyStopPressed(String doNotBroadcastToClient) {
        try {
            for (ClientDecorator client : clients) {

                String clientIdentifier = client.getIdentifier();

                if (!clientIdentifier.equals(doNotBroadcastToClient)) {
                    try {
                        client.getClient().onStopPressed(identifier);
                    } catch (RemoteException e) {
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void shutdown() {
        shutdown = true;
//        try {
//            broadcastThread.join();
//        } catch (InterruptedException e) {
//        }
        try {
            for (ClientDecorator client : clients) {
                try {
                    client.getClient().onServerShutdown(identifier);
                } catch (Exception e) {
                }
            }
        } catch (Exception ignored) {
        }
    }

}
