package com.p2p.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.p2p.R;

/**
 * Created by nicolas on 06/03/14.
 */
public class AppRate {

    private static final String PREFS_NAME = "app_rate_prefs";
    private final String KEY_COUNT = "count";
    private final String KEY_CLICKED = "clicked";
    private Activity activity;
    private ViewGroup viewGroup;
    private String text;
    private int initialLaunchCount = 5;
    private RetryPolicy policy = RetryPolicy.EXPONENTIAL;
    private OnShowListener onShowListener;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private int delay = 0;
    private ViewGroup mainView;
    private int mDrawableLeft = -1;
    private int mLayoutId = R.layout.layout_rate_app;
    private View.OnClickListener mTextViewClickListener;

    private int mViewGravity = Gravity.TOP;
    private AppRate(Activity activity) {
        this.activity = activity;
    }
    private boolean isShown = false;

    private AppRate(Activity activity, ViewGroup viewGroup) {
        this.activity = activity;
        this.viewGroup = viewGroup;
    }

    public static AppRate with(Activity activity) {
        AppRate instance = new AppRate(activity);
        instance.text = "Like Practo?";//activity.getString(R.string.dra_rate_app);
        instance.settings = activity.getSharedPreferences(PREFS_NAME, 0);
        instance.editor = instance.settings.edit();
        return instance;
    }

    public static AppRate with(Activity activity, ViewGroup viewGroup, String message) {
        AppRate instance = new AppRate(activity, viewGroup);
        instance.text = message;//activity.getString(R.string.dra_rate_app);
        instance.settings = activity.getSharedPreferences(PREFS_NAME, 0);
        instance.editor = instance.settings.edit();
        return instance;
    }

    public static AppRate with(Activity activity, ViewGroup viewGroup, String message, int drawableLeft) {
        AppRate instance = new AppRate(activity, viewGroup);
        instance.text = message;//activity.getString(R.string.dra_rate_app);
        instance.settings = activity.getSharedPreferences(PREFS_NAME, 0);
        instance.editor = instance.settings.edit();
        instance.mDrawableLeft = drawableLeft;
        return instance;
    }

    public void setTextViewClickListener(View.OnClickListener listener) {
        this.mTextViewClickListener = listener;
    }

    /**
     * Text to be displayed in the viewGroup
     *
     * @param text text to be displayed
     * @return the {@link AppRate} instance
     */
    public AppRate text(String text) {
        this.text = text;
        return this;
    }

    /**
     * Text to be displayed in the viewGroup
     *
     * @param textRes text ressource to be displayed
     * @return the {@link AppRate} instance
     */
    public AppRate text(int textRes) {
        this.text = activity.getString(textRes);
        return this;
    }

    /**
     * Initial times {@link AppRate} has to be called before the viewGroup is shown
     *
     * @param initialLaunchCount times count
     * @return the {@link AppRate} instance
     */
    public AppRate initialLaunchCount(int initialLaunchCount) {
        this.initialLaunchCount = initialLaunchCount;
        return this;
    }

    /**
     * Policy to use to show the {@link AppRate} again
     *
     * @param policy the {@link RetryPolicy} to be used
     * @return the {@link AppRate} instance
     */
    public AppRate retryPolicy(RetryPolicy policy) {
        this.policy = policy;
        return this;
    }

    /**
     * Listener used to get {@link AppRate} lifecycle
     *
     * @param onShowListener the listener
     * @return the {@link AppRate} instance
     */
    public AppRate listener(OnShowListener onShowListener) {
        this.onShowListener = onShowListener;
        return this;
    }

    /**
     * Delay the {@link AppRate showing time}
     * @param delay the delay in ms
     * @return the {@link AppRate} instance
     */
    public AppRate delay(int delay) {
        this.delay = delay;
        return this;
    }


    /**
     * Check and show if showing the viewGroup is needed
     */
    public void checkAndShow() {
        incrementViews();

        boolean clicked = settings.getBoolean(KEY_CLICKED, false);
        if (clicked) return;
        int count = settings.getInt(KEY_COUNT, 0);
        if (count == initialLaunchCount) {
            showAppRate();
        } else if (policy == RetryPolicy.INCREMENTAL && count % initialLaunchCount == 0) {
            showAppRate();
        }else if (policy == RetryPolicy.EXPONENTIAL && count % initialLaunchCount == 0 && isPowerOfTwo(count / initialLaunchCount)) {
            showAppRate();
        }
    }

    /**
     * Reset the count to start over
     */
    public void reset() {
        editor.putInt(KEY_COUNT, 0);
        editor.apply();
    }

    /**
     * Will force the {@link AppRate} to show
     */
    public void forceShow() {
        showAppRate();
    }

    private void incrementViews() {

        editor.putInt(KEY_COUNT, settings.getInt(KEY_COUNT, 0) + 1);
        editor.apply();
    }

    public void showIfNotShown() {
        boolean clicked = settings.getBoolean(KEY_CLICKED, false);
        if (clicked) return;
        showAppRate();
    }

    private void showAppRate() {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(null != viewGroup){
            mainView = (ViewGroup) inflater.inflate(mLayoutId, ((ViewGroup) viewGroup), false);
        }
        else{
            mainView = (ViewGroup) inflater.inflate(mLayoutId, null);
        }

        ImageView close = (ImageView) mainView.findViewById(R.id.close);

        TextView textView = (TextView) mainView.findViewById(R.id.text);
        TextView yesView = (TextView) mainView.findViewById(R.id.yes);
        TextView noView = (TextView) mainView.findViewById(R.id.no);

        textView.setText(text);
        if (mDrawableLeft != -1) {
            textView.setCompoundDrawablesWithIntrinsicBounds(mDrawableLeft, 0, 0, 0);
        }

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAllViews(mainView);
                if (onShowListener != null)onShowListener.onRateAppDismissed();
            }
        });

        // If listener has been set explicitly, set the listener instead of default redirection to play-store
        if (mTextViewClickListener != null) {
            textView.setOnClickListener(mTextViewClickListener);
        } else {
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + activity.getPackageName()));
//                if(Utils.isIntentAvailable(activity, intent)) {
                    activity.startActivity(intent);
//                }
                    if (onShowListener != null)onShowListener.onRateAppClicked();
                    hideAllViews(mainView);
                    editor.putBoolean(KEY_CLICKED, true);
                    editor.apply();

                }
            });
        }

        if (noView != null) {
            noView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideAllViews(mainView);
                    if (onShowListener != null)onShowListener.onRateAppDenied();
                }
            });
        }

        if (yesView != null) {
            if (mTextViewClickListener != null) {
                yesView.setOnClickListener(mTextViewClickListener);
            } else {
                yesView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onShowListener != null)onShowListener.onRateAppClicked();
                        hideAllViews(mainView);
                        editor.putBoolean(KEY_CLICKED, true);
                        editor.apply();

                    }
                });
            }
        }

        if (delay > 0) {
            activity.getWindow().getDecorView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    displayViews(mainView);
                }
            }, delay);
        } else {
            displayViews(mainView);
        }

        isShown = true;
    }

    public void dismiss() {
        hideAllViews(mainView);
        if (onShowListener != null)onShowListener.onRateAppDismissed();
    }

    private void hideAllViews(final ViewGroup mainView) {
        Animation hideAnimation = AnimationUtils.loadAnimation(activity, android.R.anim.fade_out);
        hideAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(null != viewGroup){
                    viewGroup.setVisibility(View.GONE);
                    viewGroup.removeAllViews();
                }
                else {
                    mainView.removeAllViews();
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mainView.startAnimation(hideAnimation);
        isShown = false;
    }

    private void displayViews(ViewGroup mainView) {
        if(null != viewGroup){
            viewGroup.setVisibility(View.VISIBLE);
            viewGroup.addView(mainView);
        }
        else{
            LinearLayout mainLayout = new LinearLayout(activity);
            mainLayout.setGravity(mViewGravity == Gravity.BOTTOM ? Gravity.BOTTOM : Gravity.TOP);

            LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

            LinearLayout.LayoutParams mainLayParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
            mainLayout.addView(mainView,params);
            activity.addContentView(mainLayout,mainLayParam);
        }

        Animation fadeInAnimation = AnimationUtils.loadAnimation(activity, android.R.anim.fade_in);
        mainView.startAnimation(fadeInAnimation);

        if (onShowListener != null) onShowListener.onRateAppShowing();
    }

    public interface OnShowListener {
        void onRateAppShowing();

        void onRateAppDismissed();

        void onRateAppDenied();

        void onRateAppClicked();
    }

    public enum  RetryPolicy {
        /**
         * Will retry each time initial count has been triggered
         * Ex: if initial is set to 3, it will be shown on the 3rd, 6th, 9th, ... times
         */
        INCREMENTAL,
        /**
         * Will retry exponentially to be less intrusive
         * Ex: if initial is set to 3, it will be shown on the 3rd, 6th, 12th, ... times
         */
        EXPONENTIAL,
        /**
         * Will never retry
         */
        NONE;
    }

    /**
     * Convert a size in dp to a size in pixels
     * @param context the {@link android.content.Context} to be used
     * @param dpi size in dp
     * @return the size in pixels
     */
    public static int convertDPItoPixels(Context context, int dpi) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpi * scale + 0.5f);
    }

    public static boolean isPowerOfTwo(int x)    {
        return (x & (x - 1)) == 0;
    }

    public void setGravity(int gravity){
        mViewGravity = gravity;
    }

    public int getLayoutId() {
        return mLayoutId;
    }

    public void setLayoutId(int mLayoutType) {
        this.mLayoutId = mLayoutType;
    }

    public boolean isShown() {
        return isShown;
    }
}