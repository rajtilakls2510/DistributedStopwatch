package rmi.virtualstopwatch;

public interface RemoteStopwatchState {

    /**
     * RemoteStopwatchState is the common abstraction over all the states that can be there for RemoteStopwatch class.
     * This class is the part of the State Pattern.
     */

    default void execute() {
        handleUI();
        changeState();
    }

    // Method which changes the UI on State transition
    public void handleUI();

    // Method which changes the state
    public void changeState();

    public String getName();
}
