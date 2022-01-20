package de.honoka.android.xposed.qingxin.xposed.init;

import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import de.honoka.android.xposed.qingxin.xposed.XposedMain;
import de.honoka.android.xposed.qingxin.xposed.hook.ChronosHook;
import de.honoka.android.xposed.qingxin.xposed.hook.CommentHook;
import de.honoka.android.xposed.qingxin.xposed.hook.DanmakuHook;
import de.honoka.android.xposed.qingxin.xposed.hook.DongtaiHook;
import de.honoka.android.xposed.qingxin.xposed.hook.JsonHook;
import de.honoka.android.xposed.qingxin.xposed.hook.RecommendedTopicHook;
import de.honoka.android.xposed.qingxin.xposed.hook.VideoRelateHook;
import de.honoka.android.xposed.qingxin.xposed.hook.WebViewHook;
import de.honoka.android.xposed.qingxin.xposed.util.XposedUtils;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import lombok.SneakyThrows;

public class HookInit {

    /**
     * 模块是否已初始化完成，用于给LateInitHook判断是否执行hook逻辑
     */
    public static volatile boolean inited = false;

    /**
     * 与initAllHook方法结合使用，initAllHook方法会执行本类中所有附带了此注解的方法
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface InitializerMethod {}

    @SneakyThrows
    public void initAllHook() {
        for(Method method : this.getClass().getDeclaredMethods()) {
            if(method.isAnnotationPresent(InitializerMethod.class)) {
                method.setAccessible(true);
                method.invoke(this);
            }
        }
    }

    /**
     * 评论拦截初始化
     */
    @InitializerMethod
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
            Class<?> clazz = XposedMain.lpparam.classLoader.loadClass(
                    fullClassName);
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
    @InitializerMethod
    @SneakyThrows
    private void jsonHook() {
        Class<?> jsonlexerClass = XposedMain.lpparam.classLoader.loadClass(
                "com.alibaba.fastjson.parser.JSONLexer");
        Constructor<?> constructor = jsonlexerClass.getDeclaredConstructor(
                String.class, int.class);
        JsonHook jsonHook = new JsonHook();
        XposedBridge.hookMethod(constructor, jsonHook);
    }

    @InitializerMethod
    @SneakyThrows
    private void initWebViewHook() {
        XposedHelpers.findAndHookMethod(WebView.class,
                "setWebViewClient", WebViewClient.class,
                new WebViewHook());
    }

    /**
     * 弹幕拦截
     */
    @InitializerMethod
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
            classes.add(XposedMain.lpparam.classLoader.loadClass(className));
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
        Class<?> abSourceClass = XposedMain.lpparam.classLoader.loadClass(
                "com.bilibili.lib.blconfig.internal.ABSource");
        Method abSourceInvoke = XposedUtils.findMethod(abSourceClass,
                "invoke");
        XposedBridge.hookMethod(abSourceInvoke, new ChronosHook());
    }

    /**
     * 屏蔽所有推荐话题（动态页）
     */
    @InitializerMethod
    @SneakyThrows
    private void initRecommendedTopicHook() {
        Class<?> clazz = XposedMain.lpparam.classLoader.loadClass(
                "com.bapis.bilibili.app.dynamic.v2.DynAllReply");
        XposedBridge.hookMethod(clazz.getMethod("getTopicList"),
                new RecommendedTopicHook());
    }

    /**
     * 动态拦截
     */
    @InitializerMethod
    @SneakyThrows
    private void initDongtaiHook() {
        Class<?> clazz = XposedMain.lpparam.classLoader.loadClass(
                "com.bapis.bilibili.app.dynamic.v2.DynamicList");
        DongtaiHook dongtaiHook = new DongtaiHook();
        XposedBridge.hookMethod(XposedUtils.findMethod(clazz,
                "getListList"), dongtaiHook);
        XposedBridge.hookMethod(XposedUtils.findMethod(clazz,
                "getListOrBuilderList"), dongtaiHook);
    }

    /**
     * 视频播放页下方的推荐视频拦截
     */
    @InitializerMethod
    @SneakyThrows
    private void initVideoRelateHook() {
        Class<?> clazz = XposedMain.lpparam.classLoader.loadClass(
                "com.bapis.bilibili.app.view.v1.ViewReply");
        VideoRelateHook videoRelateHook = new VideoRelateHook();
        XposedBridge.hookMethod(XposedUtils.findMethod(clazz,
                "getRelatesList"), videoRelateHook);
        XposedBridge.hookMethod(XposedUtils.findMethod(clazz,
                "getRelatesOrBuilderList"), videoRelateHook);
    }

    /**
     * 播放器长按事件回调Hook
     */
    @InitializerMethod
    @SneakyThrows
    private void initPlayerLongPressHook() {
        Class<?> clazz = XposedMain.lpparam.classLoader.loadClass("tv." +
                "danmaku.biliplayerimpl.gesture.GestureService$mTouchListener$1");
        Method method = XposedUtils.findMethod(clazz, "onLongPress");
        XposedBridge.hookMethod(method, new XC_MethodReplacement() {

            @SneakyThrows
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) {
                //late init
                if(inited && !Objects.equals(XposedMain.mainPreference
                        .getDisablePlayerLongPress(), true)) {
                    XposedBridge.invokeOriginalMethod(param.method,
                            param.thisObject, param.args);
                }
                return null;
            }
        });
    }
}
