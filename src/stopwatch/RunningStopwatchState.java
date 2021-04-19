package stopwatch;

public class RunningStopwatchState implements StopwatchState {

    /**
     * The Stopwatch is in this state when the Start/Resume button is pressed
     */

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
        stopwatch.getStopwatchEngine().stop();
    }


    @Override
    public void handleUI() {
        stopwatchUIUpdater.onPause();
    }

    @Override
    public void changeState() {

        stopwatch.setPreviousState(this);

        // Changes to PausedState
        stopwatch.setCurrentState(stopwatch.getPausedState());
    }

    @Override
    public String getName() {
        return name;
    }
}
