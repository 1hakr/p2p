package com.p2p.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.p2p.R;

public class PanningView extends ImageView {

	private PanningViewAttacher mAttacher;
	private int mPanningDurationInMs;

	public PanningView(Context context) {
		this(context, null);
	}

	public PanningView(Context context, AttributeSet attr) {
		this(context, attr, 0);
	}

	public PanningView(Context context, AttributeSet attr, int defStyle) {
		super(context, attr, defStyle);
		readStyleParameters(context, attr);
		super.setScaleType(ScaleType.MATRIX);
	}

	/**
	 * @param context
	 * @param attributeSet
	 */
	private void readStyleParameters(Context context, AttributeSet attributeSet) {
		TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.PanningView);
		try {
			mPanningDurationInMs = a.getInt(R.styleable.PanningView_panningDurationInMs, PanningViewAttacher.DEFAULT_PANNING_DURATION_IN_MS);
		} finally {
			a.recycle();
		}
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		super.setImageDrawable(drawable);
		stopUpdateStartIfNecessary();
	}

	@Override
	public void setImageResource(int resId) {
		super.setImageResource(resId);
		stopUpdateStartIfNecessary();
	}

	@Override
	public void setImageURI(Uri uri) {
		super.setImageURI(uri);
		stopUpdateStartIfNecessary();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if(isInEditMode()){
			return;
		}
		if(w != oldw && h != oldh){
			stopUpdateStartIfNecessary();
		}
	}

	private void stopUpdateStartIfNecessary() {
		if (null != mAttacher) {
			boolean wasPanning = mAttacher.isPanning();
			mAttacher.stopPanning();
			mAttacher.reset();
			if(wasPanning) {
				mAttacher.startPanning();
			}
		}
	}

	@Override
	public void setScaleType(ScaleType scaleType) {
		throw new UnsupportedOperationException("only matrix scaleType is supported");
	}

	@Override
	protected void onDetachedFromWindow() {
		mAttacher.cleanup();
		super.onDetachedFromWindow();
	}

	public void startPanning() {
		if(null == mAttacher){
			mAttacher = new PanningViewAttacher(this, mPanningDurationInMs);
		}
		mAttacher.startPanning();
	}
	
	public boolean isPanning() {
		return mAttacher.isPanning();
	}
	
	public void stopPanning() {
		if(null != mAttacher){
			mAttacher.stopPanning();	
		}
	}
}