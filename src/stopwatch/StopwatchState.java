package stopwatch;

public interface StopwatchState {

    /**
     * StopwatchState is the common abstraction over all the states that can be there for a Stopwatch.
     * This class is the part of the State Pattern.
     */

    default void execute() {
        handleStopwatch();
        handleUI();
        changeState();
    }

    public void handleStopwatch();

    public void handleUI();

    public void changeState();

    String getName();
}
