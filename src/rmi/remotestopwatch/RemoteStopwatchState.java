package rmi.remotestopwatch;

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

    interface RemoteStopwatchStateUser
    {


        /**
         * RemoteStopwatchStateUser is the interface which is used to provide some common functionality which the Individual StopwatchStates are going
         * to use when implementing the State Pattern. Therefore, every Stopwatch class that wants to use the state pattern of StopwatchState must
         * implement this nested interface.
         */

        public RemoteStopwatchState getCurrentState();

        public void setCurrentState(RemoteStopwatchState stopwatchState);
    }
}
