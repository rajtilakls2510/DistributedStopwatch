package stopwatch;

import java.io.Serializable;

public interface StopwatchUIUpdater extends Serializable {

    /**
     * This interface is used to provide the implementation of the UI updates when start, pause, resume and stop buttons are pressed
     * and when the time updates.
     */

    void onStart();

    void onPause();

    void onResume();

    void onStop();

    void onTimeUpdate(long time);
}
