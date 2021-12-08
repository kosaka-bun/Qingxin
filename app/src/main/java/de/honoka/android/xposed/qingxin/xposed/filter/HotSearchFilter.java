package de.honoka.android.xposed.qingxin.xposed.filter;

import static de.honoka.android.xposed.qingxin.xposed.XposedMain.blockRuleCache;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;

import de.honoka.android.xposed.qingxin.util.Logger;
import de.honoka.android.xposed.qingxin.xposed.XposedMain;

/**
 * 热搜过滤器
 */
public class HotSearchFilter implements Function<String, String> {

    @Override
    public String apply(String json) {
        JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
        JsonArray data = jo.getAsJsonArray("data");
        if(data.size() <= 0) throw new NullPointerException();
        for(JsonElement je : data) {
            JsonObject aData = je.getAsJsonObject();
            try {
                handleDataObject(aData);
            } catch(BlockAllHotSearchException bahse) {
                //抛出此异常表示要屏蔽所有热搜
                return "";
            }
        }
        return jo.toString();
    }

    /**
     * 对热搜json中的data数组里的一个对象进行处理
     */
    private void handleDataObject(JsonObject aData) {
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
            if(XposedMain.mainPreference.getBlockAllHotSearchWords()) {
                Logger.blockLog("屏蔽所有热搜");
                Logger.toastOnBlock("屏蔽所有热搜");
                throw new BlockAllHotSearchException();
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
        }
    }

    /**
     * 抛出此异常表示要屏蔽所有热搜
     */
    private static class BlockAllHotSearchException
            extends RuntimeException {}
}
