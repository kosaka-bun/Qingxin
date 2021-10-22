package de.honoka.android.xposed.qingxin.xposed.webview.handler

import android.webkit.WebView

abstract class BaseHandler(
		private val webView: WebView
) {

	abstract fun handle()
}