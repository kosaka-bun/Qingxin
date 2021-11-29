package de.honoka.android.xposed.qingxin.xposed.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import de.honoka.android.xposed.qingxin.xposed.XposedMain;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import lombok.SneakyThrows;

public class XposedUtils {

    @SneakyThrows
    public static void hookMethod(String className, String methodName,
            XC_MethodHook methodHook) {
        Class<?> clazz = XposedMain.lpparam.classLoader.loadClass(className);
        List<Method> methods = new ArrayList<>();
        for(Method method : clazz.getMethods()) {
            if(method.getName().equals(methodName))
                methods.add(method);
        }
        for(Method method : methods) {
            XposedBridge.hookMethod(method, methodHook);
        }
    }

    public static void hookBefore(String className, String methodName,
            Consumer<XC_MethodHook.MethodHookParam> hooker) {
        hookMethod(className, methodName, new XC_MethodHook() {

            @SneakyThrows
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                hooker.accept(param);
            }
        });
    }

    public static void hookAfter(String className, String methodName,
            Consumer<XC_MethodHook.MethodHookParam> hooker) {
        hookMethod(className, methodName, new XC_MethodHook() {

            @SneakyThrows
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                hooker.accept(param);
            }
        });
    }

    public static Method findMethod(Class<?> clazz, String methodName) {
        for(Method method : clazz.getDeclaredMethods()) {
            if(method.getName().equals(methodName)) return method;
        }
        return null;
    }
}
