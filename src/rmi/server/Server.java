package rmi.server;

import main.InstanceInfo;
import rmi.client.Client;
import stopwatch.VirtualStopwatch;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Server extends Remote {

    InstanceInfo getInstanceInfo() throws RemoteException;

    void registerClient(Client client, InstanceInfo clientInfo) throws RemoteException;

    void unRegisterClient(InstanceInfo clientInfo) throws RemoteException;

    VirtualStopwatch getOwnerStopwatchInstance() throws RemoteException;

    String getOwnerStopwatchState() throws RemoteException;

}
