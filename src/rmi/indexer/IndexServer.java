package rmi.indexer;

import main.InstanceInfo;
import rmi.client.Client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IndexServer extends Remote {

    void registerPeer(Client client, InstanceInfo peerInfo) throws RemoteException;

    void unRegisterPeer(InstanceInfo peerInfo) throws RemoteException;

    List<InstanceInfo> getAllPeers() throws RemoteException;

}
