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
    TextView mTimerTextView;
    LinearLayout mLayout;
    PomodoroTimer timer;
    long savedTime = 0;

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
                alertBackground();
                stop();
            } else {
                mTimerHandler.postDelayed( this, currentDelay );
                updateTimerTextView();
            }
        }
    };

    protected void updateTimerTextView() {
        mTimerTextView.setText( timer.timeElapsedToString() );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                mCountdownButton.setText(R.string.countdown_button_up);
                timer.setCountDown(false);
                mCurrentlyCountingDown = false;
            } else {
                mCountdownButton.setText(R.string.countdown_button_down);
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
        timer = new PomodoroTimer(savedTime, mCurrentlyCountingDown);
        mTimerHandler.postDelayed(mTimerRunnable, 0);
        mCurrentlyRunning = true;
    }

    public void restart() {
        defaultBackground();
        savedTime = 0;
        if (timer.completed()) {
            start();
        } else {
            timer = new PomodoroTimer(mCurrentlyCountingDown);
        }
        updateTimerTextView();
    }

    private void alertBackground() {
        mLayout.setBackgroundResource(R.color.alert_background);
    }

    private void defaultBackground() {
        mLayout.setBackgroundResource(R.color.default_background);
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
