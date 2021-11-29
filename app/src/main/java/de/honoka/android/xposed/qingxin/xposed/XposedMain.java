package de.honoka.android.xposed.qingxin.xposed;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.honoka.android.xposed.qingxin.common.Constant;
import de.honoka.android.xposed.qingxin.common.Singletons;
import de.honoka.android.xposed.qingxin.entity.BlockRule;
import de.honoka.android.xposed.qingxin.entity.MainPreference;
import de.honoka.android.xposed.qingxin.provider.QingxinProvider;
import de.honoka.android.xposed.qingxin.util.CodeUtils;
import de.honoka.android.xposed.qingxin.util.ExceptionUtils;
import de.honoka.android.xposed.qingxin.util.Logger;
import de.honoka.android.xposed.qingxin.xposed.hook.CommentHook;
import de.honoka.android.xposed.qingxin.xposed.hook.DanmakuHook;
import de.honoka.android.xposed.qingxin.xposed.hook.JsonHook;
import de.honoka.android.xposed.qingxin.xposed.hook.WebViewHook;
import de.honoka.android.xposed.qingxin.xposed.model.BlockRuleCache;
import de.honoka.android.xposed.qingxin.xposed.util.XposedUtils;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import lombok.SneakyThrows;

@SuppressLint("DiscouragedPrivateApi")
public class XposedMain implements IXposedHookLoadPackage {

    /**
     * 加载包后获得的加载参数（包含如包名、应用的类加载器等）
     */
    public static XC_LoadPackage.LoadPackageParam lpparam;

    /**
     * 被hook应用的application对象
     */
    public static Application hookApplication;

    /**
     * 被hook应用的内容解析器，用于获取主程序的配置和规则数据
     */
    public static ContentResolver contentResolver;

    /**
     * 模块基本配置
     */
    public static MainPreference mainPreference =
            MainPreference.getDefaultPreference();

    /**
     * 各作用域拦截规则列表，及各作用域的拦截逻辑
     */
    public static BlockRuleCache blockRuleCache;

    /**
     * 用来表示List<BlockRule>类型的对象，用于解析BlockRule的json数组
     * 到List
     */
    private final Type blockRuleListType =
            new TypeToken<List<BlockRule>>() {}.getType();

    /**
     * 模块是否已初始化完成，用于给LateInitHook判断是否执行hook逻辑
     */
    private volatile static boolean inited = false;

    /**
     * 用于保存Unhook对象，便于在Hook到后取消Hook
     */
    private static class Holder<T> {

        public T obj;
    }

    @SneakyThrows
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        //包名验证
        String packageName = "tv.danmaku.bili";
        if(!lpparam.packageName.equals(packageName)) return;
        //加载模块
        XposedMain.lpparam = lpparam;
        try {
            //region hook获取应用的application
            Method callApplicationOnCreate = Instrumentation.class
                    .getDeclaredMethod("callApplicationOnCreate",
                            Application.class);
            Holder<XC_MethodHook.Unhook> unhookHolder = new Holder<>();
            unhookHolder.obj = XposedBridge.hookMethod(callApplicationOnCreate,
                    new XC_MethodHook() {

                @SneakyThrows
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    //hook到后马上取消hook
                    if(unhookHolder.obj != null)
                        unhookHolder.obj.unhook();
                    //获取application
                    if(hookApplication == null) {
                        hookApplication = (Application) param.args[0];
                    }
                    afterGetApplication();
                }
            });
            //endregion
            /* 初始化所有hook
             * hook的初始化理论上要先于配置与规则的初始化
             * 若被hook的方法在配置初始化完成之前被调用，则LateInitHook类
             * 会根据init的值忽略掉本次调用，不执行hook逻辑 */
            initAllHook();
            Logger.testLogForce("hook加载完成");
        } catch(Throwable t) {
            reportProblem("hook方法时出现问题，请查看日志", t);
        }
    }

    /**
     * 得到application对象后执行的逻辑
     */
    private void afterGetApplication() {
        //初始化，构建初始化逻辑，指定初始化的信息报告逻辑
        if(inited) return;
        Logger.testLogForce("得到Application对象，开始加载配置与规则");
        //不在新线程中进行初始化可能会使APP闪退
        Runnable initAction = () -> {
            try {
                init();
                inited = true;
                if(mainPreference.getTestMode()) {
                    Logger.testLog("清心模块加载成功");
                    Logger.toast("清心模块加载成功", Toast.LENGTH_SHORT);
                }
            } catch(IllegalArgumentException iae) {
                String eMsg = iae.getMessage();
                if(eMsg == null) return;
                //初始化时读取不到provider
                if(eMsg.contains("Unknown authority") ||
                        eMsg.contains("Unknown URI")) {
                    reportProblem(Constant.ErrorMessage
                            .INIT_NO_AUTO_BOOT_PERMISSION, iae);
                } else {
                    //其他问题
                    reportProblem(Constant.ErrorMessage
                            .INIT_UNKNOWN_ERROR, iae);
                }
            } catch(Throwable t) {
                //其他问题
                reportProblem(Constant.ErrorMessage
                        .INIT_UNKNOWN_ERROR, t);
            }
        };
        //判断当前系统版本是否低于或等于Android 7.1
        //若是，则在mainLooper中进行初始化
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            //Build.VERSION_CODES.N_MR1 = 25 (Android 7.1)
            /* 用于初始化的线程，它将建立Looper，然后将初始化逻辑传递给
             * mainLooper，然后开启loop循环后阻塞
             * 它只负责传递初始化逻辑，理论上并不需要多长时间就能执行完成 */
            class InitThread extends Thread {

                /**
                 * 本线程的looper
                 */
                private Looper thisLooper;

                @Override
                public void run() {
                    Looper.prepare();
                    thisLooper = Looper.myLooper();
                    new Handler(Looper.getMainLooper())
                            .post(initAction);
                    Looper.loop();
                }
            }
            InitThread initThread = new InitThread();
            initThread.start();
            //监听InitThread线程的线程，在一定时间后停止其loop
            new Thread((CodeUtils.ThrowsRunnable) () -> {
                initThread.join(1000);
                if(initThread.isAlive()) {
                    initThread.thisLooper.quit();
                    initThread.interrupt();
                }
            }).start();
        } else {
            new Thread(initAction).start();
        }
    }

    /**
     * 加载基本配置和规则数据
     */
    private void init() {
        contentResolver = hookApplication.getContentResolver();
        //读取MainPreference
        Bundle mainPreferenceBundle = contentResolver.call(
                QingxinProvider.QINGXIN_PROVIDER_URI,
                QingxinProvider.RequestMethod.MAIN_PREFERENCE,
                null, null);
        mainPreference = Singletons.gson.fromJson(mainPreferenceBundle
                .getString("data"), MainPreference.class);
        //region 读取所有作用域的拦截规则
        blockRuleCache = new BlockRuleCache();
        //videoTitle
        blockRuleCache.setVideoTitleList(requestBlockRuleList(
                BlockRule.Region.VIDEO_TITLE));
        //videoSubArea
        blockRuleCache.setVideoSubAreaList(requestBlockRuleList(
                BlockRule.Region.VIDEO_SUB_AREA));
        //videoChannel
        blockRuleCache.setVideoChannelList(requestBlockRuleList(
                BlockRule.Region.VIDEO_CHANNEL));
        //username
        blockRuleCache.setUsernameList(requestBlockRuleList(
                BlockRule.Region.USERNAME));
        //comment
        blockRuleCache.setCommentList(requestBlockRuleList(
                BlockRule.Region.COMMENT));
        //danmaku
        blockRuleCache.setDanmakuList(requestBlockRuleList(
                BlockRule.Region.DANMAKU));
        //hotSearchWord
        blockRuleCache.setHotSearchWordList(requestBlockRuleList(
                BlockRule.Region.HOT_SEARCH_WORD));
        //dongtai
        blockRuleCache.setDongtaiList(requestBlockRuleList(
                BlockRule.Region.DONGTAI));
        //endregion
        Logger.testLog(mainPreference.toString());
        //Logger.testLog(blockRuleCache.toString());
        //注册更新receiver
        registerUpdateReceiver();
    }

    public static boolean isInited() {
        return inited;
    }

    /**
     * 用Toast、日志和文件三种方式报告问题
     */
    private void reportProblem(String toastMessage, Throwable t) {
        Logger.testLogForce(ExceptionUtils.transfer(t));
        Logger.toast(toastMessage, Toast.LENGTH_LONG);
    }

    private List<BlockRule> requestBlockRuleList(String region) {
        String ruleListJson = contentResolver.call(
                QingxinProvider.QINGXIN_PROVIDER_URI,
                QingxinProvider.RequestMethod.BLOCK_RULE,
                region, null).getString("data");
        return Singletons.gson.fromJson(ruleListJson, blockRuleListType);
    }

    /**
     * 动态注册配置更新监听器
     */
    private void registerUpdateReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.UPDATE_BROADCAST_ACTION);
        hookApplication.registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String type = intent.getStringExtra("type");
                if(type == null) return;
                switch(type) {
                    case Constant.UpdateType.MAIN_PREFERENCE: {
                        mainPreference = Singletons.gson.fromJson(
                                intent.getStringExtra("data"),
                                MainPreference.class);
                        Logger.testLog("配置已更新：" + mainPreference.toString());
                        break;
                    }
                    case Constant.UpdateType.BLOCK_RULE: {
                        updateBlockRuleCache(intent.getStringExtra("data"));
                        break;
                    }
                }
            }
        }, intentFilter);
    }

    private void updateBlockRuleCache(String json) {
        BlockRule blockRule = Singletons.gson.fromJson(json, BlockRule.class);
        if(blockRule.getVideoTitle())
            blockRuleCache.getVideoTitleList().add(blockRule);
        if(blockRule.getVideoSubArea())
            blockRuleCache.getVideoSubAreaList().add(blockRule);
        if(blockRule.getVideoChannel())
            blockRuleCache.getVideoChannelList().add(blockRule);
        if(blockRule.getUsername())
            blockRuleCache.getUsernameList().add(blockRule);
        if(blockRule.getComment())
            blockRuleCache.getCommentList().add(blockRule);
        if(blockRule.getDanmaku())
            blockRuleCache.getDanmakuList().add(blockRule);
        if(blockRule.getHotSearchWord())
            blockRuleCache.getHotSearchWordList().add(blockRule);
        if(blockRule.getDongtai())
            blockRuleCache.getDongtaiList().add(blockRule);
        Logger.testLog("收到新的规则：" + blockRule.toString());
    }

    private void initAllHook() {
        initCommentHook();
        jsonHook();
        initWebViewHook();
        initDanmakuHook();
    }

    /**
     * 评论拦截初始化
     */
    @SneakyThrows
    private void initCommentHook() {
        //列出要hook的类
        List<String> classNames = Arrays.asList(
                "MainListReply", "ReplyInfo", "DialogListReply");
        //拼接类名，获取class对象
        List<Class<?>> classes = new ArrayList<>();
        for(String className : classNames) {
            //加载类
            String fullClassName = "com.bapis.bilibili.main.community" +
                    ".reply.v1." + className;
            Class<?> clazz = lpparam.classLoader.loadClass(fullClassName);
            //添加到列表
            classes.add(clazz);
        }
        //从这些类中获取要hook的方法
        List<Method> methods = new ArrayList<>();
        //特殊的方法名（主要是一些获取置顶评论的方法，需要对这些置顶评论进行判断）
        List<String> specialMethodNames = Arrays.asList(
                "getAdminTop", "getReplies", "getTopReplies",
                "getUpTop", "getVoteTop"
        );
        for(Class<?> aClass : classes) {
            //列出这些类中的所有方法
            for(Method m : aClass.getDeclaredMethods()) {
                //将返回值类型为List的方法，或特殊方法名的方法添加到列表中
                if(m.getReturnType().equals(List.class) ||
                        specialMethodNames.contains(m.getName())) {
                    methods.add(m);
                }
            }
        }
        //初始化拦截逻辑
        CommentHook methodHook = new CommentHook();
        //为每一个要hook的方法绑定逻辑
        for(Method m : methods) {
            XposedBridge.hookMethod(m, methodHook);
        }
    }

    /**
     * json提取的hook
     */
    @SneakyThrows
    private void jsonHook() {
        Class<?> jsonlexerClass = lpparam.classLoader.loadClass(
                "com.alibaba.fastjson.parser.JSONLexer");
        Constructor<?> constructor = jsonlexerClass.getDeclaredConstructor(
                String.class, int.class);
        JsonHook jsonHook = new JsonHook();
        XposedBridge.hookMethod(constructor, jsonHook);
    }

    @SneakyThrows
    private void initWebViewHook() {
        XposedHelpers.findAndHookMethod(WebView.class,
                "setWebViewClient", WebViewClient.class,
                new WebViewHook());
    }

    /**
     * 弹幕拦截
     */
    @SneakyThrows
    private void initDanmakuHook() {
        //region 要Hook的类名
        List<String> classNames = Arrays.asList(
                "com.bapis.bilibili.broadcast.message.main.DanmukuEvent",
                "com.bapis.bilibili.broadcast.message.tv.DmSegLiveReply",
                "com.bapis.bilibili.community.service.dm.v1.DmSegMobileReply",
                "com.bapis.bilibili.community.service.dm.v1.DmSegOttReply",
                "com.bapis.bilibili.community.service.dm.v1.DmSegSDKReply",
                //"com.bapis.bilibili.community.service.dm.v1.DmViewReply",
                //"com.bapis.bilibili.community.service.dm.v1.DmWebViewReply",
                "com.bapis.bilibili.tv.interfaces.dm.v1.DmSegMobileReply",
                //"com.bapis.bilibili.tv.interfaces.dm.v1.DmViewReply",
                "com.bilibili.playerbizcommon.api.PlayerDanmukuReplyListInfo"
        );
        //endregion
        //根据类名获得类对象
        List<Class<?>> classes = new ArrayList<>();
        for(String className : classNames) {
            classes.add(lpparam.classLoader.loadClass(className));
        }
        //拿到这些类中的返回值类型为List的对象
        List<Method> methods = new ArrayList<>();
        for(Class<?> aClass : classes) {
            //遍历某个类中的所有方法，将返回值类型为List的方法添加到列表中
            Method[] declaredMethods = aClass.getDeclaredMethods();
            for(Method declaredMethod : declaredMethods) {
                if(declaredMethod.getReturnType().equals(List.class)) {
                    methods.add(declaredMethod);
                }
            }
        }
        //为这些方法绑定Hook
        DanmakuHook danmakuHook = new DanmakuHook();
        for(Method method : methods) {
            XposedBridge.hookMethod(method, danmakuHook);
        }
        //特殊方法的hook（使弹幕使用java层加载而不是native，与chronos有关）
        //6.50.0
        CodeUtils.doIgnoreException(() -> XposedUtils.hookAfter(
                "tv.danmaku.chronos.wrapper.f", "f",
                param -> param.setResult(false)));
    }
}
