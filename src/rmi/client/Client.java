package rmi.client;

import main.InstanceInfo;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Client extends Remote {

    /**
     * Client is the interface for the main Client of this application which registers to a remote instance's server
     */

    // This method is called by the server when new time is received for its stopwatch
    void onTimeUpdated(long time, InstanceInfo serverInfo) throws RemoteException;

    // This method is called by the server when the start/pause/resume button is pressed for its stopwatch
    void onStartPauseResumePressed(InstanceInfo serverInfo) throws RemoteException;

    // This method is called by the server when the stop button is pressed for its stopwatch
    void onStopPressed(InstanceInfo serverInfo) throws RemoteException;

    // This method is called by the server it wants to shutdown thereby notifying the client through this method
    void onServerShutdown(InstanceInfo serverInfo) throws RemoteException;

    // This method is called by the Index Server when a new peer is registered to the index server
    void onNewPeer(InstanceInfo peerInfo) throws RemoteException;

    // This method is called by the Index Server when a peer is unregistered from the server
    void onPeerClose(InstanceInfo peerInfo) throws RemoteException;

    // This method is called by the Index Server when it wants to see if the peer is alive or not
    void onPing() throws RemoteException;
}
