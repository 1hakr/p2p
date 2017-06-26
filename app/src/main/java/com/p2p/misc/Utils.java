package com.p2p.misc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.p2p.BuildConfig;
import com.p2p.P2PApplication;
import com.p2p.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by HaKr on 20-Sep-14.
 */
public class Utils {

    // cache
    public static final int IMAGE_SIZE_BIG = 100;
    public static final int IMAGE_SIZE = 50;
    public static final int SCALE_DOWN_SIZE = 1024;
    public static final int SCALE_DOWN_SIZE_SMALL = 512;
    public static final String IMAGE_CACHE_DIR = "thumbs";
    public static final String IMAGE_BG_CACHE_DIR = "bgs";

    public static final String API_URL = "http://p2p.practodev.com";
    static final String TAG = "Utils";
    public static final String BUNDLE_RESTAURANT = "bundle_restaurant";
    public static final String BUNDLE_MENU = "bundle_menu";
    public static final String BUNDLE_LOCATION = "bundle_latlong";

    public static final String KEY_NAME = "key_name";
    public static final String KEY_DEVICE_ID = "key_device_id";
    public static final String KEY_PHOTO_URL = "key_photo_url";
    public static final String KEY_PROFILE_ID = "key_profile_id";

    public static final String GOOGLE_PLACES_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    public static final String GOOGLE_PHOTOS_URL = "https://maps.googleapis.com/maps/api/place/photo";
    public static final String GOOGLE_PLACES_API_KEY = "AIzaSyArBmLVB_OqHZAiQo7zoSzbnAiDjkPZ03o";
    public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", P2PApplication.getInstance().getLocale());

    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * SECOND;
    private static final int HOUR = 60 * MINUTE;
    private static final int DAY = 24 * HOUR;
    public static final TimeZone CONFERENCE_TIMEZONE = TimeZone.getTimeZone("America/Los_Angeles");
    private static final SimpleDateFormat dateFormatDisplay = new SimpleDateFormat("MMMM yyyy", P2PApplication.getInstance().getLocale());

    public static final long MONTH_IN_MILLIS = DateUtils.DAY_IN_MILLIS * 7;
    public static final long YEAR_IN_MILLIS = DateUtils.DAY_IN_MILLIS * 7 * 12;
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(int type, Bundle bundle);
    }

    public static void log(String t, String s) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        String topic = "PRACTO-DROID";
        if (t != null) {
            topic = topic + " " + t;
        }
        if (!TextUtils.isEmpty(s)) {
            android.util.Log.w(topic, s);
        }
    }

    public static void log(String s) {
        log(null, s);
    }

    /**
     * Interface for components that are internally scrollable left-to-right.
     */
    public static interface HorizontallyScrollable {
        /**
         * Return {@code true} if the component needs to receive right-to-left
         * touch movements.
         *
         * @param origX
         *            the raw x coordinate of the initial touch
         * @param origY
         *            the raw y coordinate of the initial touch
         */

        public boolean interceptMoveLeft(float origX, float origY);

        /**
         * Return {@code true} if the component needs to receive left-to-right
         * touch movements.
         *
         * @param origX
         *            the raw x coordinate of the initial touch
         * @param origY
         *            the raw y coordinate of the initial touch
         */
        public boolean interceptMoveRight(float origX, float origY);
    }

    public static void crossfade(View fadeIn, final View fadeOut, int duration, final AnimatorListenerAdapter listener) {

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        Log.i("FadeIn", "FadeOut alpha: " + String.valueOf(ViewHelper.getAlpha(fadeOut)));
        ViewHelper.setAlpha(fadeIn, 0f);
        fadeIn.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        ViewPropertyAnimator.animate(fadeIn).alpha(1f).setDuration(duration).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (null != listener)
                    listener.onAnimationEnd(animation);
                super.onAnimationEnd(animation);
            }
        }).start();

        // Zoom content view
		/*
		 * ViewPropertyAnimator.animate(fadeIn) .scaleX((float) 1.10)
		 * .scaleY((float) 1.10) .setDuration(duration - 50).start();
		 */

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        Log.i("FadeOut", "FadeIn alpha: " + String.valueOf(ViewHelper.getAlpha(fadeIn)));
        ViewPropertyAnimator.animate(fadeOut).alpha(0f).setDuration(duration).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fadeOut.setVisibility(View.GONE);
                ViewHelper.setScaleX(fadeOut, 1.0f);
                ViewHelper.setScaleY(fadeOut, 1.0f);
            }
        }).start();
    }


    public static boolean hasFroyo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasIceCreamSandwich() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean hasJellyBeanMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    public static boolean hasJellyBeanMR2() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
    public static boolean hasMoreHeap(){
        return Runtime.getRuntime().maxMemory() > 20971520;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isLowRamDevice(Context context) {
        if(Utils.hasKitKat()){
            final ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            return am.isLowRamDevice();
        }
        return !hasMoreHeap();
    }

    public static boolean isTablet(Context context) {
        return context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }


    public static String getBooleanValue(boolean bool) {
        return bool ? "1" : "0";
    }

    public static String getBooleanStringValue(int value) {
        return value == 1 ? "true" : "false";
    }

    public static boolean isNetConnected(Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
            return false;
        }
        return true;
    }

    /**
     * Simple network connection check.
     *
     * @param context
     * @return true if connection is present else false
     */
    public static boolean checkConnection(Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
            Toast.makeText(context, "No Internet", Toast.LENGTH_LONG).show();
            Utils.log(TAG, "checkConnection - no connection found");
            return false;
        }
        return true;
    }

    public static String capitalizeFirstLetter(String original) {
        if (original.length() == 0)
            return original;
        return original.substring(0, 1).toUpperCase(P2PApplication.getInstance().getLocale()) + original.substring(1);
    }


    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isActivityAlive(Activity activity) {
        if (null == activity
                || (null != activity && Utils.hasJellyBeanMR1() ? activity.isDestroyed() : activity.isFinishing())) {
            return false;
        }
        return true;
    }

    public static boolean isNetConnect(Context mContext) {

        ConnectivityManager connec = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED
                || connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED)
            return true;

        return false;
    }

    public static String getPlacesPhoto(String reference, int height){
        return GOOGLE_PHOTOS_URL
                + "?maxwidth=500"
                + "&photoreference="+reference
                + "&key=" + GOOGLE_PLACES_API_KEY;
    }


    public static String getRelativeTime(Context context, long time) {
        Resources r = context.getResources();
        String date = "";
        long now = Calendar.getInstance(P2PApplication.getInstance().getLocale()).getTimeInMillis();
        boolean past = (now >= time);
        long duration = Math.abs(now - time);
        long minResolution = DateUtils.MINUTE_IN_MILLIS;

        if (duration > YEAR_IN_MILLIS) {
            long count = duration / YEAR_IN_MILLIS;
            int resId = R.plurals.abbrev_num_years_ago;
            String format = r.getQuantityString(resId, (int) count);
            date = String.format(format, count);
        } else if (duration > MONTH_IN_MILLIS) {
            long count = duration / MONTH_IN_MILLIS;
            int resId = R.plurals.abbrev_num_months_ago;
            String format = r.getQuantityString(resId, (int) count);
            date = String.format(format, count);
        } else {
            date = (String) DateUtils.getRelativeTimeSpanString(time, now, minResolution);
        }
        return date;
    }
}
