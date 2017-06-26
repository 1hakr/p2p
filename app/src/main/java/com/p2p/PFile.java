package com.p2p;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.cache.SimpleImageLoader;
import com.android.volley.error.VolleyError;
import com.android.volley.request.GsonRequest;
import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.p2p.entity.PlaceDetails;
import com.p2p.entity.Places;
import com.p2p.entity.Profile;
import com.p2p.misc.Utils;


public class PFile extends ActionBarActivity implements OnMapReadyCallback, ClusterManager.OnClusterItemClickListener<Profile.Checkin>,
        ClusterManager.OnClusterItemInfoWindowClickListener<Profile.Checkin> {

    private GoogleMap mMap;
    private ClusterManager<Profile.Checkin> mClusterManager;
    private SharedPreferences mSharedPreferences;
    private Profile.Checkin checkin;
    private TextView name;
    private ImageView thumb;
    private SimpleImageLoader mImageFetcher;
    private View header;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pfile);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        checkin = (Profile.Checkin) getIntent().getExtras().getSerializable(Utils.BUNDLE_MENU);

        init();
        initCache();
        initControls();
        getPlace();
    }

    private void initCache() {
        mImageFetcher = P2PApplication.getInstance().getImageLoader();
        mImageFetcher.startProcessingQueue();
    }

    private void initControls() {
        header = findViewById(R.id.header);
        name = (TextView)findViewById(R.id.name);
        thumb = (ImageView)findViewById(R.id.thumb);
        name.setText(checkin.name);
        mImageFetcher.get(checkin.photo_url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                if(null != imageContainer.getBitmap()) {
                    thumb.setImageBitmap(imageContainer.getBitmap());
                    Palette.generateAsync(imageContainer.getBitmap(), new Palette.PaletteAsyncListener() {
                        public void onGenerated(Palette palette) {
                            try{
                                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(palette.getVibrantSwatch().getBodyTextColor()));
                                header.setBackgroundColor(palette.getVibrantSwatch().getBodyTextColor());
                            }catch (Exception e){

                            }
                        }
                    });
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    private void init() {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_pfile, menu);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setContentDescription("Map with toilets");
        mMap.setMyLocationEnabled(true);
        mClusterManager = new ClusterManager<Profile.Checkin>(this, getMap());
        mClusterManager.setRenderer(new PlaceRenderer());
        getMap().setOnCameraChangeListener(mClusterManager);
        getMap().setOnMarkerClickListener(mClusterManager);
        getMap().setOnInfoWindowClickListener(mClusterManager);
        //getMap().setTrafficEnabled(true);

        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);
    }

    protected GoogleMap getMap() {
        return mMap;
    }

    @Override
    public boolean onClusterItemClick(Profile.Checkin place) {
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(Profile.Checkin place) {
        String latlong = "";//LocationUtils.getLatLng(getActivity(), location);
        Bundle bundle = new Bundle();
        bundle.putString(Utils.BUNDLE_LOCATION, latlong);
        bundle.putSerializable(Utils.BUNDLE_RESTAURANT, place);
        Intent intent = new Intent(this, PzoneDetails.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private class PlaceRenderer extends DefaultClusterRenderer<Profile.Checkin> {
        private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private final ImageView mImageView;
        private final int mDimension;

        public PlaceRenderer() {
            super(getApplicationContext(), getMap(), mClusterManager);

            mImageView = new ImageView(getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(Profile.Checkin person, MarkerOptions markerOptions) {
            mImageView.setImageResource(R.drawable.ic_p2p);
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(person.name);
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            return cluster.getSize() > 10;
        }
    }

    public void getPlace(){
        final ArrayMap<String, String> param = new ArrayMap<String, String>();
        param.put("id", checkin.profile_id);

        GsonRequest<Profile> request = new GsonRequest<Profile>(Request.Method.GET,
                Utils.API_URL + "/profiles",
                Profile.class,
                null,
                param,
                new Response.Listener<Profile>() {
                    @Override
                    public void onResponse(Profile profile) {
                        showProfileDetails(profile);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(Utils.isActivityAlive(PFile.this))
                            Toast.makeText(PFile.this, "No Pee Places found", Toast.LENGTH_LONG).show();
                    }
                });

        P2PApplication.getInstance().addToRequestQueue(request);
    }

    public void showProfileDetails(Profile profile){
        getMap().clear();
        mClusterManager.clearItems();
        mClusterManager.addItems(profile.checkins);
        mClusterManager.cluster();

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Profile.Checkin checkin1 : profile.checkins) {
            builder.include(checkin1.getPosition());
        }

        changeCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200));
        name.setText(profile.name);
        mImageFetcher.get(checkin.photo_url, thumb);

    }


    private void changeCamera(CameraUpdate update) {
        changeCamera(update, null, true);
    }

    private void changeCamera(CameraUpdate update, GoogleMap.CancelableCallback callback, boolean animate) {
        if (animate) {
            getMap().animateCamera(update, callback);
        } else {
            getMap().moveCamera(update);
        }
    }

}
