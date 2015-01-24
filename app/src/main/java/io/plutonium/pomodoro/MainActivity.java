package io.plutonium.pomodoro;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
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
    NotificationManager notificationManager;

    final static int NORMAL_TIMER_DELAY = 100;
    final static int BACKGROUND_TIMER_DELAY = 10000;

    int currentDelay = NORMAL_TIMER_DELAY;

    Runnable mTimerRunnable = new Runnable() {
        @Override
        public void run() {
            if (timer.completed()) {
                updateTimerTextView();
                notifyCompleted();
                promptPhaseChange();
                stop();
            } else {
                mTimerHandler.postDelayed( this, currentDelay );
                updateView();
            }
        }
    };

    private void notifyCompleted() {
        Log.d("enter", "notifyCompleted");
        String completionText;

        if (phasePomodoro) {
            completionText = getString(R.string.completed_pomodoro);
        } else {
            completionText = getString(R.string.completed_rest);
        }

        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this) // or getApplicationContext();
                .setSmallIcon( R.drawable.ic_launcher )
                .setContentTitle( getString(R.string.app_name) )
                .setContentText( completionText )
                .setContentIntent( PendingIntent.getActivity(this, 0, intent, 0) )
                .setSound( soundUri );

        notificationManager.notify(0, mBuilder.build());
    }

    private void setTimerTextCompleted() {
        Log.d("enter", "setTimerTextCompleted");
        mTimerTextView.setText( timer.timeCompleted() );
    }

    private void updateTimerTextView() {
        Log.d("enter", "updateTimerTextView");
        mTimerTextView.setText( timer.timeElapsedToString() );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("enter", "onCreate");
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

    private void updateView() {
        Log.d("enter", "updateView");
        updateTimerTextView();

        if (promptPhaseChange) {
            if (phasePomodoro) {
                mStartButton.setText(R.string.start_rest_button);
            } else {
                mStartButton.setText(R.string.start_pomodoro_button);
            }
        } else {
            mStartButton.setText(R.string.start_button);
            if (phasePomodoro) {
                mPhaseText.setText(R.string.phase_pomodoro);
            } else {
                mPhaseText.setText(R.string.phase_rest);
            }
        }

        setBackground();
    }

    @Override
    protected void onPause() {
        Log.d("enter", "onPause");
        super.onPause();
        currentDelay = BACKGROUND_TIMER_DELAY;
    }

    private void initializeTimer() {
        Log.d("enter", "initializeTimer");
        if (timer == null) {
            if (phasePomodoro) {
                timer = new PomodoroTimer(savedTime, mCurrentlyCountingDown);
            } else {
                timer = new RestTimer(savedTime, mCurrentlyCountingDown);
            }
        }
    }

    @Override
    protected void onResume() {
        Log.d("enter", "onResume");
        super.onResume();

        initializeTimer();

        mTimerHandler.removeCallbacks(mTimerRunnable);
        currentDelay = NORMAL_TIMER_DELAY;
        if (mCurrentlyRunning) {
            mTimerHandler.postDelayed(mTimerRunnable, 0);
        } else {
            updateView();
        }
    }

    View.OnClickListener start_button_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("enter", "start_button_listener");
            if (!mCurrentlyRunning) {
                start();
            }
        }
    };

    View.OnClickListener stop_button_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("enter", "stop_button_listener");
            if (mCurrentlyRunning) {
                stop();
            }
        }
    };

    View.OnClickListener restart_button_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("enter", "restart_button_listener");
            restart();
        }
    };

    View.OnClickListener countdown_button_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("enter", "countdown_button_listener");
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
        Log.d("enter", "stop");
        savedTime = timer.timeElapsed();
        mTimerHandler.removeCallbacks(mTimerRunnable);
        mCurrentlyRunning = false;
    }

    public void start() {
        Log.d("enter", "start");
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
        Log.d("enter", "restart");
        savedTime = 0;
        if (promptPhaseChange) {
            clearPromptPhaseChange();
            start();
        } else {
            if (phasePomodoro) {
                timer = new PomodoroTimer(mCurrentlyCountingDown);
            } else {
                timer = new RestTimer(mCurrentlyCountingDown);
            }
        }
        updateTimerTextView();
    }

    private void setBackground() {
        Log.d("enter", "setBackground");
        if (phasePomodoro) {
            mLayout.setBackgroundResource(R.color.pomodoro_background);
        } else {
            mLayout.setBackgroundResource(R.color.rest_background);
        }
    }

    private void promptPhaseChange() {
        Log.d("enter", "promptPhaseChange");
        promptPhaseChange = true;
        updateView();

    }

    private void clearPromptPhaseChange() {
        Log.d("enter", "clearPromptPhaseChange");
        promptPhaseChange = false;
        updateView();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("enter", "onCreateOptionsMenu");
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
