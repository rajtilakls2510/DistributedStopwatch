package rmi.virtualstopwatch;

import stopwatch.VirtualStopwatch;
import stopwatch.Stopwatch;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class VirtualStopwatchServer implements VirtualStopwatch {


    transient Stopwatch sw;

    public VirtualStopwatchServer(Stopwatch sw) throws RemoteException {
        this.sw = sw;
        UnicastRemoteObject.exportObject(this, 0);
    }

    @Override
    public void startPauseResume() throws RemoteException {
        sw.startPauseResume();
    }

    @Override
    public void stop() throws RemoteException {
        sw.stop();
    }

    @Override
    public long getTime() throws RemoteException {
        return sw.getTime();
    }

    @Override
    public void remoteStartPressed(String clientName) throws RemoteException {
        sw.remoteStartPressed(clientName);
    }

    @Override
    public void remoteStopPressed(String clientName) throws RemoteException {
        sw.remoteStopPressed(clientName);
    }


}
