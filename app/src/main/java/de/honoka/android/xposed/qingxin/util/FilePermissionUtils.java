package de.honoka.android.xposed.qingxin.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

public class FilePermissionUtils {

    private final Activity activity;

    private static final String[] FILE_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE
            //Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public FilePermissionUtils(Activity activity) {
        this.activity = activity;
    }

    public boolean hasPermissions() {
        for(String permission : FILE_PERMISSIONS) {
            if(activity.checkSelfPermission(permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void requestPermissions(int requestCode) {
        List<String> requests = new ArrayList<>();
        for(String permission : FILE_PERMISSIONS) {
            if(activity.checkSelfPermission(permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                requests.add(permission);
            }
        }
        if(requests.size() > 0) {
            activity.requestPermissions(requests.toArray(new String[0]),
                    requestCode);
        }
    }
}
