package rmi.client;

import stopwatch.StopwatchUIUpdater;

public class RunningVirtualStopwatchState implements VirtualStopwatchState {
    VirtualStopwatchClientImpl stopwatch;
    StopwatchUIUpdater stopwatchUIUpdater;
    String name;

    public RunningVirtualStopwatchState(VirtualStopwatchClientImpl stopwatch, StopwatchUIUpdater stopwatchUIUpdater, String name) {
        this.stopwatch = stopwatch;
        this.stopwatchUIUpdater = stopwatchUIUpdater;
        this.name = name;
    }

    @Override
    public void handleUI() {
        stopwatchUIUpdater.onPause();
    }

    @Override
    public void changeState() {
        stopwatch.setState(stopwatch.getPausedState());
    }

    @Override
    public String getName() {
        return name;
    }
}
