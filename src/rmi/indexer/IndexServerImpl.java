package rmi.indexer;

import main.Indexer;
import main.InstanceInfo;
import rmi.client.Client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class IndexServerImpl implements IndexServer {

    /**
     * IndexServerImpl provides the implementation for IndexServer interface.
     * <p>
     * This class is responsible for storing the information about the peers registered to it.
     */

    // List of all active peers
    private ArrayList<PeerDecorator> activePeers;

    public IndexServerImpl() throws RemoteException {
        activePeers = new ArrayList<>();
        UnicastRemoteObject.exportObject(this, 0);
    }

    // <------------------------------- Interface methods ------------------------->

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

    /**
     * This method is used for garbage collection. This method pings the peers and filters the inactive peers and notifies
     * all active clients about the inactive peers.
     */
    public void filterInactivePeers() {

        ArrayList<PeerDecorator> inactivePeers = new ArrayList<>();
        ArrayList<Thread> inactiveDetectorThread = new ArrayList<>();

        // Ping all Peers present in the server
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

        // Wait for all the responses to come
        for (Thread thread : inactiveDetectorThread) {
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
        }

        // Notify all active peers about the inactive peers
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
