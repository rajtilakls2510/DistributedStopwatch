package stopwatch;

public class PausedStopwatchState implements StopwatchState {
    Stopwatch stopwatch;
    StopwatchUIUpdater stopwatchUIUpdater;
    String name;

    public PausedStopwatchState(Stopwatch stopwatch, StopwatchUIUpdater stopwatchUIUpdater, String name) {
        this.stopwatch = stopwatch;
        this.stopwatchUIUpdater = stopwatchUIUpdater;
        this.name = name;
    }

    @Override
    public void handleStopwatch() {
        stopwatch.sw.start();
    }

    @Override
    public void handleUI() {
        stopwatchUIUpdater.onResume();
    }

    @Override
    public void changeState() {
        stopwatch.setPreviousState(this);
        stopwatch.setState(stopwatch.getRunningState());
    }

    @Override
    public String getName() {
        return name;
    }
}
