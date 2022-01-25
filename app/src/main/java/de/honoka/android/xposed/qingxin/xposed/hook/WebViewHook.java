package de.honoka.android.xposed.qingxin.xposed.hook;

import android.annotation.SuppressLint;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.honoka.android.xposed.qingxin.util.CodeUtils;
import de.honoka.android.xposed.qingxin.util.Logger;
import de.honoka.android.xposed.qingxin.xposed.webview.HookedWebViewClient;
import de.honoka.android.xposed.qingxin.xposed.webview.handler.BaseHandler;
import de.honoka.android.xposed.qingxin.xposed.webview.handler.ColumnHandler;
import lombok.SneakyThrows;

@SuppressLint("JavascriptInterface")
public class WebViewHook extends LateInitHook {

    /**
     * 要加载的Javascript接口类型
     */
    private final List<Class<? extends BaseHandler>>
            javascriptInterfaceClasses = Arrays.asList(
            ColumnHandler.class
    );

    @SneakyThrows
    @Override
    public void before(MethodHookParam param) {
        WebView.setWebContentsDebuggingEnabled(true);
        //Logger.testLog("WebView调试已开启");
        //Hook WebViewClient
        WebView webView = (WebView) param.thisObject;
        WebViewClient webViewClient = (WebViewClient) param.args[0];
        //为webView添加js接口
        Map<Class<? extends BaseHandler>, BaseHandler>
                javascriptInterfaces = new HashMap<>();
        for(Class<? extends BaseHandler> clazz :
                javascriptInterfaceClasses) {
            BaseHandler handler = clazz.getConstructor(WebView.class)
                    .newInstance(webView);
            javascriptInterfaces.put(clazz, handler);
            webView.addJavascriptInterface(handler,
                    CodeUtils.getCamelCaseName(clazz));
        }
        //代理即将添加的webViewClient
        param.args[0] = new HookedWebViewClient(
                webViewClient, javascriptInterfaces);
    }
}
