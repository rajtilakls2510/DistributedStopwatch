package rmi.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Client extends Remote {
    void onTimeUpdated(long time, String serverIdentifier) throws RemoteException;

    void onStartPauseResumePressed(String serverIdentifier) throws RemoteException;

    void onStopPressed(String serverIdentifier) throws RemoteException;


    void onServerShutdown(String serverIdentifier) throws RemoteException;
}
