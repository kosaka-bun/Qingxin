package de.honoka.android.xposed.qingxin.xposed.webview

import de.honoka.android.xposed.qingxin.xposed.webview.handler.BaseHandler
import de.honoka.android.xposed.qingxin.xposed.webview.handler.ColumnHandler

/**
 * 转发器，根据URL来判断采用哪个处理器对页面进行处理
 */
object HandlerSwitcher {

	@JvmStatic
	fun apply(url: String): Class<out BaseHandler>? {
		when {
			url.contains("www.bilibili.com/read/native") -> {
				return ColumnHandler::class.java
			}
		}
		return null
	}
}