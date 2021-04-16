package rmi.indexer;

import main.Indexer;
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
        for (PeerDecorator peer : activePeers) {
            Indexer.networkThreadPool.submit(
                    new Runnable() {
                        @Override
                        public void run() {

                            try {
                                peer.getClient().onNewPeer(peerInfo);
                            } catch (RemoteException ignored) {
                            }
                        }
                    });
        }

        printPeers();
    }

    @Override
    public void unRegisterPeer(InstanceInfo peerInfo) throws RemoteException {
        activePeers.removeIf(peerDecorator -> peerDecorator.getInstanceInfo().getInstanceIdentifier().equals(peerInfo.getInstanceIdentifier()));
        printPeers();
    }

    @Override
    public List<InstanceInfo> getAllPeers() throws RemoteException {
        ArrayList<InstanceInfo> peerInfos = new ArrayList<>();
        for (PeerDecorator peerDecorator : activePeers)
            peerInfos.add(peerDecorator.getInstanceInfo());

        return peerInfos;
    }

    @Override
    public void filterInactivePeers() throws RemoteException {

        ArrayList<PeerDecorator> inactivePeers = new ArrayList<>();
        ArrayList<Thread> inactiveDetectorThread = new ArrayList<>();
        for (PeerDecorator peer : activePeers) {

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        peer.getClient().onPing();
                    } catch (RemoteException e) {
                        try {
                            unRegisterPeer(peer.getInstanceInfo());
                            inactivePeers.add(peer);
                        } catch (RemoteException ignored) {
                        }
                    }
                }
            });
            thread.start();
            inactiveDetectorThread.add(thread);
        }

        for (Thread thread : inactiveDetectorThread) {
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
        }

        for (PeerDecorator inactivePeer : inactivePeers) {
            for (PeerDecorator activePeer : activePeers) {
                Indexer.networkThreadPool.submit(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    activePeer.getClient().onPeerClose(inactivePeer.getInstanceInfo());
                                } catch (RemoteException ignored) {
                                }

                            }
                        });
            }
        }

    }

    public void printPeers() {
        System.out.println("\nPeers: ");
        for (PeerDecorator peerDecorator : activePeers) {
            System.out.print(peerDecorator.getInstanceInfo().getInstanceIdentifier() + "\t");
        }

    }
}
