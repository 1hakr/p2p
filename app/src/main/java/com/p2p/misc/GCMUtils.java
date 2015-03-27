package com.p2p.misc;

import android.os.Build;

public class GCMUtils {

	static final String TAG = "GCM Fabric";
	
	public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String SENDER_ID = "680027294602";
    public static final String DISPLAY_MESSAGE_ACTION = "com.practo.droid.DISPLAY_MESSAGE";
    public static final String APP_NAME = "Fabric Droid";
    public static final String OS_NAME = "Android";
    public static final String OS_VER = Build.VERSION.RELEASE;
    public static final String PHONE_MODEL = Build.MANUFACTURER + Build.MODEL;
    
    //GCM payload
    public static final String GCM_TYPE = "type";
    public static final String GCM_TYPE_REGISTRATION_ID = "registration_id";
    public static final String GCM_TYPE_UPDATE_AVAILABLE = "upgrade_available";
    public static final String GCM_TYPE_UPDATE_REQUIRED= "upgrade_required";
    public static final String GCM_TYPE_CUSTOM_MESSAGE = "custom_message";
    public static final String GCM_TYPE_PUSH2SYNC = "push2sync";
	public static final String VERSION_UPDATED = "version_updated";
	public static final String VERSION_DEPRECATED = "version_deprecated";
	public static final String VERSION_DEPRECATED_CODE = "version_deprecated_code";
    
    public static final String GCM_UPDATE_VERSION = "new_version";
    public static final String GCM_MESSAGE = "message";
    public static final String GCM_PUSH2SYNC = "push2sync_command";

}