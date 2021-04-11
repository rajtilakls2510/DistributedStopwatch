package rmi.server;

import main.ApplicationController;
import main.InstanceInfo;
import rmi.client.Client;
import stopwatch.VirtualStopwatch;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class RMIServer implements Server {

    transient ArrayList<ClientDecorator> clients;
    public InstanceInfo instanceInfo;
    transient ApplicationController context;
    transient boolean shutdown;

    public RMIServer(ApplicationController context) throws RemoteException {
        this.instanceInfo = ApplicationController.instanceInfo;
        this.context = context;
        clients = new ArrayList<>();
        shutdown = false;
        UnicastRemoteObject.exportObject(this, 0);
    }

    @Override
    public InstanceInfo getInstanceInfo() throws RemoteException {
        return instanceInfo;
    }

    @Override
    public void registerClient(Client client, InstanceInfo clientInfo) throws RemoteException {
        clients.add(new ClientDecorator(client, clientInfo));
    }

    @Override
    public void unRegisterClient(InstanceInfo clientInfo) throws RemoteException {
        clients.removeIf(client -> client.getInstanceInfo().getInstanceIdentifier().equals(clientInfo.getInstanceIdentifier()));
    }

    @Override
    public VirtualStopwatch getOwnerStopwatchInstance() throws RemoteException {
        return new StopwatchDecoratorForServer(context.ownerStopwatchInstance);
    }

    @Override
    public String getOwnerStopwatchState() throws RemoteException {
        return context.ownerStopwatchInstance.getPreviousStateName();
    }

    public void notifyClients(long time) {
        try {
            for (ClientDecorator client : clients) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            client.getClient().onTimeUpdated(time, instanceInfo);
                        } catch (RemoteException e) {
                        }
                    }
                }).start();

            }
        } catch (Exception ignored) {
        }
    }

    public void notifyStartPauseResumePressed() {
        try {
            for (ClientDecorator client : clients) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            client.getClient().onStartPauseResumePressed(instanceInfo);
                        } catch (RemoteException e) {
                        }
                    }
                }).start();

            }
        } catch (Exception ignored) {
        }
    }

    public void notifyStartPauseResumePressed(InstanceInfo doNotBroadcastToClient) {
        try {
            for (ClientDecorator client : clients) {

                String clientIdentifier = client.getInstanceInfo().getInstanceIdentifier();
                if (!clientIdentifier.equals(doNotBroadcastToClient.getInstanceIdentifier())) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                client.getClient().onStartPauseResumePressed(instanceInfo);
                            } catch (RemoteException e) {
                            }
                        }
                    }).start();

                }
            }
        } catch (Exception ignored) {
        }
    }

    public void notifyStopPressed() {
        try {
            for (ClientDecorator client : clients) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            client.getClient().onStopPressed(instanceInfo);
                        } catch (RemoteException e) {
                        }
                    }
                }).start();

            }
        } catch (Exception ignored) {
        }
    }

    public void notifyStopPressed(InstanceInfo doNotBroadcastToClient) {
        try {
            for (ClientDecorator client : clients) {

                String clientIdentifier = client.getInstanceInfo().getInstanceIdentifier();

                if (!clientIdentifier.equals(doNotBroadcastToClient.getInstanceIdentifier())) {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                client.getClient().onStopPressed(instanceInfo);
                            } catch (RemoteException e) {
                            }
                        }
                    }).start();

                }
            }
        } catch (Exception ignored) {
        }
    }
    public void unRegisterAllClients()
    {
        try {
            for (ClientDecorator client : clients) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            client.getClient().onServerShutdown(instanceInfo);
                        } catch (Exception e) {
                        }
                    }
                }).start();

            }
        } catch (Exception ignored) {
        }
    }

    public void shutdown() {
        shutdown = true;
        unRegisterAllClients();
    }

}
