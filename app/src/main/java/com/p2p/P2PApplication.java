package com.p2p;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RequestTickle;
import com.android.volley.VolleyLog;
import com.android.volley.cache.DiskLruBasedCache.ImageCacheParams;
import com.android.volley.cache.SimpleImageLoader;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.VolleyTickle;
import com.p2p.misc.Utils;

import java.util.ArrayList;
import java.util.Locale;

import dev.dworks.libs.actionbarplus.app.ActionBarApplication;


public class P2PApplication extends ActionBarApplication {
	public static final String AUTH_HEADER =  "X-AUTH-TOKEN";
	public static final String DROID_HEADER =  "X-DROID-VERSION";
	public static final String ACCEPT =  "Accept";
	public static final String TAG = "FabricVolley";
	
    private String APP_VERSION;
    private int APP_VERSION_CODE;
	private RequestTickle mRequestTickle;
	private SimpleImageLoader mImageFetcher;
	private SimpleImageLoader mImageBgFetcher;
	private SimpleImageLoader mImagePracticeFetcher;
	private static P2PApplication sInstance;
	private RequestQueue mRequestQueue;
	private Locale current;

	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;
		
		// The following line triggers the initialization of Sentry
		//Sentry.init(this, "https://75fbbdd500e5497dae7ba833c7db7886:be061de670354feb857a47ee44baf4f5@sentry.practo.com/24");
		
		Log.d("Build Config", "Config " + BuildConfig.DEBUG);
    	try {
            final PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
    		APP_VERSION = info.versionName;
    		APP_VERSION_CODE = info.versionCode;
		} catch (NameNotFoundException e) {
			APP_VERSION = "Unknown";
			APP_VERSION_CODE = 0;
			e.printStackTrace();
		}
	}
	
	public Locale getLocale() {
		if(current == null){
			current = Locale.US;
		}
		return current;
	}
	
	public static synchronized P2PApplication getInstance() {
		return sInstance;
	}
	
	public RequestTickle getRequestTickle() {
		if (mRequestTickle == null) {
			mRequestTickle = VolleyTickle.newRequestTickle(getApplicationContext());
		}

		return mRequestTickle;
	}

	public SimpleImageLoader getImageLoader(){
		if(null == mImageFetcher){
			ImageCacheParams cacheParams = new ImageCacheParams(getApplicationContext(), Utils.IMAGE_CACHE_DIR);
			cacheParams.setMemCacheSizePercent(0.5f);

			mImageFetcher = new SimpleImageLoader(getApplicationContext(), cacheParams);
            mImageFetcher.setDefaultDrawable(R.drawable.ic_person);
			mImageFetcher.setMaxImageSize(Utils.hasMoreHeap() ? Utils.IMAGE_SIZE_BIG: Utils.IMAGE_SIZE);
			mImageFetcher.setFadeInImage(false);
		}

        return mImageFetcher;
    }
	
	public SimpleImageLoader getBgLoader(){
		if(null == mImageBgFetcher){
			
			ImageCacheParams cacheParams = new ImageCacheParams(this, Utils.IMAGE_BG_CACHE_DIR);
			cacheParams.setMemCacheSizePercent(0.25f);
			mImageBgFetcher = new SimpleImageLoader(this, cacheParams);
            mImageBgFetcher.setDefaultDrawable(R.drawable.banner);
			//mImageBgFetcher.setMaxImageSize(Utils.hasMoreHeap() ? Utils.SCALE_DOWN_SIZE : Utils.SCALE_DOWN_SIZE_SMALL);
			mImageBgFetcher.setFadeInImage(false);
		}

        return mImageBgFetcher;
    }
	
	public SimpleImageLoader getPracticeLoader(){
		if(null == mImagePracticeFetcher){
			ImageCacheParams cacheParams = new ImageCacheParams(getApplicationContext(), Utils.IMAGE_CACHE_DIR);
			cacheParams.setMemCacheSizePercent(0.5f);
			
			ArrayList<Drawable> placeHolderDrawables = new ArrayList<Drawable>();
			placeHolderDrawables.add(getResources().getDrawable(R.drawable.empty_photo1));
			
			mImagePracticeFetcher = new SimpleImageLoader(getApplicationContext(), cacheParams);
            mImagePracticeFetcher.setDefaultDrawables(placeHolderDrawables);
			mImagePracticeFetcher.setMaxImageSize(Utils.SCALE_DOWN_SIZE_SMALL);
			mImagePracticeFetcher.setFadeInImage(false);
		}

        return mImagePracticeFetcher;
    }
	
	public RequestQueue getRequestQueue() {
		// lazy initialize the request queue, the queue instance will be
		// created when it is accessed for the first time
		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}

		return mRequestQueue;
	}

	public <T> void addToRequestQueue(Request<T> req, String tag) {
		// set the default tag if tag is empty
		req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
		VolleyLog.d("Adding request to queue: %s", req.getUrl());
		getRequestQueue().add(req);
	}

	public <T> void addToRequestQueue(Request<T> req) {
		// set the default tag if tag is empty
		req.setTag(TAG);
		getRequestQueue().add(req);
	}

	public void cancelPendingRequests(Object tag) {
		if (mRequestQueue != null) {
			mRequestQueue.cancelAll(tag);
		}
	}
	
	@Override
	public void onLowMemory() {
		Runtime.getRuntime().gc();
		super.onLowMemory();
	}
}