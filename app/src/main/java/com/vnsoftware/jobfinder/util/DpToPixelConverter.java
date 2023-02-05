package com.vnsoftware.jobfinder.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

public class DpToPixelConverter {
    public static final int convertDpToPixels(Context context, int dp) {
        Resources r = context.getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics()
        );
        return px;
    }
}
