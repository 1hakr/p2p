/*
 *
 *    * Copyright 2014 Basit Parkar.
 *    *
 *    * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *    * use this file except in compliance with the License. You may obtain a copy of
 *    * the License at
 *    *
 *    * http://www.apache.org/licenses/LICENSE-2.0
 *    *
 *    * Unless required by applicable law or agreed to in writing, software
 *    * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    * License for the specific language governing permissions and limitations under
 *    * the License.
 *    *
 *    * @date 7/7/14 1:02 PM
 *    * @modified 7/7/14 12:57 PM
 *
 */

package com.p2p.misc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DeviceUtility {
    public static int signal_value;
    private static int is_exception = 0;
    int myLatitude, myLongitude;
    private Context mactivity;
    private float level;
    private TelephonyManager telephonyManager = null;
    private GsmCellLocation cellLocation = null;
    private String networkOperator;
    private String lastLoc = "0,0";
    private LocationListener listener;
    private String provider = "";
    private boolean UpdateCaught;
    private String providerUpdates = "0,0";
    private String longitude = "0", latitude = "0";
    private LocationManager mLocationManager = null;
    private Boolean needtoUpdate = true, is_AGPS_Require = false;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg9) {

            if (is_exception == 1) {
                try {

                    //toggleGPS(false, mactivity);
                    provider = "";
                    UpdateCaught = false;

                    listener = new LocationListener() {
                        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
                        }

                        public void onProviderEnabled(String arg0) {
                        }

                        public void onProviderDisabled(String arg0) {
                        }

                        public void onLocationChanged(Location location) {
                            UpdateCaught = true;
                            providerUpdates = location.getLatitude() + ","
                                    + location.getLongitude();
                            System.out.println("UPDATE CAUGHT" + providerUpdates);
                            mLocationManager.removeUpdates(listener);
                        }
                    };


                    if (chkGPS()) {
                        if (mLocationManager
                                .isProviderEnabled(LocationManager.GPS_PROVIDER))
                            provider = LocationManager.GPS_PROVIDER;

                        System.out.println("Provider:---" + provider);
                    } else {

                        System.out.println("Network--->>" + mLocationManager
                                .isProviderEnabled(LocationManager.NETWORK_PROVIDER));
                        System.out.println("GPS--->>" + mLocationManager
                                .isProviderEnabled(LocationManager.NETWORK_PROVIDER));
                        if (mLocationManager
                                .isProviderEnabled(LocationManager.GPS_PROVIDER))
                            provider = LocationManager.GPS_PROVIDER;

                        System.out.println("Provider:" + provider);
                        toggleGPS(false, mactivity);
                    }

                    if (provider != null && !provider.equals("")) {
                        mLocationManager.requestLocationUpdates(provider, 0, 0,
                                listener);

                        Location loc = mLocationManager.getLastKnownLocation(provider.trim());
                        System.out.println("after:" + loc);
                        String lastKnownLoc = "0,0";
                        if (loc != null) {
                            lastKnownLoc = loc.getLatitude() + ","
                                    + loc.getLongitude();
                            System.out.println("last known location:" + lastKnownLoc);
                            if (!needtoUpdate) {
                                if (lastKnownLoc.length() > 0) {
                                    String strlat = lastKnownLoc.substring(0,
                                            lastKnownLoc.indexOf(","));
                                    String strlong = lastKnownLoc.substring(
                                            lastKnownLoc.indexOf(",") + 1,
                                            lastKnownLoc.length());
                                    System.out.println("LAT:::" + strlat + "   "
                                            + "LONG:::" + strlong);
                                    set_Latitude(strlat);
                                    set_Longitude(strlong);
                                }
                            }

                            if (lastKnownLoc.equals(lastLoc)
                                    && !providerUpdates.equals("0,0")) {
                                if (providerUpdates.length() > 0) {
                                    String strlat = providerUpdates.substring(0,
                                            providerUpdates.indexOf(","));
                                    String strlong = providerUpdates.substring(
                                            providerUpdates.indexOf(",") + 1,
                                            providerUpdates.length());
                                    System.out.println("LAT:::" + strlat + "   "
                                            + "LONG:::" + strlong);
                                    set_Latitude(strlat);
                                    set_Longitude(strlong);
                                }
                            } else {
                                if (lastKnownLoc.length() > 0) {
                                    String strlat = lastKnownLoc.substring(0,
                                            lastKnownLoc.indexOf(","));
                                    String strlong = lastKnownLoc.substring(
                                            lastKnownLoc.indexOf(",") + 1,
                                            lastKnownLoc.length());
                                    System.out.println("LAT:::" + strlat + "   "
                                            + "LONG:::" + strlong);
                                    set_Latitude(strlat);
                                    set_Longitude(strlong);
                                }
                            }

                            System.out.println("Last location CAUGHT" + lastKnownLoc);

                            mLocationManager.requestLocationUpdates(provider, 0, 0,
                                    listener);
                            new Thread() {
                                public void run() {
                                    try {
                                        Thread.sleep(30000);
                                    } catch (InterruptedException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                    if (!UpdateCaught)
                                        mLocationManager.removeUpdates(listener);
                                }
                            }.start();
                        } else {
                            if (is_AGPS_Require) {
                                if (RqsLocation(get_Cid(), get_lac())) {

                                    longitude = String.valueOf((float) myLongitude / 1000000);
                                    latitude = String.valueOf((float) myLatitude / 1000000);
                                    System.out.println("latitude--->>" + latitude
                                            + "longitude---->>" + longitude);
                                    set_Latitude(latitude);
                                    set_Longitude(longitude);
                                }
                            }
                        }

                    } else {
                        if (is_AGPS_Require) {
                            if (RqsLocation(get_Cid(), get_lac())) {

                                longitude = String.valueOf((float) myLongitude / 1000000);
                                latitude = String.valueOf((float) myLatitude / 1000000);
                                System.out.println("latitude--->>" + latitude
                                        + "longitude---->>" + longitude);
                                set_Latitude(latitude);
                                set_Longitude(longitude);
                            }
                        }
                    }

                } catch (Exception e) {
                    System.out.println("Error:" + e.toString());
                }
            }
        }
    };
    private MyPhoneStateListener Listener;

    public DeviceUtility(Context context) {
        mactivity = context;

        level = -1;
        telephonyManager = (TelephonyManager) mactivity.getSystemService(Context.TELEPHONY_SERVICE);

        try {
            cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Listener = new MyPhoneStateListener();
        //	batteryLevel();
        getSignalStrength();
    }

    public static int getSignal_value() {
        return signal_value;
    }

    public static void setSignal_value(int signal_value) {
        DeviceUtility.signal_value = signal_value;
    }

    public static boolean CheckNetConnectivity(Context mContext) {

        ConnectivityManager connec = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connec.getNetworkInfo(0).getState() == State.CONNECTED
                || connec.getNetworkInfo(1).getState() == State.CONNECTED)
            return true;

        return false;

    }

    public String get_MCC() {
        String mcc = "0";
        if (simPresent() == 1) {
            if (!inAirplaneMode()) {
                networkOperator = telephonyManager.getNetworkOperator();
                System.out.println("Netwok operator " + networkOperator);
                mcc = networkOperator.substring(0, 3);
            }
        }
        return mcc;
    }

    public String get_MNC() {
        String mnc = "0";
        if (simPresent() == 1) {
            if (!inAirplaneMode()) {
                networkOperator = telephonyManager.getNetworkOperator();
                System.out.println("Netwok operator " + networkOperator);
                mnc = networkOperator.substring(3);
            }
        }
        return mnc;
    }

    public int get_Cid() {
        int cid = 0;
        if (cellLocation != null)
            cid = cellLocation.getCid();

        return cid;
    }

    public int get_lac() {
        int lac = 0;
        if (cellLocation != null)
            lac = cellLocation.getLac();

        return lac;
    }

    public void set_Latitude(String lat) {
        latitude = lat;
    }

    public void set_Longitude(String Long) {
        longitude = Long;
    }

    public String get_latitude() {
        return latitude;
    }

    public float get_BatteryLevel() {
        return level;
    }

    public void set_BatteryLevel(float Level) {
        level = Level;
    }

    public String get_longitude() {
        return longitude;
    }

    public void getLocation(final Context c, boolean needtoUpdate, boolean is_AGPS_Require) {
        this.mactivity = c;
        this.needtoUpdate = needtoUpdate;
        this.is_AGPS_Require = is_AGPS_Require;
        new Location_Progress().execute();

    }

    public String osVersion() {
        String version = System.getProperty("os.version");
        return version;
    }

    public String platformVersion() {
        String PVersion = android.os.Build.VERSION.RELEASE;
        return PVersion;
    }

    public String modelNumber() {
        String modelNo = android.os.Build.MODEL;
        return modelNo;
    }

    public float batteryLevel() {
        try {

            BroadcastReceiver batteryReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    //  context.unregisterReceiver(this);
                    Intent batteryIntent = mactivity.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                    int rawlevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    if (level == -1 || scale == -1) {
                        level = 50.0f;
                    }

                    if (rawlevel >= 0 && scale > 0) {
                        level = ((float) level / (float) scale) * 100.0f;
                    }

                    set_BatteryLevel(level);
                    // Constants.BATTERY_LEVEL=level;
                    System.out.println("Battery Level Remaining: " + level + "%");
                }

            };
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            mactivity.registerReceiver(batteryReceiver, filter);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return level;
    }

    public int simPresent() {

        TelephonyManager tManager = (TelephonyManager) mactivity
                .getSystemService(Context.TELEPHONY_SERVICE);

        int state = TelephonyManager.SIM_STATE_ABSENT;

		/*
         * if (tManager.getSimState() != state) { // sim present } else { // sim
		 * absent }
		 */

        return state;
    }

    public String getIMEINo() {

        TelephonyManager tManager = (TelephonyManager) mactivity
                .getSystemService(Context.TELEPHONY_SERVICE);
        String uid = tManager.getDeviceId();

        return uid;
    }

    public Boolean hasCamera() {

        PackageManager pm = mactivity.getApplicationContext()
                .getPackageManager();
        Boolean camera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);

        return camera;
    }

    public String getIMSINo() {

        TelephonyManager tManager = (TelephonyManager) mactivity
                .getSystemService(Context.TELEPHONY_SERVICE);

        String imsi = tManager.getSubscriberId();

        return imsi;
    }

    public int osVersionNo() {

        int SDK_INT = android.os.Build.VERSION.SDK_INT;

        return SDK_INT;

    }

    public String dateNTime() {
        // date and time
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("a");

        String amOrPm = df.format(c.getTime());

        Calendar ci = Calendar.getInstance();

        String CiDateTime = "" + ci.get(Calendar.DAY_OF_MONTH) + "/"
                + (ci.get(Calendar.MONTH) + 1) + "/" + ci.get(Calendar.YEAR)
                + " " + "  TIME-" + ci.get(Calendar.HOUR) + ":"
                + ci.get(Calendar.MINUTE) + " " + amOrPm;

        return CiDateTime;
    }

    public String getTimeZone() {
        SimpleDateFormat df = new SimpleDateFormat("a");
        String timeZone = df.getTimeZone().getDisplayName();
        return timeZone;
    }

    public String availableConnectivity() {
        ConnectivityManager conMan = (ConnectivityManager) mactivity
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        String nwrType = "";

        // wifi
        State wifi = conMan.getNetworkInfo(1).getState();

        // mobile
        State mobile = null;
        try {
            mobile = conMan.getNetworkInfo(0).getState();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        if (mobile != null && mobile == State.CONNECTED
                || mobile == State.CONNECTING) {
            nwrType = "GSM";

        } else if (wifi == State.CONNECTED
                || wifi == State.CONNECTING) {
            nwrType = "Wi-Fi";
        }


        return nwrType;
    }

    public Boolean inAirplaneMode() {

        boolean isEnabled = Settings.System.getInt(
                mactivity.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) == 1;

        String var;

        if (isEnabled) {
            return true;
        } else {
            return false;
            /*
             * // check for roaming.. if airplane mode is on, there will be no
			 * // roaming. TelephonyManager tManager = (TelephonyManager)
			 * mactivity .getSystemService(Context.TELEPHONY_SERVICE);
			 * 
			 * if (tManager.getSimState() != TelephonyManager.SIM_STATE_ABSENT)
			 * { if (isRoaming() == true) { return true; } else { return false;
			 * }
			 * 
			 * }
			 */
        }

    }

    public Boolean isRoaming() {
        try {
            if (CheckNetConnectivity(mactivity)) {

                ConnectivityManager cm = (ConnectivityManager) mactivity
                        .getSystemService(Context.CONNECTIVITY_SERVICE);

                if (cm.getActiveNetworkInfo().isRoaming())
                    return true;
                else
                    return false;
            } else
                return false;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }

    }

    public Boolean chkGPS() {
        LocationManager locManager = (LocationManager) mactivity
                .getSystemService(mactivity.LOCATION_SERVICE);

        Boolean gps = locManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        System.out.println("IsGPS Present-->>" + gps);
        return gps;
    }

    public String[] memCardDetails() {

        String[] memDetails = new String[3];

        StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
                .getPath());
        double sdAvailSize = (double) stat.getAvailableBlocks()
                * (double) stat.getBlockSize();
        double sdtotalSize = (double) stat.getBlockCount()
                * (double) stat.getBlockSize();
        // One binary gigabyte equals 1,073,741,824 bytes.
        float gigaTotal = (float) (sdtotalSize / 1073741824);
        float gigaAvailable = (float) (sdAvailSize / 1073741824);
        float usedSize = gigaTotal - gigaAvailable;
        memDetails[0] = String.valueOf(gigaTotal);
        memDetails[1] = String.valueOf(gigaAvailable);
        memDetails[2] = String.valueOf(usedSize);

        return memDetails;
    }

    // call this method where signal strength is required
    public int getSignalStrength() {


        telephonyManager = (TelephonyManager) mactivity
                .getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(Listener, Listener.LISTEN_SIGNAL_STRENGTHS);

        System.out.println("returning per_value = " + signal_value);

        return signal_value;
    }

    public void toggleGPS(boolean enable, Context mContext) {
        try {
            String provider = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            //	 Intent I = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            //	    mContext.startActivity(I);
            if (!provider.contains("gps")) { //if gps is disabled
                final Intent poke = new Intent();
                poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                mContext.sendBroadcast(poke);
                System.out.println("GPS is turn ON");
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    ;

    public void toggleGPSOFF(boolean enable, Context mContext) {
        try {
            String provider = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

            if (provider.contains("gps")) { //if gps is disabled
                final Intent poke = new Intent();
                poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                mContext.sendBroadcast(poke);
                System.out.println("GPS is turn OFF");
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Boolean RqsLocation(int cid, int lac) {

        Boolean result = false;

        String urlmmap = "http://www.google.com/glm/mmap";

        try {
            if (simPresent() == 1) {
                if (!inAirplaneMode()) {
                    if (CheckNetConnectivity(mactivity)) {
                        URL url = new URL(urlmmap);
                        URLConnection conn = url.openConnection();
                        HttpURLConnection httpConn = (HttpURLConnection) conn;
                        httpConn.setRequestMethod("POST");
                        httpConn.setDoOutput(true);
                        httpConn.setDoInput(true);
                        httpConn.setReadTimeout(60000);
                        httpConn.connect();

                        OutputStream outputStream = httpConn.getOutputStream();
                        WriteData(outputStream, cid, lac);

                        InputStream inputStream = httpConn.getInputStream();
                        DataInputStream dataInputStream = new DataInputStream(inputStream);

                        dataInputStream.readShort();
                        dataInputStream.readByte();
                        int code = dataInputStream.readInt();
                        System.out.println("code--->>" + code);
                        if (code == 0) {
                            myLatitude = dataInputStream.readInt();
                            myLongitude = dataInputStream.readInt();
                            System.out.println("myLatitude--->>" + myLatitude);
                            System.out.println("myLongitude--->>" + myLongitude);
                            result = true;

                        }
                        else
                        {
                            OpenCellID opencellid=new OpenCellID();
                            try {
                                opencellid.GetOpenCellID();
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        } catch (ProtocolException e) {
            // TODO: handle exception
            System.out.println("In Protocol Exception");
            latitude = "0";
            longitude = "0";
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("In IO Exception");
            latitude = "0";
            longitude = "0";
        }

        return result;

    }

    private void WriteData(OutputStream out, int cid, int lac)
            throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(out);
        dataOutputStream.writeShort(21);
        dataOutputStream.writeLong(0);
        dataOutputStream.writeUTF("en");
        dataOutputStream.writeUTF("Android");
        dataOutputStream.writeUTF("1.0");
        dataOutputStream.writeUTF("Mobile");
        dataOutputStream.writeByte(27);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(3);
        dataOutputStream.writeUTF("");

        dataOutputStream.writeInt(cid);
        dataOutputStream.writeInt(lac);

        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.flush();
    }

    public boolean isWIFIconnected() {
        ConnectivityManager conMan = (ConnectivityManager) mactivity
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean isConnected = false;

        // wifi
        State wifi = conMan.getNetworkInfo(1).getState();
        if (wifi == State.CONNECTED
                || wifi == State.CONNECTING) {
            isConnected = true;
        }
        return isConnected;
    }

    public boolean isWifiOn() {
        ConnectivityManager conMan = (ConnectivityManager) mactivity
                .getSystemService(Context.CONNECTIVITY_SERVICE);


        // wifi
        State wifi = conMan.getNetworkInfo(1).getState();
        if (wifi == State.CONNECTED
                || wifi == State.CONNECTING
                || wifi == State.DISCONNECTING
                || wifi == State.DISCONNECTED) {

            return true;
        }
        return false;
    }

    public class Location_Progress extends AsyncTask<String, Void, Void> {

        protected void onPreExecute() {
            mLocationManager = (LocationManager) mactivity
                    .getSystemService(Context.LOCATION_SERVICE);
            if (!mLocationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER))
                toggleGPS(false, mactivity);


        }

        @Override
        protected Void doInBackground(String... params) {
            // TODO Auto-generated method stub
            is_exception = 1;
            handler.sendEmptyMessage(0);
            return null;
        }

        protected void onPostExecute(Void unused) {

            toggleGPSOFF(false, mactivity);

        }

    }

    // signal strength
    private class MyPhoneStateListener extends PhoneStateListener {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {

            // TODO Auto-generated method stub
            super.onSignalStrengthsChanged(signalStrength);
            // Method to get Signal Strength in %
            int signalvalue = signalStrength.getGsmSignalStrength();
            signal_value = (int) signalvalue * 100 / 31;
            //Constants.SIGNAL_STRENGTH = per_value;
            setSignal_value(signal_value);
//			System.out.println("per_value in class = " + signal_value);
        }

    }

    public class OpenCellID {
        String mcc; // Mobile Country Code
        String mnc; // mobile network code
        String cellid; // Cell ID
        String lac; // Location Area Code

        Boolean error;
        String strURLSent;
        String GetOpenCellID_fullresult;

        String latitude;
        String longitude;

        public Boolean isError() {
            return error;
        }

        public void setMcc(String value) {
            mcc = value;
        }

        public void setMnc(String value) {
            mnc = value;
        }

        public void setCallID(int value) {
            cellid = String.valueOf(value);
        }

        public void setCallLac(int value) {
            lac = String.valueOf(value);
        }

        public String getLocation() {
            return (latitude + " : " + longitude);
        }

        public void groupURLSent() {
            strURLSent = "http://www.opencellid.org/cell/get?mcc=" + mcc
                    + "&mnc=" + mnc + "&cellid=" + cellid + "&lac=" + lac
                    + "&fmt=txt";
        }

        public String getstrURLSent() {
            return strURLSent;
        }

        public String getGetOpenCellID_fullresult() {
            return GetOpenCellID_fullresult;
        }

        public void GetOpenCellID() throws Exception {
            groupURLSent();
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(strURLSent);
            HttpResponse response = client.execute(request);
            GetOpenCellID_fullresult = EntityUtils.toString(response
                    .getEntity());
            spliteResult();
        }

        private void spliteResult() {
            if (GetOpenCellID_fullresult.equalsIgnoreCase("err")) {
                error = true;
            } else {
                error = false;
                String[] tResult = GetOpenCellID_fullresult.split(",");
                latitude = tResult[0];
                longitude = tResult[1];
                set_Latitude(latitude);
                set_Longitude(longitude);
            }

        }
    }
}
