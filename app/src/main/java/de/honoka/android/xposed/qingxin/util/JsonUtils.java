package de.honoka.android.xposed.qingxin.util;

import com.google.gson.JsonObject;

public class JsonUtils {

    /**
     * 检查一个json对象是否包含提供的所有key
     */
    public static boolean allHas(JsonObject jo, String[] keys) {
        for(String key : keys) {
            if(!jo.has(key)) return false;
        }
        return true;
    }
}
