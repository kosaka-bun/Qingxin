package de.honoka.android.xposed.qingxin.util

object TextUtils {

    @JvmStatic
    fun String.singleLine(limit: Int? = null): String {
        var singleLine = replace("\r", "")
                .replace("\n", " ")
        if(limit != null) {
            if(singleLine.length > limit) {
                singleLine = singleLine.substring(0, limit) + "..."
            }
        }
        return singleLine
    }
}