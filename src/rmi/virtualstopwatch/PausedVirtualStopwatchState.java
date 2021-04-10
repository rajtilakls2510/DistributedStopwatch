package rmi.virtualstopwatch;

import stopwatch.StopwatchUIUpdater;

public class PausedVirtualStopwatchState implements VirtualStopwatchState {
    RemoteStopwatch stopwatch;
    StopwatchUIUpdater stopwatchUIUpdater;
    String name;

    public PausedVirtualStopwatchState(RemoteStopwatch stopwatch, StopwatchUIUpdater stopwatchUIUpdater, String name) {
        this.stopwatch = stopwatch;
        this.stopwatchUIUpdater = stopwatchUIUpdater;
        this.name = name;
    }


    @Override
    public void handleUI() {
        stopwatchUIUpdater.onResume();
    }

    @Override
    public void changeState() {
        stopwatch.setState(stopwatch.getRunningState());
    }

    @Override
    public String getName() {
        return name;
    }
}
