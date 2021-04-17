package rmi.server;

import main.InstanceInfo;
import rmi.client.Client;
import stopwatch.VirtualStopwatch;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Server extends Remote {

    /**
     * Server is the interface for the main server of this application.
     */

    // Method to return instance information about this instance
    InstanceInfo getInstanceInfo() throws RemoteException;

    // Method used by clients to register to a server
    void registerClient(Client client, InstanceInfo clientInfo) throws RemoteException;

    // Method used by clients to unregister from the server
    void unRegisterClient(InstanceInfo clientInfo) throws RemoteException;

    // Method used by clients to get the main Stopwatch instance of this instance
    VirtualStopwatch getOwnerStopwatchInstance() throws RemoteException;

    /* Method used by clients to get the state of the stopwatch.
        The name of the state is sent in String format because it is serializable
     */
    String getOwnerStopwatchState() throws RemoteException;

}
