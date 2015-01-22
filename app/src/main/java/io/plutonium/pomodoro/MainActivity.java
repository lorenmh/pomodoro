package io.plutonium.pomodoro;

import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {
    TextView mPhaseText;
    TextView mTimerTextView;
    LinearLayout mLayout;
    long savedTime = 0;

    Timeable timer;
    boolean phasePomodoro;
    boolean promptPhaseChange;

    Button mStartButton;
    Button mStopButton;
    Button mRestartButton;
    Button mCountdownButton;

    boolean mCurrentlyCountingDown = false;
    boolean mCurrentlyRunning = false;

    Handler mTimerHandler = new Handler();
    final static int NORMAL_TIMER_DELAY = 100;
    final static int BACKGROUND_TIMER_DELAY = 10000;

    int currentDelay = NORMAL_TIMER_DELAY;

    Runnable mTimerRunnable = new Runnable() {
        @Override
        public void run() {
            if (timer.completed()) {
                setTimerTextCompleted();
                promptPhaseChange();
                stop();
            } else {
                mTimerHandler.postDelayed( this, currentDelay );
                updateTimerTextView();
            }
        }
    };

    private void setTimerTextCompleted() {
        mTimerTextView.setText( timer.timeCompleted() );
    }

    private void updateTimerTextView() {
        mTimerTextView.setText( timer.timeElapsedToString() );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phasePomodoro = true;
        promptPhaseChange = false;

        mPhaseText = (TextView) findViewById(R.id.phase_text);

        mTimerTextView = (TextView) findViewById(R.id.timer_view);
        mLayout = (LinearLayout) findViewById(R.id.layout_view);

        mStartButton = (Button) findViewById(R.id.start_button);
        mStartButton.setOnClickListener(start_button_listener);

        mStopButton = (Button) findViewById(R.id.stop_button);
        mStopButton.setOnClickListener(stop_button_listener);

        mRestartButton = (Button) findViewById(R.id.restart_button);
        mRestartButton.setOnClickListener(restart_button_listener);

        mCountdownButton = (Button) findViewById(R.id.countdown_button);
        mCountdownButton.setOnClickListener(countdown_button_listener);
    }

    @Override
    protected void onPause() {
        Log.d("state", "onPause");
        super.onPause();
        currentDelay = BACKGROUND_TIMER_DELAY;
    }

    @Override
    protected void onResume() {
        Log.d("state", "onResume");
        super.onResume();
        mTimerHandler.removeCallbacks(mTimerRunnable);
        currentDelay = NORMAL_TIMER_DELAY;
        if (mCurrentlyRunning) {
            mTimerHandler.postDelayed(mTimerRunnable, 0);
        }
    }

    View.OnClickListener start_button_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!mCurrentlyRunning) {
                start();
            }
        }
    };

    View.OnClickListener stop_button_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mCurrentlyRunning) {
                stop();
            }
        }
    };

    View.OnClickListener restart_button_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            restart();
        }
    };

    View.OnClickListener countdown_button_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mCurrentlyCountingDown) {
                mCountdownButton.setText(R.string.countdown_button_down);
                timer.setCountDown(false);
                mCurrentlyCountingDown = false;
            } else {
                mCountdownButton.setText(R.string.countdown_button_up);
                timer.setCountDown(true);
                mCurrentlyCountingDown = true;
            }
        }
    };

    public void stop() {
        savedTime = timer.timeElapsed();
        mTimerHandler.removeCallbacks(mTimerRunnable);
        mCurrentlyRunning = false;
    }

    public void start() {
        if (promptPhaseChange) {
            if (phasePomodoro) {
                timer = new RestTimer(mCurrentlyCountingDown);
                phasePomodoro = false;
                clearPromptPhaseChange();
            } else {
                timer = new PomodoroTimer(mCurrentlyCountingDown);
                phasePomodoro = true;
                clearPromptPhaseChange();
            }
        } else {
            if (phasePomodoro) {
                timer = new PomodoroTimer(savedTime, mCurrentlyCountingDown);

            } else {
                timer = new RestTimer(savedTime, mCurrentlyCountingDown);
            }
        }
        mTimerHandler.postDelayed(mTimerRunnable, 0);
        mCurrentlyRunning = true;
    }

    public void restart() {
        if (promptPhaseChange) {
            clearPromptPhaseChange();
        }
        savedTime = 0;
        if (timer.completed()) {
            start();
        } else {
            timer = new PomodoroTimer(mCurrentlyCountingDown);
        }
        updateTimerTextView();
    }

    private void setBackground() {
        if (phasePomodoro) {
            mLayout.setBackgroundResource(R.color.pomodoro_background);
        } else {
            mLayout.setBackgroundResource(R.color.rest_background);
        }
    }

    private void promptPhaseChange() {
        promptPhaseChange = true;
        if (phasePomodoro) {
            mStartButton.setText(R.string.start_rest_button);
        } else {
            mStartButton.setText(R.string.start_pomodoro_button);
        }
    }

    private void clearPromptPhaseChange() {
        promptPhaseChange = false;
        mStartButton.setText(R.string.start_button);
        if (phasePomodoro) {
            mPhaseText.setText(R.string.phase_pomodoro);
        } else {
            mPhaseText.setText(R.string.phase_rest);
        }
        setBackground();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
