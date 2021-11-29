package de.honoka.android.xposed.qingxin.xposed.hook;

import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import lombok.SneakyThrows;

/**
 * 使弹幕使用java层加载而不是native，与chronos有关
 */
public class ChronosHook extends XC_MethodHook {

    private static final List<String> BLOCK_KEYS = Arrays.asList(
            "chronos_enable_dfm_v3",
            "enable_chronos",
            "enable_chronos_before_lollipop",
            "enable_x86_chronos"
    );

    @SneakyThrows
    @Override
    protected void afterHookedMethod(MethodHookParam param) {
        String key = (String) param.args[0];
        if(BLOCK_KEYS.contains(key)) {
            param.setResult(false);
        }
    }
}
