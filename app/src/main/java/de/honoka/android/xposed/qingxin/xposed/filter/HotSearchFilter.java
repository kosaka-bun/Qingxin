package de.honoka.android.xposed.qingxin.xposed.filter;

import static de.honoka.android.xposed.qingxin.xposed.XposedMain.blockRuleCache;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Iterator;
import java.util.Objects;

import de.honoka.android.xposed.qingxin.util.Logger;
import de.honoka.android.xposed.qingxin.xposed.XposedMain;
import de.honoka.android.xposed.qingxin.xposed.init.HookInit;
import de.honoka.android.xposed.qingxin.xposed.util.JsonFilter;

/**
 * 热搜过滤器
 */
@SuppressWarnings("ConstantConditions")
public class HotSearchFilter extends JsonFilter {

    @Override
    public boolean isJsonWillBeFiltered(JsonElement je) {
        JsonArray data = je.getAsJsonObject()
                .getAsJsonArray("data");
        if(data.size() <= 0) return false;
        for(JsonElement jeInData : data) {
            JsonObject aData = jeInData.getAsJsonObject();
            //获取这个对象里的type和title
            //type和title并不一定存在
            String type = null, title = null;
            if(aData.has("type"))
                type = aData.get("type").getAsString();
            if(aData.has("title"))
                title = aData.get("title").getAsString();
            //判断是否是热搜对象
            if(Objects.equals(type, "trending") ||
               Objects.equals(title, "热搜"))
                return true;
        }
        return false;
    }

    @Override
    public String doFilter(JsonElement je) {
        JsonArray data = je.getAsJsonObject()
                .getAsJsonArray("data");
        for(Iterator<JsonElement> dataIterator = data.iterator();
            dataIterator.hasNext(); ) {
            JsonObject aData = dataIterator.next().getAsJsonObject();
            //获取这个对象里的type和title
            //type和title并不一定存在
            String type = null, title = null;
            if(aData.has("type"))
                type = aData.get("type").getAsString();
            if(aData.has("title"))
                title = aData.get("title").getAsString();
            //判断是否是热搜对象
            if(Objects.equals(type, "trending") ||
               Objects.equals(title, "热搜")) {
                //判断是否屏蔽所有热搜
                if(!HookInit.inited ||
                   XposedMain.mainPreference.getBlockAllHotSearchWords()) {
                    dataIterator.remove();
                    Logger.blockLog("屏蔽所有热搜");
                    Logger.toastOnBlock("屏蔽所有热搜");
                    continue;
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
                    if(HookInit.inited &&
                       blockRuleCache.isBlockHotSearch(hotSearchJo)) {
                        iterator.remove();
                        Logger.blockLog("热搜拦截：" + hotSearchJo
                                .get("show_name").getAsString());
                        blockCount++;
                    }
                }
                if(blockCount > 0)
                    Logger.toastOnBlock("拦截了" + blockCount + "条热搜");
            } else if(Objects.equals(type, "recommend") ||
                      Objects.equals(title, "搜索发现")) {
                //是否屏蔽所有搜索发现
                if(!HookInit.inited ||
                   XposedMain.mainPreference.getBlockAllHotSearchWords()) {
                    dataIterator.remove();
                    Logger.toastOnBlock("拦截了搜索框热搜");
                }
            }
        }
        return je.toString();
    }
}
