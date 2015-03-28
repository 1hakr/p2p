package com.p2p;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.p2p.ui.AppRate;
import com.p2p.ui.wave.WaveView;


public class FlushedActivity extends ActionBarActivity {

    private WaveView wave;
    private AppRate mAppRate;
    private ViewGroup feedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flushed);

        wave = (WaveView) findViewById(R.id.wave);
        feedback = (ViewGroup) findViewById(R.id.feedback);
        new AsyncTask<Void, Void, Void>() {
            int i;
            private int mCounter;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(0, 900000);
                    Thread.sleep(0, 900000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (i = 0; i <= 100; i++) {
                    try {
                        mCounter += 100;
                        Thread.sleep(0, 300000);
                        publishProgress();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                wave.setProgress(wave.getProgress()-1);
                super.onProgressUpdate(values);
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                wave.setProgress(0);
                showFeedback();
            }

        }.execute();
    }

    private void showFeedback() {
        mAppRate = AppRate.with(this, feedback, getString(R.string.feedback_message), 0);
        mAppRate.setLayoutId(R.layout.layout_rate_app);
        mAppRate.listener(new AppRate.OnShowListener() {
            @Override
            public void onRateAppShowing() {

            }

            @Override
            public void onRateAppDismissed() {
            }

            @Override
            public void onRateAppDenied() {

            }

            @Override
            public void onRateAppClicked() {

            }
        });
        mAppRate.setTextViewClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        mAppRate.forceShow();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_flushed, menu);
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
