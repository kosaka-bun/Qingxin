package de.honoka.android.xposed.qingxin.xposed.init;

import java.lang.reflect.Method;

import de.honoka.android.xposed.qingxin.util.Logger;
import de.robv.android.xposed.XposedBridge;
import lombok.SneakyThrows;

public class HookInit {

    /**
     * 模块是否已初始化完成，用于给LateInitHook判断是否执行hook逻辑
     */
    public static volatile boolean inited = false;

    @SneakyThrows
    public void initAllHook() {
        AllInitializers allInitializers = new AllInitializers();
        for(Method method : allInitializers.getClass().getDeclaredMethods()) {
            if(method.isAnnotationPresent(InitializerMethod.class)) {
                try {
                    method.setAccessible(true);
                    method.invoke(allInitializers);
                } catch(Throwable t) {
                    Logger.testLogForce(method.getName() + "加载失败");
                    XposedBridge.log(t);
                }
            }
        }
    }
}
