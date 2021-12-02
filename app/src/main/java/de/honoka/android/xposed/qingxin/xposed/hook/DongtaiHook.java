package de.honoka.android.xposed.qingxin.xposed.hook;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.honoka.android.xposed.qingxin.util.Logger;
import de.honoka.android.xposed.qingxin.util.TextUtils;
import de.honoka.android.xposed.qingxin.xposed.XposedMain;
import de.robv.android.xposed.XposedHelpers;
import lombok.SneakyThrows;

@SuppressWarnings("unchecked")
public class DongtaiHook extends LateInitHook {

    /**
     * 显示拦截日志时，最多只显示被拦截的评论的多少个字符
     */
    public static final int BLOCK_LOG_LENGTH_LIMIT = 50;

    @SneakyThrows
    @Override
    public void after(MethodHookParam param) {
        //class缓存
        Class<?> moduleDescClass = XposedMain.lpparam.classLoader.loadClass(
                "com.bapis.bilibili.app.dynamic.v2.ModuleDesc");
        Class<?> moduleAuthorClass = XposedMain.lpparam.classLoader.loadClass(
                "com.bapis.bilibili.app.dynamic.v2.ModuleAuthor");
        //过滤
        List<Object> dynamicItems = (List<Object>) param.getResult();
        dynamicItems = new ArrayList<>(dynamicItems);
        int blockCount = 0;
        dynamicItemsLoop:
        for(Iterator<Object> iterator = dynamicItems.iterator();
            iterator.hasNext(); ) {
            Object dynamicItem = iterator.next();
            List<Object> modules = (List<Object>) XposedHelpers.callMethod(
                    dynamicItem, "getModulesList");
            //动态内容缓存
            String content = "";
            //遍历动态的每一个部分
            //查找内容
            for(Object module : modules) {
                Object moduleDesc = XposedHelpers.callMethod(module,
                        "getModuleDesc");
                Object defaultModuleDesc = XposedHelpers.callStaticMethod(
                        moduleDescClass, "getDefaultInstance");
                if(moduleDesc != defaultModuleDesc) {
                    content = (String) XposedHelpers.callMethod(moduleDesc,
                            "getText");
                    if(XposedMain.blockRuleCache.isMatchRuleList(content,
                            XposedMain.blockRuleCache.getDongtaiList())) {
                        iterator.remove();
                        blockCount++;
                        Logger.blockLog("动态拦截（按内容）：" + TextUtils
                                .singleLine(content, BLOCK_LOG_LENGTH_LIMIT));
                        continue dynamicItemsLoop;
                    }
                }
            }
            //查找用户名
            for(Object module : modules) {
                Object moduleAuthor = XposedHelpers.callMethod(module,
                        "getModuleAuthor");
                Object defaultModuleAuthor = XposedHelpers.callStaticMethod(
                        moduleAuthorClass, "getDefaultInstance");
                if(moduleAuthor != defaultModuleAuthor) {
                    Object userInfo = XposedHelpers.callMethod(moduleAuthor,
                            "getAuthor");
                    String name = (String) XposedHelpers.callMethod(userInfo,
                            "getName");
                    if(XposedMain.blockRuleCache.isMatchRuleList(name,
                            XposedMain.blockRuleCache.getUsernameList())) {
                        iterator.remove();
                        blockCount++;
                        Logger.blockLog("动态拦截（按用户名）：" + TextUtils
                                .singleLine(content, BLOCK_LOG_LENGTH_LIMIT) +
                                        "\n用户名：" + name);
                        continue dynamicItemsLoop;
                    }
                }
            }
        }
        if(blockCount > 0) {
            Logger.toastOnBlock("拦截了" + blockCount + "条动态");
            param.setResult(dynamicItems);
        }
    }
}
