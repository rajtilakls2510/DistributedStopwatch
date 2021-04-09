package rmi.indexer;

import rmi.client.Client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class IndexServerImpl implements IndexServer {

    private ArrayList<PeerDecorator> activePeers;

    public IndexServerImpl() throws RemoteException {
        activePeers = new ArrayList<>();
        UnicastRemoteObject.exportObject(this, 1100);
    }

    @Override
    public void registerPeer(Client client, String ip) throws RemoteException {

        activePeers.removeIf(peerDecorator -> peerDecorator.getIp().equals(ip));

        activePeers.add(new PeerDecorator(client, ip));
        if(activePeers.size()>0)
        {
            for(PeerDecorator peer: activePeers)
            {
                peer.getClient().onNewPeer(ip);
            }
        }
//        printPeers();
    }

    @Override
    public void unRegisterPeer(String ip) throws RemoteException {
        activePeers.removeIf(peerDecorator ->  peerDecorator.getIp().equals(ip));
//        printPeers();
    }

    @Override
    public List<String> getAllPeers() throws RemoteException {
        ArrayList<String> peerIps = new ArrayList<>();
        for (PeerDecorator peerDecorator: activePeers)
            peerIps.add(peerDecorator.getIp());

        return peerIps;
    }

    public void printPeers()
    {
        System.out.println("\nPeers: ");
        for (PeerDecorator peerDecorator: activePeers)
        {
            System.out.print(peerDecorator.getIp() + "\t");
        }

    }
}
