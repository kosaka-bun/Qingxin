package de.honoka.android.xposed.qingxin.util

import android.webkit.WebView
import de.honoka.android.xposed.qingxin.util.TextUtils.singleLine

object WebViewUtils {

    @JvmStatic
    fun WebView.executeJs(js: String) {
        loadUrl("javascript:(function(){${js.trimIndent()
                .trim().singleLine()}})();")
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