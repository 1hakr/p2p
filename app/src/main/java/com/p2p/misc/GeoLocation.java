package com.p2p.misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.RequestTickle;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.VolleyTickle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.p2p.P2PApplication;
import com.p2p.entity.GeoCode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by saravanan on 05/03/15.
 */

public class GeoLocation {
    private final int PLAY_SERVICE_RETRY = 3;
    private final int TIMEOUT_SECONDS = 25;
    private final int TIMESTAMP_TIMEOUT = 1000*60*30;
    private HashMap<String,String> localCityNameMap = new HashMap<String,String>();

    private int retryCount = 0;
    private Context context;

    private GoogleApiClient mGoogleApiClient;
    private Location mTempLocation = null;

    private final String URL_GOOGLE_MMAP = "http://www.google.com/glm/mmap";

    private final String TAG="Geo Location";
    public static final String LOCATION_LATITUDE = "location_latitude",
                               LOCATION_LONGITUDE="location_longitude",
                               LOCATION_TIMESTAMP="location_timestamp",
                               LOCATION_PRIORITY="location_priority",
                               LOCATION_LOCALITY="location_locality",
                               LOCATION_CITY="location_city";
    public static final int LOCATION_USER_WEIGHT = 2,LOCATION_AUTO_WEIGHT = 1;

    private String mGeoLatitude,mGeoLongitude;
    private long mTimeStamp;
    public interface OnLocationUpdateListener{
        public void onLocationUpdated(LocationStatus status);
    }
    public GeoLocation(Context context){
        this.context = context;
    }

    public Location getAvailableLocation(boolean isNeedLiveLoc) throws Exception {
        Location location = null;

        if(PlayServicesUtils.checkGooglePlaySevices(context) && (location = getPlayServiceLastLocation(isNeedLiveLoc))!=null){
            Utils.log(TAG,"play serivce first location returned");
            return location;
        }else if(PlayServicesUtils.checkGooglePlaySevices(context) && (location = getPlayServiceLastLocation(true))!=null){
            Utils.log(TAG,"play serivce second location returned");
            return location;
        }
        else if((location = getTowerLocation())!=null){
            Utils.log(TAG,"tower location returned");
            return location;
        }

        return location;
    }


    public Location getPlayServiceLastLocation(final boolean isLiveLocation) {

        try {
            final CountDownLatch latch = new CountDownLatch(1);
            retryCount = 0;
            mTempLocation = null;

            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            Utils.log(TAG, "location connected");
                            if (isLiveLocation) {
                                LocationRequest mLocationRequest = new LocationRequest();
                                mLocationRequest.setInterval(10 * 1000);
                                mLocationRequest.setFastestInterval(10 * 1000);
                                mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

                                LocationServices.FusedLocationApi.requestLocationUpdates(
                                        mGoogleApiClient, mLocationRequest, new com.google.android.gms.location.LocationListener() {
                                            @Override
                                            public void onLocationChanged(Location location) {
                                                mTempLocation = location;

                                                if (mTempLocation != null) {
                                                    Utils.log(TAG, "play service live location fetched" + mTempLocation.getLatitude() + ":" + mTempLocation.getLongitude());
                                                }
                                                latch.countDown();
                                            }

                                        });

                            } else {
                                mTempLocation = LocationServices.FusedLocationApi.getLastLocation(
                                        mGoogleApiClient);

                                if (mTempLocation != null) {
                                    Utils.log(TAG, "play service last location fetched" + mTempLocation.getLatitude() + ":" + mTempLocation.getLongitude());
                                }
                                latch.countDown();
                            }
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            if (retryCount < PLAY_SERVICE_RETRY) {
                                mGoogleApiClient.reconnect();
                                retryCount++;
                            }
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {
                            if (retryCount < PLAY_SERVICE_RETRY) {
                                mGoogleApiClient.reconnect();
                                retryCount++;
                            }
                        }
                    })
                    .addApi(LocationServices.API)
                    .build();

            mGoogleApiClient.connect();
            latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            mGoogleApiClient.disconnect();


        }catch (Exception e){
            e.printStackTrace();
        }
        return mTempLocation;
    }


    private Location getLastLocation(String provider){
        Location location = null;
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            if (locationManager != null) {
                if (locationManager.isProviderEnabled(provider)) {
                    location = locationManager.getLastKnownLocation(provider);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return location;
    }

    private Location getCurrentLocation(final String provider){
        final LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

        try {
            if (locationManager != null) {
                if (locationManager.isProviderEnabled(provider)) {

                    final CountDownLatch latch = new CountDownLatch(1);
                    mTempLocation = null;

                    final LocationListener mLocationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {

                            mTempLocation = location;
                            latch.countDown();
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {

                        }

                        @Override
                        public void onProviderEnabled(String provider) {

                        }

                        @Override
                        public void onProviderDisabled(String provider) {

                        }
                    };

                    if (Looper.myLooper() == null) {
                        Looper.prepare();
                    }
                    Handler handler = new Handler();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            locationManager.requestLocationUpdates(provider, 0, 0, mLocationListener);
                            Looper.myLooper().quit();
                        }
                    });
                    Looper.loop();

                    latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    locationManager.removeUpdates(mLocationListener);
                }
            }

            Log.i("provider name", "" + provider);
        }catch (Exception e){
            e.printStackTrace();
        }
        return mTempLocation;
    }


    private Location getTowerLocation() throws Exception {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        Location location = null;
        if(telephonyManager!=null) {

            int phoneType = telephonyManager.getPhoneType();
            if(phoneType == TelephonyManager.PHONE_TYPE_CDMA){
                CdmaCellLocation cellLocation = (CdmaCellLocation)telephonyManager.getCellLocation();

                location = getCdmaTowerLocation(cellLocation);
            }else if(phoneType == TelephonyManager.PHONE_TYPE_GSM){
                GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();

                location = getGsmTowerLocation(cellLocation);
            }


        }
        return location;
    }

    private Location getCdmaTowerLocation(CdmaCellLocation cdmaCellLocation) throws Exception {
        Location location = null;
        if(cdmaCellLocation!=null) {
            location = new Location(LocationManager.NETWORK_PROVIDER);
            location.setLatitude(cdmaCellLocation.getBaseStationLatitude());
            location.setLongitude(cdmaCellLocation.getBaseStationLongitude());
        }
        return location;
    }

    private Location getGsmTowerLocation(GsmCellLocation cellLocation) throws Exception {
        int cid,lac;

        if (cellLocation == null) {
            return null;
        }else{
             cid = cellLocation.getCid();
             lac = cellLocation.getLac();
        }

        HttpURLConnection httpConn = null;
        try {
            URL url = new URL(URL_GOOGLE_MMAP);
            URLConnection conn = url.openConnection();
            httpConn = (HttpURLConnection) conn;
            httpConn.setRequestMethod("POST");
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            httpConn.connect();

            OutputStream outputStream = httpConn.getOutputStream();
            writeData(outputStream, cid, lac);

            InputStream inputStream = httpConn.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            dataInputStream.readShort();
            dataInputStream.readByte();
            int code = dataInputStream.readInt();
            if (code == 0) {
                double lat = (double) dataInputStream.readInt() / 1000000D;
                double lon = (double) dataInputStream.readInt() / 1000000D;
                int i = dataInputStream.readInt();
                int j = dataInputStream.readInt();
                String s = dataInputStream.readUTF();
                dataInputStream.close();

                Location loc = new Location(LocationManager.NETWORK_PROVIDER);
                loc.setLatitude(lat);
                loc.setLongitude(lon);
                //loc.setAccuracy(Float.parseFloat(data.get("accuracy").toString()));
                loc.setTime(System.currentTimeMillis());
                return loc;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (null != httpConn) {
                httpConn.disconnect();
            }
        }

        return null;

    }

    private static void writeData(OutputStream out, int cid, int lac)
            throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(out);
        dataOutputStream.writeShort(21);
        dataOutputStream.writeLong(0);
        dataOutputStream.writeUTF("en");
        dataOutputStream.writeUTF("Android");
        dataOutputStream.writeUTF("1.0");
        dataOutputStream.writeUTF("Web");
        dataOutputStream.writeByte(27);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        if (cid >= 65536) {
            // Unicom 3G
            dataOutputStream.writeInt(5);
        } else {
            // Mobile 3G
            dataOutputStream.writeInt(3);
        }
        dataOutputStream.writeUTF("");

        dataOutputStream.writeInt(cid);
        dataOutputStream.writeInt(lac);

        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.flush();
    }

    private Address getAddress(String latitude,String longitude)throws Exception {
        Address address = getAddressFromGeocoder(latitude,longitude);
        if(address == null)
            address = getAddressFromMapApi(latitude,longitude);

        return address;
    }

    private Address getAddressFromGeocoder(String latitude,String longitude) throws Exception {
        Geocoder geocoder = new Geocoder(context);
        List<Address> addresses = null;

        Utils.log(TAG,"trying geocoder");
        try {
            addresses = geocoder.getFromLocation(Double.parseDouble(latitude), Double.parseDouble(longitude), 1);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        Address address = null;
        // If the reverse geocode returned an address
        if (addresses != null && addresses.size() > 0) {
            address = addresses.get(0);
        }

        return address;
    }

    private Address getAddressFromMapApi(String latitude,String longitude) throws Exception {
        Address address = null;

        Utils.log(TAG,"trying mapapi");
        Locale locale = P2PApplication.getInstance().getLocale();
        RequestTickle mRequestTickle = VolleyTickle.newRequestTickle(context);
        String url = String.format(locale,
                "http://maps.googleapis.com/maps/api/geocode/json?latlng=%1$s,%2$s&sensor=true&language=" + locale.getCountry(),
                latitude, longitude);
        NetworkResponse response = new NetworkResponse(000, null, null, false);
        StringRequest request = new StringRequest(url, null, null);
        mRequestTickle.add(request);
        response = mRequestTickle.start();
        String data = "";
        try {
            if (response.statusCode == 200) {
                data = VolleyTickle.parseResponse(response);
                Gson gson = new Gson();
                final GeoCode geoCode = gson.fromJson(data, GeoCode.class);

                if ("OK".equalsIgnoreCase(geoCode.status)) {
                    if (geoCode.results.size() > 0) {
                        GeoCode.Result result = geoCode.results.get(0);
                        address = new Address(locale);
                        address.setLatitude(Double.parseDouble(latitude));
                        address.setLongitude(Double.parseDouble(longitude));
                        for (GeoCode.Result.Component component : result.address_components) {
                            if (component.types.contains("locality")) {
                                address.setLocality(component.long_name);
                            } else if (component.types.contains("sublocality")) {
                                address.setSubLocality(component.long_name);
                            } else if (component.types.contains("country")) {
                                address.setCountryName(component.long_name);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return address;
    }

    public static class LocationStatus{
        public boolean isNewCityFetched = false;
        public boolean isNewLocationFetched = false;
        public Bundle bundle = null;
    }
}
