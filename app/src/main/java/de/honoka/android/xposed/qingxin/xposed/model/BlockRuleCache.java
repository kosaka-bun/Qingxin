package de.honoka.android.xposed.qingxin.xposed.model;

import com.google.gson.JsonObject;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import de.honoka.android.xposed.qingxin.entity.BlockRule;
import de.honoka.android.xposed.qingxin.util.Logger;
import de.honoka.android.xposed.qingxin.util.PatternUtils;
import de.honoka.android.xposed.qingxin.xposed.filter.MainPageFilter;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Xposed模块加载后会从主程序中读取所有作用域的规则，存放在此对象的每一个List中
 */
@Data
@Accessors(chain = true)
public class BlockRuleCache {

    private List<BlockRule> videoTitleList;

    private List<BlockRule> videoSubAreaList;

    private List<BlockRule> videoChannelList;

    private List<BlockRule> usernameList;

    private List<BlockRule> commentList;

    private List<BlockRule> danmakuList;

    private List<BlockRule> hotSearchWordList;

    private List<BlockRule> dongtaiList;

    /**
     * 判断某个字符串是否匹配某个规则列表里面的某条规则
     */
    public boolean isMatchRuleList(String str, List<BlockRule> blockRuleList) {
        return blockRuleList.stream().parallel().anyMatch(blockRule -> {
            switch(blockRule.getType()) {
                //按关键词
                case BlockRule.RuleType.KEYWORD: {
                    //在不区分大小写的情况下，消息是否包含这条规则的内容（关键词）
                    if(StringUtils.containsIgnoreCase(str,
                            blockRule.getContent())) {
                        return true;
                    }
                    break;
                }
                //按正则表达式
                case BlockRule.RuleType.PATTERN: {
                    if(PatternUtils.containsMatch(str, "(?i)(" +
                            blockRule.getContent() + ")")) {
                        return true;
                    }
                    break;
                }
            }
            return false;
        });
    }

    /**
     * 根据评论的内容，和评论作用域规则列表，判断评论是否应当拦截
     * @param message 评论的文本内容
     */
    public boolean isBlockCommentMessage(String message) {
        //根据评论作用域里的规则来判断
        return isMatchRuleList(message, commentList);
    }

    /**
     * 判断某个用户名是否应当拦截
     */
    public boolean isBlockUsername(String username) {
        return isMatchRuleList(username, usernameList);
    }

    /**
     * 判断首页推荐项目是否是一个推广项目
     */
    public boolean isMainPageItemPublicity(JsonObject item) {
        String cardGoto = item.get("card_goto").getAsString();
        //明确的广告
        if(item.has("ad_info")) return true;
        //轮播图，或番剧、纪录片
        if(cardGoto.contains("bangumi") || cardGoto.contains("banner")) return true;
        //电影
        if(cardGoto.contains("special")) return true;
        //游戏
        if(cardGoto.contains("game")) return true;
        //番剧
        if(cardGoto.equals("pgc")) return true;
        //另一种轮播图
        if(cardGoto.equals("new_tunnel")) return true;
        //其实一般来说，只要这个card_goto不是av，就都可以认为是推广
        if(!cardGoto.equals("av")) {
            Logger.testLog("可能未拦截的card_goto：" + cardGoto);
        }
        return false;
    }

    /**
     * 判断首页推荐项目是否是按规则应当拦截的
     */
    public boolean isBlockMainPageItem(JsonObject item) {
        //推荐项目不一定是视频！要先判断是不是视频
        String cardGoto = item.get("card_goto").getAsString();
        switch(cardGoto) {
            //如果是视频
            case "av": {
                //按视频标题判断
                String videoTitle = item.get("title").getAsString();
                if(isMatchRuleList(videoTitle, videoTitleList)) return true;
                //按用户名判断
                JsonObject args = item.getAsJsonObject("args");
                String username = args.get("up_name").getAsString();
                if(isBlockUsername(username)) return true;
                //按视频分区判断
                String rname = args.get("rname").getAsString();
                if(isMatchRuleList(rname, videoSubAreaList)) return true;
                //按视频频道判断
                String tname = args.get("tname").getAsString();
                if(isMatchRuleList(tname, videoChannelList)) return true;
                break;
            }
            //其他项目
            default: {
                //按项目标题判断
                String title = MainPageFilter.getMainPageItemTitle(item);
                if(isMatchRuleList(title, videoTitleList)) return true;
                break;
            }
        }
        return false;
    }

    /**
     * 判断某个轮播图项目是否应当被拦截
     */
    public boolean isBlockBannerItem(JsonObject bannerItem) {
        //按标题判断
        String title = MainPageFilter.getBannerItemTitle(bannerItem);
        if(isMatchRuleList(title, videoTitleList)) return true;
        return false;
    }

    /**
     * 判断某个热搜条目是否应当被拦截
     */
    public boolean isBlockHotSearch(JsonObject hotSearchJo) {
        String keyword = hotSearchJo.get("keyword").getAsString();
        String showName = hotSearchJo.get("show_name").getAsString();
        //按搜索词匹配
        if(isMatchRuleList(keyword, hotSearchWordList)) return true;
        //按热搜条目的文字来匹配
        if(isMatchRuleList(showName, hotSearchWordList)) return true;
        return false;
    }

    /**
     * 判断某个弹幕内容是否应当被拦截
     */
    public boolean isBlockDanmakuContent(String content) {
        return isMatchRuleList(content, danmakuList);
    }
}
