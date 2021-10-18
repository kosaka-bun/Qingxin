package de.honoka.android.xposed.qingxin.xposed;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

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
import de.honoka.android.xposed.qingxin.util.ExceptionUtils;
import de.honoka.android.xposed.qingxin.util.Logger;
import de.honoka.android.xposed.qingxin.xposed.hook.CommentHook;
import de.honoka.android.xposed.qingxin.xposed.hook.ResponseBodyHook;
import de.honoka.android.xposed.qingxin.xposed.model.BlockRuleCache;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
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

	private final Type blockRuleListType = new TypeToken<List<BlockRule>>() {}.getType();

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
				//初始化
				//不在新线程中进行初始化可能会使APP闪退
				new Thread(() -> {
					try {
						init();
						if(mainPreference.getTestMode()) {
							Logger.toast("清心模块加载成功", Toast.LENGTH_SHORT);
						}
					} catch(IllegalArgumentException iae) {
						String eMsg = iae.getMessage();
						if(eMsg == null) return;
						//初始化时读取不到provider
						if(eMsg.contains("Unknown authority") ||
								eMsg.contains("Unknown URI")) {
							XposedBridge.log(iae);
							Logger.writeToFile(ExceptionUtils.transfer(iae));
							String toastMessage = "清心模块加载失败，" +
									"请检查清心模块的自启动权限是否已开启";
							Logger.toast(toastMessage, Toast.LENGTH_LONG);
						} else {
							//其他问题
							reportProblem(iae);
						}
					} catch(Throwable t) {
						//其他问题
						reportProblem(t);
					}
				}).start();
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
		Logger.testLog(blockRuleCache.toString());
		//注册更新receiver
		registerUpdateReceiver();
		//初始化所有hook
		initCommentHook();
		initResponseBodyHook();
	}

	/**
	 * 用Toast、日志和文件三种方式报告问题
	 */
	private void reportProblem(Throwable t) {
		XposedBridge.log(t);
		Logger.writeToFile(ExceptionUtils.transfer(t));
		String toastMessage = "清心模块加载失败，请到bilibili数据目录下查看日志文件";
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
		CommentHook commentHook = new CommentHook(blockRuleCache);
		//为每一个要hook的方法绑定逻辑
		for(Method m : methods) {
			XposedBridge.hookMethod(m, commentHook);
		}
	}

	/**
	 * OkHttp响应体解析方法hook
	 */
	@SneakyThrows
	private void initResponseBodyHook() {
		Class<?> responseBodyClass = lpparam.classLoader.loadClass(
				"okhttp3.f0");
		ResponseBodyHook responseBodyHook = new ResponseBodyHook(blockRuleCache);
		XposedHelpers.findAndHookMethod(responseBodyClass,
				"A", responseBodyHook);
		XposedHelpers.findAndHookMethod(responseBodyClass,
				"c", responseBodyHook);
	}
}
