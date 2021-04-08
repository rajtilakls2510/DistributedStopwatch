package rmi.client;

import rmi.shared.VirtualStopwatch;
import stopwatch.StopwatchUIUpdater;
import ui.ApplicationController;

import java.rmi.RemoteException;

public class VirtualStopwatchClientImpl implements VirtualStopwatchClient {
    VirtualStopwatch sw;


    VirtualStopwatchState notRunningState;
    VirtualStopwatchState runningState;
    VirtualStopwatchState pausedState;
    VirtualStopwatchState stopPressedState;

    VirtualStopwatchState currentState;

    StopwatchUIUpdater stopwatchUIUpdater;
    String previousState;

    String clientIdentifier;

    public VirtualStopwatchClientImpl(VirtualStopwatch stopwatch, String previousState, String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
        sw = stopwatch;
        this.previousState = previousState;

    }

    private void decodeState(String previousState) {
        if (previousState.equals(pausedState.getName()))
            currentState = pausedState;

        else if (previousState.equals(notRunningState.getName()))
            currentState = notRunningState;

        else if (previousState.equals(runningState.getName()))
            currentState = runningState;

        else
            currentState = stopPressedState;

        currentState.execute();
        stopwatchUIUpdater.onTimeUpdate(getTime());
    }

    @Override
    public void startPauseResume() {
        try {
            sw.remoteStartPressed(ApplicationController.hostname);
            currentState.execute();
        } catch (RemoteException e) {
        }
    }

    @Override
    public void stop() {
        try {
            sw.remoteStopPressed(ApplicationController.hostname);
            currentState = stopPressedState;
            currentState.execute();
        } catch (RemoteException e) {

        }
    }

    @Override
    public long getTime() {
        try {
            return sw.getTime();
        } catch (RemoteException e) {
        }
        return 0L;
    }

    @Override
    public void setStopwatchUiUpdater(StopwatchUIUpdater stopwatchUIUpdater) {
        this.stopwatchUIUpdater = stopwatchUIUpdater;
        notRunningState = new NotRunningStopwatchState(this, this.stopwatchUIUpdater, "NOT_RUNNING");
        runningState = new RunningStopwatchState(this, this.stopwatchUIUpdater, "RUNNING");
        pausedState = new PausedStopwatchState(this, this.stopwatchUIUpdater, "PAUSED");
        stopPressedState = new InitializerStopwatchState(this, this.stopwatchUIUpdater, "STOP_PRESSED");
        currentState = stopPressedState;

        currentState.execute();
        decodeState(previousState);
    }

    @Override
    public void remoteStartPressed(String serverName) {
        currentState.execute();
    }

    @Override
    public void remoteStopPressed(String serverName) {
        currentState = stopPressedState;
        currentState.execute();
    }

    @Override
    public void remoteOnTimeUpdated(long time) {
        stopwatchUIUpdater.onTimeUpdate(time);
    }


    public String getName() {
        return clientIdentifier;
    }


    public void setState(VirtualStopwatchState stopwatchState) {
        currentState = stopwatchState;
    }

    public VirtualStopwatchState getNotRunningState() {
        return notRunningState;
    }

    public VirtualStopwatchState getRunningState() {
        return runningState;
    }

    public VirtualStopwatchState getPausedState() {
        return pausedState;
    }

    public VirtualStopwatchState getCurrentState() {
        return currentState;
    }
}
