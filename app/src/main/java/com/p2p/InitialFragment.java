package com.p2p;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.misc.AsyncTask;
import com.android.volley.request.GsonRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.p2p.directions.GoogleParser;
import com.p2p.directions.Route;
import com.p2p.directions.Routing;
import com.p2p.directions.RoutingListener;
import com.p2p.entity.Places;
import com.p2p.entity.Places.Place;
import com.p2p.entity.RestaurantProfile;
import com.p2p.misc.ErrorDialogFragment;
import com.p2p.misc.GCMUtils;
import com.p2p.misc.GeoLocation;
import com.p2p.misc.LocationHelper;
import com.p2p.misc.LocationUtils;
import com.p2p.misc.Utils;
//import com.p2p.restaurant.RestaurantProfileActivity;
import com.p2p.ui.RippleBackground;
import com.p2p.ui.seekbar.ComboSeekBar;
import com.p2p.ui.seekbar.PhasedListener;
import com.p2p.ui.seekbar.PhasedSeekBar;
import com.p2p.ui.seekbar.SimplePhasedAdapter;

import java.util.ArrayList;

public class InitialFragment extends Fragment implements ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener, OnMapReadyCallback, View.OnClickListener,
        ClusterManager.OnClusterItemClickListener<Place>,
        ClusterManager.OnClusterItemInfoWindowClickListener<Place>, RoutingListener, PhasedListener {

    public static final int SEARCH_RADIUS = 800;
    private static final String TAG = "InitialFragment";

    private Utils.OnFragmentInteractionListener mListener;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private boolean myLocation;
    private boolean pathDrawn;
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    private GoogleMap mMap;
    private int mStrokeColor;
    private int mFillColor;
    private View initialContainer;
    private CurrentLocationSource currentLocationSource;
    private ClusterManager<Places.Place> mClusterManager;
    private View loadingContainer;
    private View retryContainer;
    private Button retry;
    private RippleBackground rippleBackground;
    private LatLng current;
    private PhasedSeekBar seekbar;
    private Places currentPlaces;

    public static InitialFragment newInstance() {
        InitialFragment fragment = new InitialFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public InitialFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initControls();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_initial, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initialContainer = view.findViewById(R.id.initialContainer);
        loadingContainer = view.findViewById(R.id.loadingContainer);
        retryContainer = view.findViewById(R.id.retryContainer);
        retry = (Button) view.findViewById(R.id.retry);
        retry.setOnClickListener(this);

        seekbar = (PhasedSeekBar) view.findViewById(R.id.seekbar);
        seekbar.setAdapter(new SimplePhasedAdapter(getResources(), new int[] {
                R.drawable.one_selector,
                R.drawable.two_selector,
                R.drawable.three_selector}));

        seekbar.setListener(this);
        seekbar.setPosition(0);
        rippleBackground = (RippleBackground)view.findViewById(R.id.rippleBackground);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (servicesConnected() && null != mGoogleApiClient) {
            mGoogleApiClient.connect();
        }
        currentLocationSource.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (servicesConnected()) {
            if (null != mGoogleApiClient)
                mGoogleApiClient.disconnect();
        }
        currentLocationSource.onPause();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (Utils.OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        ((MainActivity) activity).onSectionAttached(0);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getMyLocation();
    }

    @Override
    public void onConnected(Bundle bundle) {
        fusedLocationProviderApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        if (checkLocationServices()) {
            Location currentLocation = fusedLocationProviderApi.getLastLocation(mGoogleApiClient);
            getGeoLocation(false);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if(mGoogleApiClient.isConnected()){
            fusedLocationProviderApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (checkLocationServices()) {
            if(pathDrawn){
                currentLocationSource.onLocationChanged(new LatLng(location.getLatitude(), location.getLongitude()));
            } else {
                getGeoLocation(false);
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(getActivity(), LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    private void initControls() {
        mFillColor = Color.parseColor("#34FF5722");
        mStrokeColor = Color.RED;
        currentLocationSource = new CurrentLocationSource();

        if (servicesConnected()) {
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                            .addApi(LocationServices.API)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .build();
        }
    }

    public boolean isMyLocationAvailable() {
        return myLocation;
    }

    public void getMyLocation() {
        rippleBackground.startRippleAnimation();
        retryContainer.setVisibility(View.GONE);
        loadingContainer.setVisibility(View.VISIBLE);
        initialContainer.setVisibility(View.VISIBLE);
        if (!checkLocationServices()) {
            checkOrEnableLocationServices();
            //new ReqestTask().execute();
        } else {
            Toast.makeText(getActivity(), "Waiting for Location", Toast.LENGTH_SHORT).show();
            getGeoLocation(false);
        }
    }

    public void getGeoLocation(boolean silently) {
        boolean gpsConnected = servicesConnected();
        boolean locationAvailable = isMyLocationAvailable();
        if (!silently && gpsConnected && !locationAvailable) {
            Location currentLocation = fusedLocationProviderApi.getLastLocation(mGoogleApiClient);
            if(null != currentLocation){
                myLocation = true;
                //new WaitTask(currentLocation).execute();
                showCurrentLocation(currentLocation);
            }
            else {
                new ReqestTask().execute();
            }
        } else {
            new RequestRelaxedTask().execute();
        }
    }

    public boolean checkLocationServices() {
        boolean gps_enabled = false;
        boolean network_enabled = false;
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        return gps_enabled || network_enabled;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setContentDescription("Map with restaurants");
        mMap.setLocationSource(currentLocationSource);
        mMap.setMyLocationEnabled(true);
        mClusterManager = new ClusterManager<Place>(getActivity(), getMap());
        mClusterManager.setRenderer(new PlaceRenderer());
        getMap().setOnCameraChangeListener(mClusterManager);
        getMap().setOnMarkerClickListener(mClusterManager);
        getMap().setOnInfoWindowClickListener(mClusterManager);

        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);

    }

    public boolean checkOrEnableLocationServices() {
        if (!checkLocationServices()) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setTitle("Location Services Disabled").setMessage("Practo Search needs access to your location. Please turn on location access.")
                    .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                        }
                    }).
                    setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            getGeoLocation(true);
                        }
                    }).show();
            return false;
        } else {
            return true;
        }
    }

    private boolean servicesConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (ConnectionResult.SUCCESS == resultCode) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), GCMUtils.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return true;
        }
        return false;
    }

    private void showErrorDialog(int errorCode) {

        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, getActivity(), LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
        if (errorDialog != null) {
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();
            errorFragment.setDialog(errorDialog);
            errorFragment.show(getActivity().getFragmentManager(), LocationUtils.TAG);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.retry:
                getMyLocation();
                break;
        }
    }

    @Override
    public boolean onClusterItemClick(Place place) {
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(Place place) {
        RestaurantProfile.Restaurant restaurant = new RestaurantProfile.Restaurant();
        RestaurantProfile profile = new RestaurantProfile();
        restaurant.name = place.name;
        restaurant.image_url = "";
        profile.places.add(0, restaurant);

        String latlong = "";//LocationUtils.getLatLng(getActivity(), location);
        Bundle bundle = new Bundle();
        bundle.putString(Utils.BUNDLE_LOCATION, latlong);
        bundle.putSerializable(Utils.BUNDLE_RESTAURANT, profile);
        //Intent intent = new Intent(getActivity(), RestaurantProfileActivity.class);
        //intent.putExtras(bundle);
        //getActivity().startActivity(intent);
    }

    public class ReqestTask extends AsyncTask<Void, Void, Location>{

        @Override
        protected Location doInBackground(Void... voids) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return new GeoLocation(getActivity()).getPlayServiceLastLocation(true);
            //return LocationHelper.getLocation(getActivity());
        }

        @Override
        protected void onPostExecute(Location location) {
            super.onPostExecute(location);
            showCurrentLocation(location);
        }
    }

    public class RequestRelaxedTask extends AsyncTask<Void, Void, Location>{

        @Override
        protected Location doInBackground(Void... voids) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return Utils.isActivityAlive(getActivity()) ? LocationHelper.getLocation(getActivity()) : null;
        }

        @Override
        protected void onPostExecute(Location location) {
            super.onPostExecute(location);
            if(null != location) {
                showCurrentLocation(location);
            }
        }
    }

    private void showRestaurantDetails(Location location){

        if(null != location){
            String latlong = LocationUtils.getLatLng(getActivity(), location);
            //Toast.makeText(getActivity(), "Location is: " + latLlong, Toast.LENGTH_SHORT).show();
            getActivity().finish();
            Bundle bundle = new Bundle();
            bundle.putString(Utils.BUNDLE_LOCATION, latlong);
            //Intent intent = new Intent(getActivity(), RestaurantProfileActivity.class);
            //intent.putExtras(bundle);
            //getActivity().startActivity(intent);
        }
    }

    private void showCurrentLocation(Location currentLocation){
        if(null != currentLocation){
            getRestaurantsAround(currentLocation);
            showCurrentLocation(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
        } else {
            retryContainer.setVisibility(View.VISIBLE);
            loadingContainer.setVisibility(View.GONE);
        }
    }

    private void showCurrentLocation(LatLng currentLocation){
        current = currentLocation;
        rippleBackground.stopRippleAnimation();
        initialContainer.setVisibility(View.GONE);
        if(null != mMap) {
            currentLocationSource.onLocationChanged(currentLocation);
            CameraPosition comerCameraPosition =
                    new CameraPosition.Builder().target((currentLocation))
                            .zoom(14)
                            .tilt(50)
                            .build();
            // Move the map so that it is centered on the initial circle
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(comerCameraPosition));
        }
    }

    public void getRestaurantsAround(Location location){
        final ArrayMap<String, String> param = new ArrayMap<String, String>();
        param.put("lat", location.getLatitude() + "");
        param.put("lng", location.getLongitude() + "");
        param.put("gender", "male");

        GsonRequest<Places> request = new GsonRequest<Places>(Request.Method.GET,
                Utils.API_URL + "/places",
                Places.class,
                null,
                param,
                new Response.Listener<Places>() {
                    @Override
                    public void onResponse(Places places) {
                        currentPlaces = places;
                        seekbar.setPosition(0);
                        onPositionSelected(0);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(Utils.isActivityAlive(getActivity()))
                        Toast.makeText(getActivity(), "No Pee Places found", Toast.LENGTH_LONG).show();
                    }
                });

        P2PApplication.getInstance().addToRequestQueue(request, TAG);
        P2PApplication.getInstance().getRequestQueue().start();
    }

    public void drawRestaurantsAround(Place place){
        pathDrawn = true;
        getMap().clear();
        mClusterManager.clearItems();
        mClusterManager.addItem(place);
        mClusterManager.cluster();
        drawRoute(place.getPosition());
    }

    public void drawRoute(LatLng destination){
        if(null == current){
            return;
        }
        LatLng start = current;
        Routing routing = new Routing(Routing.TravelMode.DRIVING);
        routing.registerListener(this);
        routing.execute(start, destination);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(start);
        builder.include(destination);

        changeCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200));
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

    private static class CurrentLocationSource implements LocationSource{
        private OnLocationChangedListener mListener;
        private boolean mPaused;

        @Override
        public void activate(OnLocationChangedListener listener) {
            mListener = listener;
        }

        @Override
        public void deactivate() {
            mListener = null;
        }

        public void onLocationChanged(LatLng point) {
            if (mListener != null && !mPaused) {
                Location location = new Location("GSMLocationProvider");
                location.setLatitude(point.latitude);
                location.setLongitude(point.longitude);
                location.setAccuracy(100);
                mListener.onLocationChanged(location);
            }
        }

        public void onPause() {
            mPaused = true;
        }

        public void onResume() {
            mPaused = false;
        }
    }


    private class PlaceRenderer extends DefaultClusterRenderer<Place> {
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
        protected void onBeforeClusterItemRendered(Place person, MarkerOptions markerOptions) {
            mImageView.setImageResource(R.drawable.ic_p2p);
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(person.name);
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            return cluster.getSize() > 10;
        }
    }

    public class WaitTask extends AsyncTask<Void,Void,Void> {

        private Location currentLocation;

        public WaitTask(Location location){
            currentLocation = location;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            showCurrentLocation(currentLocation);
        }
    }

    private Context getApplicationContext() {
        return getActivity().getApplicationContext();
    }

    protected GoogleMap getMap() {
        return mMap;
    }


    @Override
    public void onRoutingFailure() {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(PolylineOptions mPolyOptions, Route route) {
        if (isAdded() && isVisible()) {
            PolylineOptions polyoptions = new PolylineOptions();
            polyoptions.color(getResources().getColor(R.color.practo_blue));
            polyoptions.width(20);
            polyoptions.addAll(mPolyOptions.getPoints());
            getMap().addPolyline(polyoptions);
        }
    }

    @Override
    public void onPositionSelected(int position) {

        if(null == currentPlaces){
            return;
        }
        switch (position){
            case 0:
                drawRestaurantsAround(currentPlaces.now);
                break;
            case 1:
                drawRestaurantsAround(currentPlaces.can_wait);
                break;
            case 2:
                drawRestaurantsAround(currentPlaces.royal_pee);
                startActivity(new Intent(getActivity(), FlushedActivity.class));
                break;
        }
    }
}