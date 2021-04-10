package stopwatch;

import java.io.Serializable;

public interface StopwatchUIUpdater extends Serializable {
    void onStart();

    void onPause();

    void onResume();

    void onStop();

    void onTimeUpdate(long time);
}
