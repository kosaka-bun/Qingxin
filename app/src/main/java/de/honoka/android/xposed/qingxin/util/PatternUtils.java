package de.honoka.android.xposed.qingxin.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternUtils {

    /**
     * 判断字符串中是否存在匹配正则表达式的子串
     */
    public static boolean containsMatch(String str, String pattern) {
        Pattern patternObj = Pattern.compile(pattern);
        Matcher matcher = patternObj.matcher(str);
        return matcher.find();
    }
}
