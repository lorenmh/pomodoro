package io.plutonium.pomodoro;

/**
 * Created by loren on 1/21/15.
 */
public interface Timeable {
    public void setCountDown(boolean bool);
    public long timeElapsed();
    public boolean completed();
    public String timeElapsedToString();
    public String timeCompleted();
}
