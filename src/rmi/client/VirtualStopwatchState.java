package rmi.client;

public interface VirtualStopwatchState {

    default void execute() {
        handleUI();
        changeState();
    }

    public void handleUI();

    public void changeState();

    public String getName();
}
