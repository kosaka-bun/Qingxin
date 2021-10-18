package de.honoka.android.xposed.qingxin.xposed.hook;

import de.honoka.android.xposed.qingxin.xposed.XposedMain;
import de.robv.android.xposed.XC_MethodHook;

/**
 * 可以判断初始化状态的MethodHook，在初始化完成前，Hook逻辑不会被执行
 */
public abstract class LateInitHook extends XC_MethodHook {

	void before(MethodHookParam param) {}

	void after(MethodHookParam param) {}

	@Override
	protected void beforeHookedMethod(MethodHookParam param) {
		if(XposedMain.isInited()) before(param);
	}

	@Override
	protected void afterHookedMethod(MethodHookParam param) {
		if(XposedMain.isInited()) after(param);
	}
}
