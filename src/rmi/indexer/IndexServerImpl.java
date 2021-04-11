package rmi.indexer;

import main.InstanceInfo;
import rmi.client.Client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class IndexServerImpl implements IndexServer {

    private ArrayList<PeerDecorator> activePeers;

    public IndexServerImpl() throws RemoteException {
        activePeers = new ArrayList<>();
        UnicastRemoteObject.exportObject(this, 0);
    }

    @Override
    public void registerPeer(Client client, InstanceInfo peerInfo) throws RemoteException {

        activePeers.removeIf(peerDecorator -> peerDecorator.getInstanceInfo().getInstanceIdentifier().equals(peerInfo.getInstanceIdentifier()));

        activePeers.add(new PeerDecorator(client, peerInfo));
        if(activePeers.size()>0)
        {
            for(PeerDecorator peer: activePeers)
            {
                peer.getClient().onNewPeer(peerInfo);
            }
        }
        printPeers();
    }

    @Override
    public void unRegisterPeer(InstanceInfo peerInfo) throws RemoteException {
        activePeers.removeIf(peerDecorator ->  peerDecorator.getInstanceInfo().getInstanceIdentifier().equals(peerInfo.getInstanceIdentifier()));
        printPeers();
    }

    @Override
    public List<InstanceInfo> getAllPeers() throws RemoteException {
        ArrayList<InstanceInfo> peerInfos = new ArrayList<>();
        for (PeerDecorator peerDecorator: activePeers)
            peerInfos.add(peerDecorator.getInstanceInfo());

        return peerInfos;
    }

    public void printPeers()
    {
        System.out.println("\nPeers: ");
        for (PeerDecorator peerDecorator: activePeers)
        {
            System.out.print(peerDecorator.getInstanceInfo().getInstanceIdentifier() + "\t");
        }

    }
}
