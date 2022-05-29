package de.honoka.android.xposed.qingxin.service;

import android.content.Context;

import java.io.FileInputStream;

import de.honoka.android.xposed.qingxin.common.Singletons;
import de.honoka.android.xposed.qingxin.entity.MainPreference;
import de.honoka.android.xposed.qingxin.ui.fragment.MainPreferenceFragment;
import de.honoka.android.xposed.qingxin.util.FileUtils;
import lombok.SneakyThrows;

/**
 * 在不加载配置界面的情况下读取配置文件
 */
public class MainPreferenceService {

    //context由ContentProvider提供
    private final Context context;

    public MainPreferenceService(Context context) {
        this.context = context;
    }

    @SneakyThrows
    public String readPreferenceJson() {
        try(FileInputStream fileInputStream = context.openFileInput(
                MainPreferenceFragment.MAIN_PROP_FILENAME)) {
            return FileUtils.streamToString(fileInputStream);
        }
    }

    public MainPreference getPreference() {
        return Singletons.gson.fromJson(readPreferenceJson(), MainPreference.class);
    }
}
