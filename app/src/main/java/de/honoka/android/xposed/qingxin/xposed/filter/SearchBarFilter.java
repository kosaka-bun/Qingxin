package de.honoka.android.xposed.qingxin.xposed.filter;

import static de.honoka.android.xposed.qingxin.xposed.XposedMain.blockRuleCache;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.honoka.android.xposed.qingxin.util.JsonUtils;
import de.honoka.android.xposed.qingxin.util.Logger;
import de.honoka.android.xposed.qingxin.xposed.XposedMain;
import de.honoka.android.xposed.qingxin.xposed.init.HookInit;
import de.honoka.android.xposed.qingxin.xposed.util.JsonFilter;

/**
 * 搜索栏推荐搜索词过滤器（6.74.0版本前有效）
 */
@SuppressWarnings("ConstantConditions")
public class SearchBarFilter extends JsonFilter {

    @Override
    public boolean isJsonWillBeFiltered(JsonElement je) {
        JsonObject data = je.getAsJsonObject()
                .getAsJsonObject("data");
        return JsonUtils.allHas(data, new String[] {
                "show", "word", "exp_str"
        }) || JsonUtils.allHas(data, new String[] {
                "show", "uri", "goto", "exp_str"
        });
    }

    @Override
    public String doFilter(JsonElement je) {
        JsonObject data = je.getAsJsonObject()
                .getAsJsonObject("data");
        String show = data.get("show").getAsString();
        //屏蔽所有热搜，或者是搜索词匹配热搜拦截规则
        if(!HookInit.inited ||
           XposedMain.mainPreference.getBlockAllHotSearchWords() ||
           blockRuleCache.isMatchRuleList(show, blockRuleCache
                        .getHotSearchWordList())
        ) {
            Logger.blockLog("搜索框热搜拦截：" + show);
            Logger.toastOnBlock("拦截了搜索框热搜");
            return "";
        }
        //返回原值
        return je.toString();
    }
}
