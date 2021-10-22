package de.honoka.android.xposed.qingxin.util

import android.webkit.WebView

object WebViewUtils {

    /**
     * 执行一段js，可指定延迟毫秒数
     */
    @JvmStatic
    fun WebView.executeJs(js: String, delay: Long? = null) {
        //转为单行，去掉注释
        val lines = js.trimIndent().trim().lines()
        val jsBuilder = StringBuilder()
        for(line in lines) {
            val trimLine = line.trim()
            if(!trimLine.startsWith("//"))
                jsBuilder.append(trimLine).append(" ")
        }
        //执行
        if(delay == null) {
            loadUrl("javascript:(function(){$jsBuilder})();")
        } else {
            loadUrl("javascript:setTimeout(function(){${
                jsBuilder}}, ${delay});")
        }
    }

    private object Scripts {

        const val INIT_JQUERY: String = """
            let script = document.createElement('script');
            script.setAttribute('src', 'https://apps.bdimg.com' + 
                    '/libs/jquery/2.1.4/jquery.min.js');
            document.body.appendChild(script);
        """
    }

    @JvmStatic
    fun WebView.initJquery() {
        executeJs(Scripts.INIT_JQUERY)
    }
}