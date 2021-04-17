package rmi.indexer;

import main.InstanceInfo;
import rmi.client.Client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IndexServer extends Remote {

    /**
     * IndexServer is the interface which is used to define common methods that will be used by a Peer to contact with the Index Server
     */

    // This method is used by a peer to register to this server
    void registerPeer(Client client, InstanceInfo peerInfo) throws RemoteException;

    // This method is used by a peer to unregister from this server
    void unRegisterPeer(InstanceInfo peerInfo) throws RemoteException;

    // This method is used by a peer to get all servers present in this server
    List<InstanceInfo> getAllPeers() throws RemoteException;

}
