package com.p2p;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.GsonRequest;
import com.p2p.entity.Check;
import com.p2p.entity.Feedback;
import com.p2p.entity.Places;
import com.p2p.misc.Utils;
import com.p2p.ui.AppRate;
import com.p2p.ui.wave.WaveView;

import java.io.Serializable;


public class FlushedActivity extends ActionBarActivity {

    private WaveView wave;
    private AppRate mAppRate;
    private ViewGroup feedback;
    private SharedPreferences mSharedPreferences;
    private Places.Place place;
    private View king_message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flushed);

        getSupportActionBar().hide();
        place = (Places.Place) getIntent().getExtras().getSerializable(Utils.BUNDLE_RESTAURANT);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        wave = (WaveView) findViewById(R.id.wave);
        feedback = (ViewGroup) findViewById(R.id.feedback);
        king_message = findViewById(R.id.king_message);

        checkin();
        animate();
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
                feedback(false);

            }

            @Override
            public void onRateAppClicked() {
                feedback(true);

            }
        });
        mAppRate.forceShow();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_flushed, menu);
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

    public void checkin(){
        final ArrayMap<String, String> param = new ArrayMap<String, String>();
        param.put("place_id", place.id);
        param.put("profile_id", mSharedPreferences.getString(Utils.KEY_PROFILE_ID, "1"));

        GsonRequest<Check> request = new GsonRequest<Check>(Request.Method.POST,
                Utils.API_URL + "/checkin",
                Check.class,
                null,
                param,
                new Response.Listener<Check>() {
                    @Override
                    public void onResponse(Check places) {
                        //if(Utils.isActivityAlive(FlushedActivity.this))
                            //Toast.makeText(FlushedActivity.this, "Checked-In", Toast.LENGTH_LONG).show();
                        if(places.king){
                            king_message.setVisibility(View.VISIBLE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(Utils.isActivityAlive(FlushedActivity.this))
                            Toast.makeText(FlushedActivity.this, "No Check-In", Toast.LENGTH_LONG).show();
                    }
                });

        P2PApplication.getInstance().addToRequestQueue(request);
    }

    public void feedback(boolean like){
        final ArrayMap<String, String> param = new ArrayMap<String, String>();
        param.put("place_id", place.id);
        param.put("profile_id", mSharedPreferences.getString(Utils.KEY_PROFILE_ID, "1"));
        param.put("feedback", like ? "1" : "0");

        GsonRequest<Feedback> request = new GsonRequest<Feedback>(Request.Method.POST,
                Utils.API_URL + "/feedback",
                Feedback.class,
                null,
                param,
                new Response.Listener<Feedback>() {
                    @Override
                    public void onResponse(Feedback feedback1) {
                        if(Utils.isActivityAlive(FlushedActivity.this))
                            Toast.makeText(FlushedActivity.this, "Feedback give", Toast.LENGTH_LONG).show();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Utils.BUNDLE_RESTAURANT, place);
                        Intent intent = new Intent(FlushedActivity.this, PzoneDetails.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(Utils.isActivityAlive(FlushedActivity.this))
                            Toast.makeText(FlushedActivity.this, "Feedback Not given", Toast.LENGTH_LONG).show();
                    }
                });

        P2PApplication.getInstance().addToRequestQueue(request);
    }


    private void animate() {
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
}
