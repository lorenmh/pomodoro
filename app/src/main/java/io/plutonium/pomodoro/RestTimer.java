package io.plutonium.pomodoro;

import android.util.Log;

/**
 * Created by loren on 1/18/15.
 */
public class RestTimer implements Timeable {
    private long mStartTime = 0;
    private long mSavedTime = 0;

    private boolean countDown = false;

    private static final int END_TIME_MINUTES = 5;

    public RestTimer(boolean aCountDown) {
        countDown = aCountDown;
        mStartTime = System.currentTimeMillis();
    }

    public RestTimer(long savedTime, boolean aCountDown) {
        countDown = aCountDown;
        mStartTime = System.currentTimeMillis();
        mSavedTime = savedTime;
    }

    public void setCountDown(boolean bool) {
        countDown = bool;
    }

    public long timeElapsed() {
        long time = System.currentTimeMillis() - mStartTime + mSavedTime;
        if (time >= endTimeMs()) {
            return endTimeMs();
        } else {
            return time;
        }
    }

    private long endTimeMs() {
        return END_TIME_MINUTES * 60 * 1000;
    }

    public boolean completed() {
        return timeElapsed() >= endTimeMs();
    }

    public String timeCompleted() {
        if (countDown) {
            return "00:00";
        } else {
            return String.format("%02d:00", END_TIME_MINUTES);
        }
    }

    public String timeElapsedToString() {
        if (completed()) {
            return timeCompleted();
        } else {
            long timeElapsedMs = timeElapsed();
            if (countDown) {
                timeElapsedMs = endTimeMs() - timeElapsedMs;
            }
            int totalSeconds = (int) (timeElapsedMs / 1000);
            int seconds = (int) totalSeconds % 60;
            int minutes = totalSeconds / 60;
            Log.d("time elapsed", String.valueOf(timeElapsed()));
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}
