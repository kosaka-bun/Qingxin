package de.honoka.android.xposed.qingxin.xposed.filter;

import static de.honoka.android.xposed.qingxin.xposed.XposedMain.blockRuleCache;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.function.Function;

import de.honoka.android.xposed.qingxin.util.Logger;
import de.honoka.android.xposed.qingxin.xposed.XposedMain;

/**
 * 搜索栏推荐搜索词过滤器
 */
public class SearchBarFilter implements Function<String, String> {

    @Override
    public String apply(String json) {
        JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
        JsonObject data = jo.getAsJsonObject("data");
        if(!data.has("show") ||
           !data.has("uri") ||
           !data.has("goto") ||
           !data.has("exp_str")) {
            throw new NullPointerException();
        }
        String show = data.get("show").getAsString();
        //屏蔽所有热搜，或者是搜索词匹配热搜拦截规则
        if(XposedMain.mainPreference.getBlockAllHotSearchWords() ||
           blockRuleCache.isMatchRuleList(show, blockRuleCache
                        .getHotSearchWordList())) {
            Logger.blockLog("搜索框热搜拦截：" + show);
            Logger.toastOnBlock("拦截了搜索框热搜");
            return "";
        }
        //返回原值
        return json;
    }
}
