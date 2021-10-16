package de.honoka.android.xposed.qingxin.xposed;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.google.gson.reflect.TypeToken;

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
import de.honoka.android.xposed.qingxin.util.Logger;
import de.honoka.android.xposed.qingxin.xposed.hook.CommentHook;
import de.honoka.android.xposed.qingxin.xposed.model.BlockRuleCache;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import lombok.SneakyThrows;

@SuppressLint("DiscouragedPrivateApi")
@SuppressWarnings("JavaReflectionMemberAccess")
public class XposedMain implements IXposedHookLoadPackage {

	private XC_LoadPackage.LoadPackageParam lpparam;

	public static Application hookApplication;

	private ContentResolver contentResolver;

	private XC_MethodHook.Unhook unhook;

	public static MainPreference mainPreference;

	private BlockRuleCache blockRuleCache;

	private Type blockRuleListType = new TypeToken<List<BlockRule>>() {}.getType();

	@SneakyThrows
	@Override
	public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
		//包名验证
		String packageName = "tv.danmaku.bili";
		if(!lpparam.packageName.equals(packageName)) return;
		//初始化
		this.lpparam = lpparam;
		//hook获取应用的application
		Method attach = Application.class.getDeclaredMethod(
				"attach", Context.class);
		unhook = XposedBridge.hookMethod(attach, new XC_MethodHook() {

			@SneakyThrows
			@Override
			protected void afterHookedMethod(MethodHookParam param) {
				//hook到后马上取消hook
				unhook.unhook();
				//获取application
				if(hookApplication == null) {
					hookApplication = (Application) param.thisObject;
				}
				new Thread(() -> init()).start();
			}
		});
	}

	/**
	 * 在hook到application后执行
	 */
	private void init() {
		contentResolver = hookApplication.getContentResolver();
		//读取MainPreference
		Bundle mainPreferenceBundle = contentResolver.call(
				QingxinProvider.QINGXIN_PROVIDER_AUTHORITIES,
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
		Logger.testLog(blockRuleCache.toString());
		//注册更新receiver
		registerUpdateReceiver();
		//初始化所有hook
		initCommentHook();
	}

	private List<BlockRule> requestBlockRuleList(String region) {
		String ruleListJson = contentResolver.call(
				QingxinProvider.QINGXIN_PROVIDER_AUTHORITIES,
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
				switch(type) {
					case Constant.UpdateType.MAIN_PREFERENCE: {
						mainPreference = Singletons.gson.fromJson(
								intent.getStringExtra("data"),
								MainPreference.class);
						Logger.testLog("配置已更新：" + mainPreference.toString());
						break;
					}
				}
			}
		}, intentFilter);
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
			className = "com.bapis.bilibili.main.community.reply.v1." + className;
			classes.add(lpparam.classLoader.loadClass(className));
		}
		//从这些类中获取要hook的方法
		List<Method> methods = new ArrayList<>();
		for(Class<?> aClass : classes) {
			//列出这些类中的所有方法
			for(Method m : aClass.getDeclaredMethods()) {
				//将返回值类型为List的方法添加到列表中
				if(m.getReturnType().equals(List.class)) {
					methods.add(m);
				}
			}
		}
		//初始化拦截逻辑
		CommentHook commentHook = new CommentHook(blockRuleCache);
		//为每一个要hook的方法绑定逻辑
		for(Method m : methods) {
			XposedBridge.hookMethod(m, commentHook);
		}
	}
}
