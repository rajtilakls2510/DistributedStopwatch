package rmi.server;

import main.ApplicationController;
import rmi.client.Client;
import rmi.virtualstopwatch.StopwatchDecoratorForServer;
import stopwatch.VirtualStopwatch;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class RMIServer implements Server {

    transient ArrayList<ClientDecorator> clients;
    public String name;
    public String identifier;
    transient ApplicationController context;
    transient boolean shutdown;

    public RMIServer(String identifier, ApplicationController context) throws RemoteException {
        name = ApplicationController.SERVER_NAME;
        this.identifier = identifier;
        this.context = context;
        clients = new ArrayList<>();
        shutdown = false;
        UnicastRemoteObject.exportObject(this, 0);
    }

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
                            client.getClient().onTimeUpdated(time, identifier);
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
                            client.getClient().onStartPauseResumePressed(identifier);
                        } catch (RemoteException e) {
                        }
                    }
                }).start();

            }
        } catch (Exception ignored) {
        }
    }

    public void notifyStartPauseResumePressed(String doNotBroadcastToClient) {
        try {
            for (ClientDecorator client : clients) {

                String clientIdentifier = client.getIdentifier();

                if (!clientIdentifier.equals(doNotBroadcastToClient)) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                client.getClient().onStartPauseResumePressed(identifier);
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
                            client.getClient().onStopPressed(identifier);
                        } catch (RemoteException e) {
                        }
                    }
                }).start();

            }
        } catch (Exception ignored) {
        }
    }

    public void notifyStopPressed(String doNotBroadcastToClient) {
        try {
            for (ClientDecorator client : clients) {

                String clientIdentifier = client.getIdentifier();

                if (!clientIdentifier.equals(doNotBroadcastToClient)) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                client.getClient().onStopPressed(identifier);
                            } catch (RemoteException e) {
                            }
                        }
                    }).start();

                }
            }
        } catch (Exception ignored) {
        }
    }

    public void shutdown() {
        shutdown = true;
        try {
            for (ClientDecorator client : clients) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            client.getClient().onServerShutdown(identifier);
                        } catch (Exception e) {
                        }
                    }
                }).start();

            }
        } catch (Exception ignored) {
        }
    }

}
