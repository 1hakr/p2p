package com.p2p.ui.seekbar;

import android.graphics.drawable.StateListDrawable;

public interface PhasedAdapter {

    public int getCount();

    public StateListDrawable getItem(int position);

}