package rmi.remotestopwatch;

import stopwatch.StopwatchUIUpdater;

public class InitializerRemoteStopwatchState implements RemoteStopwatchState {

    /**
     * This state should be used for initializing the remote stopwatch and the UI
     */

    RemoteStopwatch stopwatch;
    StopwatchUIUpdater stopwatchUIUpdater;
    String name;

    public InitializerRemoteStopwatchState(RemoteStopwatch stopwatch, StopwatchUIUpdater stopwatchUIUpdater, String name) {
        this.stopwatch = stopwatch;
        this.stopwatchUIUpdater = stopwatchUIUpdater;
        this.name = name;
    }

    @Override
    public void handleUI() {
        stopwatchUIUpdater.onStop();
    }

    @Override
    public void changeState() {
        stopwatch.setCurrentState(stopwatch.getNotRunningState());
    }

    @Override
    public String getName() {
        return name;
    }
}
