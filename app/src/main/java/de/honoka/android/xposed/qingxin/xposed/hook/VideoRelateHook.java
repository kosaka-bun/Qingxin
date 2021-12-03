package de.honoka.android.xposed.qingxin.xposed.hook;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.honoka.android.xposed.qingxin.util.Logger;
import de.honoka.android.xposed.qingxin.xposed.XposedMain;
import de.robv.android.xposed.XposedHelpers;

/**
 * 拦截视频播放页下方的推荐视频
 */
@SuppressWarnings("unchecked")
public class VideoRelateHook extends LateInitHook {

    @Override
    public void after(MethodHookParam param) {
        List<Object> relates = (List<Object>) param.getResult();
        relates = new ArrayList<>(relates);
        int blockCount = 0;
        for(Iterator<Object> iterator = relates.iterator();
            iterator.hasNext(); ) {
            Object relate = iterator.next();
            Object author = XposedHelpers.callMethod(relate,
                    "getAuthor");
            String authorName = (String) XposedHelpers.callMethod(
                    author, "getName");
            String title = (String) XposedHelpers.callMethod(relate,
                    "getTitle");
            if(XposedMain.blockRuleCache.isMatchRuleList(title,
                    XposedMain.blockRuleCache.getVideoTitleList())) {
                iterator.remove();
                blockCount++;
                Logger.blockLog("播放页推荐视频拦截（按标题）：" + title);
                continue;
            }
            if(XposedMain.blockRuleCache.isMatchRuleList(authorName,
                    XposedMain.blockRuleCache.getUsernameList())) {
                iterator.remove();
                blockCount++;
                Logger.blockLog("播放页推荐视频拦截（按用户名）：" +
                        title + "\n用户名：" + authorName);
            }
        }
        if(blockCount > 0) {
            Logger.toastOnBlock("拦截了" + blockCount + "条播放页推荐视频");
            param.setResult(relates);
        }
    }
}
