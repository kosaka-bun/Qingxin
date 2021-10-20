package de.honoka.android.xposed.qingxin.xposed.filter;

import static de.honoka.android.xposed.qingxin.xposed.XposedMain.blockRuleCache;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Iterator;
import java.util.function.Function;

import de.honoka.android.xposed.qingxin.util.Logger;
import de.honoka.android.xposed.qingxin.xposed.XposedMain;

/**
 * 热搜过滤器
 */
public class HotSearchFilter implements Function<String, String> {

    public static final HotSearchFilter instance = new HotSearchFilter();

    private HotSearchFilter() {}

    @Override
    public String apply(String json) {
        JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
        JsonArray data = jo.getAsJsonArray("data");
        if(data.size() <= 0) throw new NullPointerException();
        for(JsonElement je : data) {
            JsonObject aData = je.getAsJsonObject();
            //判断是否是热搜对象
            if(aData.get("type").getAsString().equals("trending") ||
               aData.get("title").getAsString().equals("热搜")) {
                //判断是否屏蔽所有热搜
                if(XposedMain.mainPreference.getBlockAllHotSearchWords()) {
                    Logger.blockLog("屏蔽所有热搜");
                    Logger.toastOnBlock("屏蔽所有热搜");
                    return "";
                }
                //拦截条目数
                int blockCount = 0;
                //拿到热搜列表
                JsonArray hotSearchList = aData.getAsJsonObject("data")
                        .getAsJsonArray("list");
                for(Iterator<JsonElement> iterator = hotSearchList.iterator();
                    iterator.hasNext(); ) {
                    JsonObject hotSearchJo = iterator.next().getAsJsonObject();
                    //判断某条热搜是否是按规则应当屏蔽的
                    if(blockRuleCache.isBlockHotSearch(hotSearchJo)) {
                        iterator.remove();
                        Logger.blockLog("热搜拦截：" + hotSearchJo
                                .get("show_name").getAsString());
                        blockCount++;
                        continue;
                    }
                }
                if(blockCount > 0)
                    Logger.toastOnBlock("拦截了" + blockCount + "条热搜");
                continue;
            }
        }
        return jo.toString();
    }
}
