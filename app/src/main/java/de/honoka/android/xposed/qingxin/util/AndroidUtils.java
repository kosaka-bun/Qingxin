package de.honoka.android.xposed.qingxin.util;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import de.honoka.android.xposed.qingxin.R;
import lombok.SneakyThrows;

public class AndroidUtils {

    public static void setActionBarIcon(AppCompatActivity activity) {
        ActionBar actionBar = activity.getSupportActionBar();
        if(actionBar == null) return;
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setLogo(R.drawable.app_icon);
        actionBar.setTitle("  " + actionBar.getTitle());
        actionBar.setDisplayUseLogoEnabled(true);
    }

    @SneakyThrows
    public static int getVersionCode(Application application) {
        PackageManager pm = application.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(application.getPackageName(), 0);
        return pi.versionCode;
    }
}
