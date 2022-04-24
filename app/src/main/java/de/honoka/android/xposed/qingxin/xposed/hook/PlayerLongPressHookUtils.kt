package de.honoka.android.xposed.qingxin.xposed.hook

import de.honoka.android.xposed.qingxin.util.CodeUtils
import de.honoka.android.xposed.qingxin.xposed.XposedMain
import de.honoka.android.xposed.qingxin.xposed.init.HookInit
import de.honoka.android.xposed.qingxin.xposed.util.XposedUtils
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Method

object PlayerLongPressHookUtils {

    private val replacement: XC_MethodReplacement =
            object : XC_MethodReplacement() {

        override fun replaceHookedMethod(param: MethodHookParam): Any {
            //late init
            if(HookInit.inited &&
               XposedMain.mainPreference.disablePlayerLongPress != true) {
                return XposedBridge.invokeOriginalMethod(param.method,
                        param.thisObject, param.args)
            }
            return true
        }
    }

    @JvmStatic
    fun doReplace() {
        replace1()
        replace2()
    }

    private fun classNameListToClassList(vararg classNameList: String):
            List<Class<*>> {
        val classes = ArrayList<Class<*>>()
        classNameList.forEach {
            classes.add(XposedMain.lpparam.classLoader.loadClass(it))
        }
        return classes
    }

    private fun hookInvokeMethod(vararg classNameList: String) {
        CodeUtils.doIgnoreException {
            for(aClass in classNameListToClassList(*classNameList)) {
                for(method in aClass.declaredMethods) {
                    if(method.name != "invoke") continue
                    if(method.returnType == Boolean::class.java ||
                       method.returnType == Boolean::class.javaPrimitiveType) {
                        XposedBridge.hookMethod(method, replacement)
                    }
                }
            }
        }
    }

    /**
     * 6.59.0以前
     */
    private fun replace1() {
        val className = "tv.danmaku.biliplayerimpl.gesture" +
                ".GestureService\$mTouchListener\$1"
        CodeUtils.doIgnoreException {
            val clazz: Class<*> = XposedMain.lpparam.classLoader
                    .loadClass(className)
            val method: Method = XposedUtils.findMethod(clazz,
                    "onLongPress")
            XposedBridge.hookMethod(method, replacement)
        }
    }

    /**
     * 6.59.0
     */
    private fun replace2() {
        val classeNames = arrayOf(
                "tv.danmaku.biliplayerimpl.gesture.GestureService" +
                        "\$initInnerLongPressListener\$1\$onLongPress\$1",
                "tv.danmaku.biliplayerimpl.gesture.GestureService" +
                        "\$initInnerLongPressListener\$1\$onLongPressEnd\$1"
        )
        hookInvokeMethod(*classeNames)
    }
}