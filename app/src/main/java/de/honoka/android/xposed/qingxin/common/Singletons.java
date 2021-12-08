package de.honoka.android.xposed.qingxin.common;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Singletons {

    public static final Gson gson = new GsonBuilder()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public static final Gson prettyGson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
}
