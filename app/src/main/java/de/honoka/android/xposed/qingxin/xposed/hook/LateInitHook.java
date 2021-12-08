package de.honoka.android.xposed.qingxin.xposed.hook;

import de.honoka.android.xposed.qingxin.xposed.init.HookInit;
import de.robv.android.xposed.XC_MethodHook;

/**
 * 可以判断初始化状态的MethodHook，在初始化完成前，Hook逻辑不会被执行
 */
public abstract class LateInitHook extends XC_MethodHook {

    public void before(MethodHookParam param) {}

    public void after(MethodHookParam param) {}

    @Override
    protected void beforeHookedMethod(MethodHookParam param) {
        if(HookInit.inited) before(param);
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) {
        if(HookInit.inited) after(param);
    }
}
