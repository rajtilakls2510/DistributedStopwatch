package rmi.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VirtualStopwatch extends Remote {


    void startPauseResume() throws RemoteException;

    void stop() throws RemoteException;

    long getTime() throws RemoteException;

    void remoteStartPressed(String name) throws RemoteException;

    void remoteStopPressed(String name) throws RemoteException;


}
