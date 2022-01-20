package de.honoka.android.xposed.qingxin.xposed.hook;

import java.util.Arrays;
import java.util.List;

import de.honoka.android.xposed.qingxin.util.AndroidUtils;
import de.honoka.android.xposed.qingxin.xposed.XposedMain;
import lombok.SneakyThrows;

/**
 * 使弹幕使用java层加载而不是native，与chronos有关
 */
public class ChronosHook extends LateInitHook {

    private static final List<String> BLOCK_KEYS = Arrays.asList(
            "chronos_enable_dfm_v3",
            "enable_chronos",
            "enable_chronos_before_lollipop",
            "enable_x86_chronos"
    );

    public static Integer bilibiliAppVersionCode;

    @SneakyThrows
    @Override
    protected void afterHookedMethod(MethodHookParam param) {
        String key = (String) param.args[0];
        if(BLOCK_KEYS.contains(key)) {
            if(bilibiliAppVersionCode == null) {
                bilibiliAppVersionCode = AndroidUtils.getVersionCode(
                        XposedMain.hookApplication);
            }
            if(bilibiliAppVersionCode <= 6500300) {
                param.setResult(false);
            }
        }
    }
}
