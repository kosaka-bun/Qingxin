package de.honoka.android.xposed.qingxin.xposed.webview.handler

import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import de.honoka.android.xposed.qingxin.provider.QingxinProvider
import de.honoka.android.xposed.qingxin.util.WebViewUtils.executeJs
import de.honoka.android.xposed.qingxin.util.WebViewUtils.initJquery
import de.honoka.android.xposed.qingxin.xposed.XposedMain

/**
 * 专栏页面处理器
 */
class ColumnHandler(
		private val webView: WebView
) : BaseHandler(webView) {

	class ColumnJsInterface {

		@JavascriptInterface
		fun nativeWait(millis: Long) {
			Thread.sleep(millis)
		}

		@JavascriptInterface
		fun log(str: String) {
			Log.i("jsLog", str);
		}

		@JavascriptInterface
		fun isBlockComment(content: String): String {
			return "${XposedMain.blockRuleCache
					.isBlockCommentMessage(content)}"
		}
	}

	override fun handle() {
		webView.addJavascriptInterface(ColumnJsInterface(),
				"columnJsInterface")
		//添加JQuery
		webView.initJquery()
		//注册监听器
		registerListener()
	}

	/**
	 * 注册监听器
	 */
	private fun registerListener() {
		val bundle = XposedMain.contentResolver.call(
				QingxinProvider.QINGXIN_PROVIDER_URI,
				QingxinProvider.RequestMethod.ASSETS,
				"webview/handler/ColumnHandler/registerListener.js",
				null)
		val js: String? = bundle?.getString("data")
		if(js != null) webView.executeJs(js)
	}
}