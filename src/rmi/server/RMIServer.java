package rmi.server;

import main.ApplicationController;
import main.InstanceInfo;
import rmi.client.Client;
import stopwatch.VirtualStopwatch;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class RMIServer implements Server {

    /**
     * This is the main server for this Application. All the broadcast information goes through this class.
     */

    // List hold all the registered clients
    transient ArrayList<ClientDecorator> clients;

    // Hold the own instance info
    public InstanceInfo instanceInfo;

    // Holds the Application context
    transient ApplicationController context;

    // Boolean to indicate whether the application is shutting down or not
    transient boolean shutdown;

    public RMIServer(ApplicationController context) throws RemoteException {
        this.instanceInfo = ApplicationController.instanceInfo;
        this.context = context;
        clients = new ArrayList<>();
        shutdown = false;
        UnicastRemoteObject.exportObject(this, 0);
    }

    // <------------------------- Interface methods --------------------->

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

    // <----------------------------- Stopwatch Information/State Broadcast methods ----------------------------->

    /**
     * This method notifies all clients with the new time of own stopwatch
     *
     * @param time
     */
    public void notifyClients(long time) {
        try {
            for (ClientDecorator client : clients) {
                ApplicationController.networkThreadPool.submit(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    client.getClient().onTimeUpdated(time, instanceInfo);
                                } catch (RemoteException e) {
                                }
                            }
                        });

            }
        } catch (Exception ignored) {
        }
    }

    /**
     * This method notifies all clients when the start/pause/resume button is pressed
     */
    public void notifyStartPauseResumePressed() {
        try {
            for (ClientDecorator client : clients) {
                ApplicationController.networkThreadPool.submit(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    client.getClient().onStartPauseResumePressed(instanceInfo);
                                } catch (RemoteException e) {
                                }
                            }
                        });

            }
        } catch (Exception ignored) {
        }
    }

    /**
     * This method notifies all clients except othe one passed as argument when the start/pause/resume button is pressed
     *
     * @param doNotBroadcastToClient
     */
    public void notifyStartPauseResumePressed(InstanceInfo doNotBroadcastToClient) {
        try {
            for (ClientDecorator client : clients) {

                String clientIdentifier = client.getInstanceInfo().getInstanceIdentifier();
                if (!clientIdentifier.equals(doNotBroadcastToClient.getInstanceIdentifier())) {
                    ApplicationController.networkThreadPool.submit(
                            new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        client.getClient().onStartPauseResumePressed(instanceInfo);
                                    } catch (RemoteException e) {
                                    }
                                }
                            });

                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * This method notifies all clients when the stop button is pressed
     */
    public void notifyStopPressed() {
        try {
            for (ClientDecorator client : clients) {
                ApplicationController.networkThreadPool.submit(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    client.getClient().onStopPressed(instanceInfo);
                                } catch (RemoteException e) {
                                }
                            }
                        });

            }
        } catch (Exception ignored) {
        }
    }

    /**
     * This method notifies all clients except othe one passed as argument when the stop button is pressed
     *
     * @param doNotBroadcastToClient
     */
    public void notifyStopPressed(InstanceInfo doNotBroadcastToClient) {
        try {
            for (ClientDecorator client : clients) {

                String clientIdentifier = client.getInstanceInfo().getInstanceIdentifier();

                if (!clientIdentifier.equals(doNotBroadcastToClient.getInstanceIdentifier())) {
                    ApplicationController.networkThreadPool.submit(
                            new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        client.getClient().onStopPressed(instanceInfo);
                                    } catch (RemoteException e) {
                                    }
                                }
                            });

                }
            }
        } catch (Exception ignored) {
        }
    }

    // <---------------------- shutdown methods -------------------->

    /**
     * This method unregisters from all clients. Mainly used when the server is about to shutdown.
     */
    public void unRegisterAllClients() {
        try {
            for (ClientDecorator client : clients) {
                ApplicationController.networkThreadPool.submit(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    client.getClient().onServerShutdown(instanceInfo);
                                } catch (Exception e) {
                                }
                            }
                        });

            }
        } catch (Exception ignored) {
        }
    }

    /**
     * This method shuts down the server by unregistering from all remote clients
     */
    public void shutdown() {
        shutdown = true;
        unRegisterAllClients();
    }

}
