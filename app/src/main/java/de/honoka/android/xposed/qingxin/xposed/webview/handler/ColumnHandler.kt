package de.honoka.android.xposed.qingxin.xposed.webview.handler

import android.webkit.JavascriptInterface
import android.webkit.WebView
import de.honoka.android.xposed.qingxin.provider.QingxinProvider
import de.honoka.android.xposed.qingxin.util.Logger
import de.honoka.android.xposed.qingxin.util.TextUtils.singleLine
import de.honoka.android.xposed.qingxin.util.WebViewUtils.executeJs
import de.honoka.android.xposed.qingxin.util.WebViewUtils.initJquery
import de.honoka.android.xposed.qingxin.xposed.XposedMain
import de.honoka.android.xposed.qingxin.xposed.hook.CommentHook

/**
 * 专栏页面处理器
 */
class ColumnHandler(
		private val webView: WebView
) : BaseHandler(webView) {

	/**
	 * 拦截报告器，可被多次调用，然后在一段时间后报告拦截条目数
	 */
	inner class BlockReporter {

		/**
		 * 上次请求报告的时间
		 */
		@Volatile
		var lastTimeRequestTime: Long? = null

		/**
		 * 被请求次数
		 */
		@Volatile
		var requestCount: Int? = null

		/**
		 * 在一段时间后进行报告的线程
		 */
		var lateReportThread: Thread? = null

		fun request() {
			//收集信息
			lastTimeRequestTime = System.currentTimeMillis()
			requestCount = if(requestCount == null) 1
					else requestCount!! + 1
			//判断或建立线程
			if(lateReportThread == null) {
				//这种lambda语法是真的难理解
				lateReportThread = Thread {
					//最多等待20秒
					for(i in 1..100) {
						//判断当前时间是否已距上次报告超过2秒
						if(System.currentTimeMillis() -
								lastTimeRequestTime!! > 2000) {
							//用Toast报告拦截数，然后退出监听
							Logger.toastOnBlock("拦截了" +
									requestCount + "条评论")
							blockReporter = null
							return@Thread
						}
						//在2秒内就不进行任何操作，等待下一次循环
						Thread.sleep(200)
					}
				}
				lateReportThread!!.start()
			}
		}
	}

	var blockReporter: BlockReporter? = null

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
		val bundle = XposedMain.contentResolver.call(
				QingxinProvider.QINGXIN_PROVIDER_URI,
				QingxinProvider.RequestMethod.ASSETS,
				"webview/handler/ColumnHandler/registerListener.js",
				null)
		val js: String? = bundle?.getString("data")
		if(js != null) webView.executeJs(js)
	}

	@JavascriptInterface
	fun isBlockComment(content: String): String {
		return "${XposedMain.blockRuleCache
				.isBlockCommentMessage(content)}"
	}

	@JavascriptInterface
	fun isBlockUsername(username: String): String {
		return "${XposedMain.blockRuleCache
				.isBlockUsername(username)}"
	}

	@JavascriptInterface
	fun reportBlock(type: String, username: String, content: String) {
		//报告
		if(blockReporter == null)
			blockReporter = BlockReporter()
		blockReporter!!.request()
		//输出日志
		when(type) {
			"content" -> {
				Logger.blockLog("评论拦截：${content.singleLine(
						CommentHook.BLOCK_LOG_LENGTH_LIMIT)}")
			}
			"username" -> {
				Logger.blockLog("评论拦截（按用户名）：${content
						.singleLine(CommentHook.BLOCK_LOG_LENGTH_LIMIT)
				}\n用户名：${username}")
			}
		}
	}
}