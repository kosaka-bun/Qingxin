package de.honoka.android.xposed.qingxin.xposed.webview.handler

import android.webkit.WebView
import de.honoka.android.xposed.qingxin.util.WebViewUtils.executeJs
import de.honoka.android.xposed.qingxin.util.WebViewUtils.initJquery

/**
 * 专栏页面处理器
 */
class ColumnHandler(
        private val webView: WebView
) : BaseHandler(webView) {

    override fun handle() {
        //添加JQuery
        webView.initJquery()
        //注册监听器
        registerListener()
    }

    /**
     * 注册监听器
     */
    private fun registerListener() {
        webView.executeJs("""
            $('div.read-reply').bind('DOMNodeInserted', function(e) {
                //console.log(e);
                let elem = e.originalEvent.path[0];
                let content = elem.innerText;
                if(content == undefined) return;
                console.log(content);
                if(content.indexOf('JVM') != -1) {
                	console.log(elem);
                	$(elem).remove();
                }
            });
        """, 1000)
    }
}