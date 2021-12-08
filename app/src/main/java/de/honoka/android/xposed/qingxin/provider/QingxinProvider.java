package de.honoka.android.xposed.qingxin.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.InputStream;
import java.util.List;

import de.honoka.android.xposed.qingxin.common.Singletons;
import de.honoka.android.xposed.qingxin.dao.BlockRuleDao;
import de.honoka.android.xposed.qingxin.entity.BlockRule;
import de.honoka.android.xposed.qingxin.entity.MainPreference;
import de.honoka.android.xposed.qingxin.service.MainPreferenceService;
import de.honoka.android.xposed.qingxin.util.FileUtils;
import lombok.SneakyThrows;

public class QingxinProvider extends ContentProvider {

    public static final String QINGXIN_PROVIDER_AUTHORITIES =
            "de.honoka.android.xposed.qingxin.provider.QingxinProvider";

    public static final Uri QINGXIN_PROVIDER_URI =
            Uri.parse("content://" + QINGXIN_PROVIDER_AUTHORITIES);

    @SneakyThrows
    @Nullable
    @Override
    public Bundle call(@NonNull String method, @Nullable String arg,
                       @Nullable Bundle extras) {
        Bundle bundle = new Bundle();
        String data = null;
        switch(method) {
            case RequestMethod.MAIN_PREFERENCE: {
                MainPreferenceService mainPreferenceService =
                        new MainPreferenceService(getContext());
                //如果模块主程序从未启动过，这个方法就会报告no such file
                try {
                    data = mainPreferenceService.readPreferenceJson();
                } catch(Throwable t) {
                    data = Singletons.gson.toJson(MainPreference
                            .getDefaultPreference());
                }
                break;
            }
            case RequestMethod.BLOCK_RULE: {
                BlockRuleDao blockRuleDao = new BlockRuleDao(getContext());
                List<BlockRule> rules = blockRuleDao.getListOfRegion(arg);
                data = Singletons.gson.toJson(rules);
                break;
            }
            case RequestMethod.ASSETS: {
                AssetManager assetManager = getContext().getAssets();
                InputStream stream = assetManager.open(arg);
                data = FileUtils.streamToString(stream);
                break;
            }
        }
        bundle.putString("data", data);
        return bundle;
    }

    public interface RequestMethod {

        String MAIN_PREFERENCE = "main_preference";

        String BLOCK_RULE = "block_rule";

        String ASSETS = "assets";
    }

    //region 无关方法

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    //endregion
}
