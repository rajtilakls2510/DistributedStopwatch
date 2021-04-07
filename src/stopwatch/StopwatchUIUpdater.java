package stopwatch;

public interface StopwatchUIUpdater {
    void onStart();

    void onPause();

    void onResume();

    void onStop();

    void onTimeUpdate(long time);
}
