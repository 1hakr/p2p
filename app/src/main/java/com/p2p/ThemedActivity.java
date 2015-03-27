package com.p2p;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import com.p2p.misc.Utils;

import dev.dworks.libs.actionbarplus.app.ActionBarActivityPlus;

/**
 * Created by HaKr on 28/01/15.
 */
public class ThemedActivity extends ActionBarActivityPlus {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (Utils.hasLollipop()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        super.onCreate(savedInstanceState);
    }
}
