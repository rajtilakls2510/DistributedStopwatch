package rmi.client;

import main.InstanceInfo;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Client extends Remote {
    void onTimeUpdated(long time, InstanceInfo serverInfo) throws RemoteException;

    void onStartPauseResumePressed(InstanceInfo serverInfo) throws RemoteException;

    void onStopPressed(InstanceInfo serverInfo) throws RemoteException;

    void onServerShutdown(InstanceInfo serverInfo) throws RemoteException;

    void onNewPeer(InstanceInfo peerInfo) throws RemoteException;

    void onPeerClose(InstanceInfo peerInfo) throws RemoteException;

    void onPing() throws RemoteException;
}
