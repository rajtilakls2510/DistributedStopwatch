package stopwatch;

public class PausedStopwatchState implements StopwatchState {

    /**
     * The Stopwatch is in this state when the Pause button is pressed
     */

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
        stopwatch.getStopwatchEngine().start();
    }

    @Override
    public void handleUI() {
        stopwatchUIUpdater.onResume();
    }

    @Override
    public void changeState() {
        stopwatch.setPreviousState(this);

        // Changes to RunningState
        stopwatch.setCurrentState(stopwatch.getRunningState());
    }

    @Override
    public String getName() {
        return name;
    }
}
