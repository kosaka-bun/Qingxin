package de.honoka.android.xposed.qingxin.xposed.filter;

import static de.honoka.android.xposed.qingxin.xposed.XposedMain.blockRuleCache;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Iterator;
import java.util.Set;

import de.honoka.android.xposed.qingxin.util.Logger;
import de.honoka.android.xposed.qingxin.xposed.XposedMain;
import de.honoka.android.xposed.qingxin.xposed.init.HookInit;
import de.honoka.android.xposed.qingxin.xposed.util.JsonFilter;

/**
 * 首页推荐过滤器
 */
public class MainPageFilter extends JsonFilter {

    //region 首页推荐项左下角的图标，也就是“UP”或者粉色手机图标的图片链接

    private String gotoIconUrl;

    private String gotoIconNightUrl;

    //endregion

    @Override
    public String apply(String json) {
        //根据字符串提取出来的主对象，所有修改操作都基于它来完成
        JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
        JsonArray items = jo.getAsJsonObject("data")
                .getAsJsonArray("items");
        //region 前置判断，判断json是不是首页推荐数据
        //首页推荐视频条目数不可能为0
        if(items.size() <= 0) throw new NullPointerException();
        //条目必须包含card_type和card_goto
        JsonObject testItem = items.get(0).getAsJsonObject();
        if(!testItem.has("card_type") ||
           !testItem.has("card_goto"))
            throw new NullPointerException();
        //endregion
        //过滤、计数
        int blockCount = 0;
        for(Iterator<JsonElement> iterator = items.iterator();
            iterator.hasNext(); ) {
            JsonObject item = iterator.next().getAsJsonObject();
            //对推荐项目进行优化
            optimizeMainPageItem(item);
            //判断是否进行规则匹配拦截
            if(!HookInit.inited) {
                //未加载配置，无条件拦截轮播图
                if(item.get("card_goto").getAsString().equals("banner")) {
                    iterator.remove();
                    blockCount++;
                    Logger.blockLog("首页推荐拦截：" +
                            getMainPageItemTitle(item));
                    continue;
                }
            }
            //判断是否是首页推广（创作推广、游戏、会员购、轮播图、纪录片、番剧等）
            if(blockRuleCache.isMainPageItemPublicity(item)) {
                //是推广，判断屏蔽开关
                //noinspection ConstantConditions
                if(XposedMain.mainPreference.getBlockAllMainPagePublicity()) {
                    iterator.remove();
                    blockCount++;
                    Logger.blockLog("首页推荐拦截【屏蔽所有推广】：" +
                            getMainPageItemTitle(item));
                    continue;
                }
            }
            //判断是不是轮播图
            if(item.get("card_goto").getAsString().equals("banner")) {
                //遍历轮播图项目
                JsonArray bannerItems = item.getAsJsonArray(
                        "banner_item");
                for(Iterator<JsonElement> bannerItemIterator =
                    bannerItems.iterator(); bannerItemIterator.hasNext(); ) {
                    JsonObject bannerItem = bannerItemIterator.next()
                            .getAsJsonObject();
                    if(blockRuleCache.isBlockBannerItem(bannerItem)) {
                        bannerItemIterator.remove();
                        blockCount++;
                        Logger.blockLog("轮播图推荐拦截：" +
                                getBannerItemTitle(bannerItem));
                        continue;
                    }
                }
                //轮播图不用再进行下面的判断，它不是一般的首页推荐项
                continue;
            }
            //判断是否是按规则应当屏蔽的内容（一般的首页推荐项目，即除了轮播图以外
            //的其他项目）
            if(blockRuleCache.isBlockMainPageItem(item)) {
                iterator.remove();
                blockCount++;
                Logger.blockLog("首页推荐拦截【规则】：" + getMainPageItemTitle(item));
                continue;
            }
            //到达此处就可以认为这个推荐项目是不用拦截的
        }
        if(blockCount > 0)
            Logger.toastOnBlock("拦截了" + blockCount + "条首页推荐");
        //String handledJson = jo.toString();
        //输出测试信息
        //Logger.testLog("输出json");
        //Logger.testLog(handledJson);
        //Logger.testLog("输出json完成");
        //return handledJson;
        return jo.toString();
    }

    /**
     * 从首页推荐轮播图中的某一个项目中获取标题
     */
    public static String getBannerItemTitle(JsonObject bannerItem) {
        //json解析非常容易抛异常，必须尽可能考虑解析错误的情况
        try {
            //查找包含banner的那个键
            Set<String> keys = bannerItem.keySet();
            for(String key : keys) {
                if(key.contains("banner")) {
                    //这个键对应的值是一个jsonObject，里面的title就是标题
                    JsonObject bannerInfo = bannerItem.getAsJsonObject(key);
                    return bannerInfo.get("title").getAsString();
                }
            }
            return "";
        } catch(Throwable t) {
            return "";
        }
    }

    /**
     * 从首页推荐项中获取标题
     */
    public static String getMainPageItemTitle(JsonObject item) {
        try {
            switch(item.get("card_goto").getAsString()) {
                case "ad_web_s": {
                    return item.getAsJsonObject("ad_info")
                            .getAsJsonObject("creative_content")
                            .get("title").getAsString();
                }
                case "banner":
                    return "【轮播图】";
                case "av":
                case "bangumi":
                case "ad_av":
                default: {
                    return item.get("title").getAsString();
                }
            }
        } catch(Throwable t) {
            return "";
        }
    }

    /**
     * 对首页推荐项进行优化的逻辑
     */
    private void optimizeMainPageItem(JsonObject item) {
        JsonElement gotoItem = item.get("goto");
        if(gotoItem == null) return;
        //判断是否是竖屏视频
        if(gotoItem.getAsString().equals("vertical_av")) {
            //noinspection ConstantConditions
            if(!HookInit.inited ||
               XposedMain.mainPreference.getConvertAllVerticalAv()) {
                convertVerticalVideoItem(item);
                //日志
                Logger.blockLog("还原竖屏视频：" + getMainPageItemTitle(item));
                //Logger.testLog(Singletons.prettyGson.toJson(item));
            }
        }
        //缓存左下角图片链接
        if(item.get("goto").getAsString().equals("av")) {
            cacheGotoIconUrl(item);
        }
    }

    /**
     * 还原首页推荐项的竖屏视频
     */
    private void convertVerticalVideoItem(JsonObject item) {
        //修改goto
        item.remove("goto");
        item.addProperty("goto", "av");
        //修改uri
        String uri = item.get("uri").getAsString();
        uri = uri.replace("bilibili://story",
                "bilibili://video");
        item.remove("uri");
        item.addProperty("uri", uri);
        //移除两个多余的键
        //item.remove("official_icon");
        //item.remove("ff_cover");
        //region 替换卡片左下角的手机图标
        //（这东西让我误以为还原没有成功，找了大半天普通视频和竖屏视频的区别）
        JsonObject gotoIcon = item.getAsJsonObject("goto_icon");
        gotoIcon.remove("icon_url");
        gotoIcon.remove("icon_night_url");
        if(gotoIconUrl == null) {
            gotoIcon.addProperty("icon_url", "https://i0.hdslb.com/bfs/activity-plat/static/20210507/0977767b2e79d8ad0a36a731068a83d7/t7zoEFhbzI.png");
        } else {
            gotoIcon.addProperty("icon_url", gotoIconUrl);
        }
        if(gotoIconNightUrl == null) {
            gotoIcon.addProperty("icon_night_url", "https://i0.hdslb.com/bfs/activity-plat/static/20210507/0977767b2e79d8ad0a36a731068a83d7/y7ewtkWg7d.png");
        } else {
            gotoIcon.addProperty("icon_night_url", gotoIconNightUrl);
        }
        //endregion
    }

    /**
     * 缓存首页推荐项左下角图片链接
     */
    private void cacheGotoIconUrl(JsonObject item) {
        try {
            //有的goto为av的item可能不包含goto_icon
            //不用try包裹会因为产生nullpointer使所有拦截失效
            JsonObject gotoIcon = item.get("goto_icon").getAsJsonObject();
            if(gotoIconUrl == null) {
                gotoIconUrl = gotoIcon.get("icon_url").getAsString();
            }
            if(gotoIconNightUrl == null) {
                gotoIconNightUrl = gotoIcon.get("icon_night_url")
                        .getAsString();
            }
        } catch(Throwable t) {
            //ignore
            //Logger.testLog(ExceptionUtils.transfer(t));
        }
    }
}
