package stopwatch;

import java.io.Serializable;

public interface StopwatchState {

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
