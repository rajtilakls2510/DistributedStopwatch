package rmi.virtualstopwatch;

import stopwatch.StopwatchUIUpdater;

public class InitializerVirtualStopwatchState implements VirtualStopwatchState {
    RemoteStopwatch stopwatch;
    StopwatchUIUpdater stopwatchUIUpdater;
    String name;

    public InitializerVirtualStopwatchState(RemoteStopwatch stopwatch, StopwatchUIUpdater stopwatchUIUpdater, String name) {
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
        stopwatch.setState(stopwatch.getNotRunningState());
    }

    @Override
    public String getName() {
        return name;
    }
}
