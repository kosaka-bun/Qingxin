package de.honoka.android.xposed.qingxin.util;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import de.honoka.android.xposed.qingxin.R;

public class AndroidUtils {

    public static void setActionBarIcon(AppCompatActivity activity) {
        ActionBar actionBar = activity.getSupportActionBar();
        if(actionBar == null) return;
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setLogo(R.drawable.app_icon);
        actionBar.setTitle("  " + actionBar.getTitle());
        actionBar.setDisplayUseLogoEnabled(true);
    }
}
