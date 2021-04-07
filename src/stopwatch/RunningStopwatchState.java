package stopwatch;

public class RunningStopwatchState implements StopwatchState {
    Stopwatch stopwatch;
    StopwatchUIUpdater stopwatchUIUpdater;
    String name;

    public RunningStopwatchState(Stopwatch stopwatch, StopwatchUIUpdater stopwatchUIUpdater, String name) {
        this.stopwatch = stopwatch;
        this.stopwatchUIUpdater = stopwatchUIUpdater;
        this.name = name;
    }

    @Override
    public void handleStopwatch() {
        stopwatch.sw.stop();
    }


    @Override
    public void handleUI() {
        stopwatchUIUpdater.onPause();
    }

    @Override
    public void changeState() {

        stopwatch.setPreviousState(this);
        stopwatch.setState(stopwatch.getPausedState());
    }

    @Override
    public String getName() {
        return name;
    }
}
