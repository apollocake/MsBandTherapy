package com.aftonmartin.android.msbandtherapy;

import android.app.Activity;
import android.widget.TextView;

public class UIAsyncUtils {
    private static UIAsyncUtils uiAsyncUtils = null;
    private static Activity mainActivity = null;

    protected UIAsyncUtils() {
    }

    public void appendToUI(final int resourceId, final String string) {
        if (mainActivity != null) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final TextView resultsView = (TextView) mainActivity.findViewById(resourceId);
                    resultsView.setText(string);
                }
            });
        }
    }

    public synchronized static UIAsyncUtils getInstance() {
        if (uiAsyncUtils == null) {
            uiAsyncUtils = new UIAsyncUtils();
        }
        return uiAsyncUtils;
    }

    public synchronized static void setUIActivity(Activity activity) {
        if (mainActivity == null) {
            mainActivity = activity;
        }
    }

}
