package com.jayis4176.mobile_client_v1;

import android.app.Activity;
import android.widget.TextView;

/**
 * Created by JAYIS4176 on 2015/1/30.
 */
public class Utility {
    public void showString (String string, Activity act) {
        TextView textView = new TextView(act);
        textView.setTextSize(20);
        textView.setText(string);
        act.setContentView(textView);
    }
}
