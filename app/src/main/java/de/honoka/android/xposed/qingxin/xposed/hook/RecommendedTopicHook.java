package de.honoka.android.xposed.qingxin.xposed.hook;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import de.honoka.android.xposed.qingxin.xposed.XposedMain;
import de.robv.android.xposed.XposedHelpers;

/**
 * 屏蔽所有推荐话题（动态页）
 */
public class RecommendedTopicHook extends LateInitHook {

    private final List<String> blockTitles = Arrays.asList(
            "推荐话题", "热门话题"
    );

    @Override
    public void after(MethodHookParam param) {
        if(!Objects.equals(XposedMain.mainPreference
                .getBlockRecommendedTopics(), true))
            return;
        String title = (String) XposedHelpers.callMethod(param
                .getResult(), "getTitle");
        if(blockTitles.contains(title)) {
            param.setResult(XposedHelpers.callStaticMethod(
                    param.getResult().getClass(),
                    "getDefaultInstance"));
        }
    }
}
