package rmi.indexer;

import rmi.client.Client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IndexServer extends Remote {

    void registerPeer(Client client, String ip) throws RemoteException;

    void unRegisterPeer(String ip) throws RemoteException;

    List<String> getAllPeers() throws RemoteException;

}
