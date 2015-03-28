package com.p2p;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.GsonRequest;
import com.p2p.entity.Profile;
import com.p2p.misc.DeviceUuidFactory;
import com.p2p.misc.Utils;

public class MainActivity extends ThemedActivity
        implements Utils.OnFragmentInteractionListener,
        NavigationDrawerFragment.NavigationDrawerCallbacks {


    private static final String TAG = "Device";
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupNavigation();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mSharedPreferences.edit();
        getPofile();

    }


    private void setupNavigation() {
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
/*        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout))*/;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.navigation, menu);
            restoreActionBar();
            return true;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.menu_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(int type, Bundle bundle) {

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        switch (position){
            case 0:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new InitialFragment())
                        .commit();
                break;
            case 1:
            case 2:

                break;
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 0:
                mTitle = getString(R.string.title_section1);
                break;
            case 1:
                mTitle = getString(R.string.title_section2);
                break;
            case 2:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    public void getPofile(){
        final ArrayMap<String, String> param = new ArrayMap<String, String>();
        DeviceUuidFactory deviceUuidFactory = new DeviceUuidFactory(getApplication());
        String device_id = deviceUuidFactory.getDeviceUuid().toString();
        param.put("device_id", device_id);

        GsonRequest<Profile> request = new GsonRequest<Profile>(Request.Method.POST,
                Utils.API_URL + "/profiles",
                Profile.class,
                null,
                param,
                new Response.Listener<Profile>() {
                    @Override
                    public void onResponse(Profile device) {
                        mEditor.putString(Utils.KEY_PROFILE_ID, device.id);
                        mEditor.putString(Utils.KEY_NAME, device.name);
                        mEditor.putString(Utils.KEY_DEVICE_ID, device.device_id);
                        mEditor.putString(Utils.KEY_PHOTO_URL, device.photo_url);
                        mEditor.commit();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_LONG).show();

                    }
                });

        P2PApplication.getInstance().addToRequestQueue(request, TAG);
    }
}
