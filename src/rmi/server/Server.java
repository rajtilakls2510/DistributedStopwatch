package rmi.server;

import rmi.client.Client;
import stopwatch.VirtualStopwatch;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Server extends Remote {

    String getIdentifier() throws RemoteException;

    void registerClient(Client client, String clientIdentifier) throws RemoteException;

    void unRegisterClient(String clientIdentifier) throws RemoteException;

    VirtualStopwatch getOwnerStopwatchInstance() throws RemoteException;

    String getOwnerStopwatchState() throws RemoteException;

}
