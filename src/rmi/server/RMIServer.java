package rmi.server;

import rmi.client.RMIClient;
import rmi.shared.Client;
import rmi.shared.Server;
import rmi.shared.VirtualStopwatch;
import ui.ApplicationController;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.stream.Stream;

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
        broadcastExistance1();
    }

    public void broadcastExistance1()
    {

        try {
            datagramSocket = new DatagramSocket(11000);
            datagramSocket.setSoTimeout(10);
            broadcastThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] buffer;
                    while(!shutdown)
                    {
                        try {
                            Enumeration<NetworkInterface> networkInterfaces
                                    = NetworkInterface.getNetworkInterfaces();
                            while (networkInterfaces.hasMoreElements()) {
                                NetworkInterface networkInterface = networkInterfaces.nextElement();
                                if (networkInterface.isLoopback() || !networkInterface.isUp() || !networkInterface.supportsMulticast())
                                    continue;

                                Iterator<InterfaceAddress> iterator = networkInterface.getInterfaceAddresses().iterator();
                                while(iterator.hasNext())
                                {
                                    InterfaceAddress iaddress = iterator.next();
                                    InetAddress broadcastAddress = iaddress.getBroadcast();
                                    if(broadcastAddress!=null) {
                                        InetAddress localAddress = iaddress.getAddress();
                                        buffer = localAddress.getHostAddress().getBytes(StandardCharsets.UTF_8);
                                        try {
                                            DatagramPacket packet = new DatagramPacket(
                                                    buffer, buffer.length, broadcastAddress, 11001
                                            );
                                            datagramSocket.send(packet);

                                        } catch (IOException e) {
                                            e.printStackTrace();

                                        }
                                    }
                                }

                            }
                        } catch (SocketException ignored) { }

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ignored) { }
                    }
                    datagramSocket.close();
                    threadShutdown = true;

                }
            });
            broadcastThread.start();


        } catch (SocketException e) {
            System.out.println("Broadcast Socket Creation exception");
        }
    }

    @Override
    public String getIdentifier() throws RemoteException {
        return identifier;
    }

    //    public void broadcastExistance() {
//        byte[] buffer = name.getBytes();
//
//        try {
//
//            datagramSocket = new DatagramSocket(11000);
//            datagramSocket.setSoTimeout(100);
//            broadcastThread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    while (!shutdown) {
//                        try {
//
//                            broadcastAddresses = listAllBroadcastAddresses();
//                            if(ownClient!=null)
//                                ownClient.setMyIps(getMyIps());
//                        } catch (SocketException e) {
//                        }
//
//                        for (InetAddress address : broadcastAddresses) {
//                            try {
//                                DatagramPacket packet = new DatagramPacket(
//                                        buffer, buffer.length, address, 11001
//                                );
//                                datagramSocket.send(packet);
//
//                            } catch (IOException e) {
//                                e.printStackTrace();
//
//                            }
//                        }
//
//                        try {
//                            Thread.sleep(500);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    datagramSocket.close();
//                    threadShutdown = true;
//                }
//
//            });
//            broadcastThread.start();
//
//        } catch (SocketException e) {
//            System.out.println("Broadcast Socket exception");
//        }
//
//
//    }
//
//    List<InetAddress> listAllBroadcastAddresses() throws SocketException {
//        List<InetAddress> broadcastList = new ArrayList<>();
//        Enumeration<NetworkInterface> interfaces
//                = NetworkInterface.getNetworkInterfaces();
//        while (interfaces.hasMoreElements()) {
//            NetworkInterface networkInterface = interfaces.nextElement();
//
//            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
//                continue;
//            }
//
//            networkInterface.getInterfaceAddresses().stream()
//                    .map(a -> a.getBroadcast())
//                    .filter(Objects::nonNull)
//                    .forEach(broadcastList::add);
//        }
//        return broadcastList;
//    }
//
//    public ArrayList<String> getMyIps()
//    {
//        ArrayList<String> localhosts = new ArrayList<>();
//        try {
//            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
//            while (interfaces.hasMoreElements()) {
//                NetworkInterface iface = interfaces.nextElement();
//                // filters out 127.0.0.1 and inactive interfaces
//                if (iface.isLoopback() || !iface.isUp() || !iface.supportsMulticast())
//                    continue;
//
//                Enumeration<InetAddress> addresses = iface.getInetAddresses();
//                while(addresses.hasMoreElements()) {
//                    InetAddress addr = addresses.nextElement();
//
//                    // *EDIT*
//                    if (addr instanceof Inet6Address) continue;
//
//                    localhosts.add( addr.getHostAddress());
//                }
//            }
//        } catch (SocketException e) {
//            System.out.println("Problem in Localhost Detection");
//        }
//        return localhosts;
//    }


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

                String clientName = client.getName();

                if (!clientName.equals(doNotBroadcastToClient)) {
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

                String clientName = client.getName();

                if (!clientName.equals(doNotBroadcastToClient)) {
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
        try {
            broadcastThread.join();
        } catch (InterruptedException e) {
        }
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
