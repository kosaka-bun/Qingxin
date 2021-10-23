package de.honoka.android.xposed.qingxin.xposed.webview;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Message;
import android.view.KeyEvent;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SafeBrowsingResponse;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import java.util.Map;

import de.honoka.android.xposed.qingxin.xposed.webview.handler.BaseHandler;

@SuppressLint("NewApi")
public class HookedWebViewClient extends WebViewClient {

	private final WebViewClient originalWebClient;

	/**
	 * 该Client关联的WebView上绑定的JsInterface
	 */
	private final Map<Class<? extends BaseHandler>, BaseHandler>
			javascriptInterfaces;

	public HookedWebViewClient(WebViewClient originalWebClient,
			Map<Class<? extends BaseHandler>, BaseHandler> jsInterfaces) {
		this.originalWebClient = originalWebClient;
		this.javascriptInterfaces = jsInterfaces;
	}

	/**
	 * 关联的WebView在页面加载完成后要执行的操作
	 */
	@Override
	public void onPageFinished(WebView view, String url) {
		originalWebClient.onPageFinished(view, url);
		//判断WebView加载的网页，选择合适的处理器
		Class<? extends BaseHandler> handlerClass =
				HandlerSwitcher.apply(url);
		//获得该WebView上绑定的对应的处理器
		BaseHandler handler = javascriptInterfaces.get(handlerClass);
		//执行
		if(handler != null) handler.handle();
	}

	//region 无关方法

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		return originalWebClient.shouldOverrideUrlLoading(view, url);
	}

	@Override
	public boolean shouldOverrideUrlLoading(
			WebView view, WebResourceRequest request) {
		return originalWebClient.shouldOverrideUrlLoading(view, request);
	}

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		originalWebClient.onPageStarted(view, url, favicon);
	}

	@Override
	public void onLoadResource(WebView view, String url) {
		originalWebClient.onLoadResource(view, url);
	}

	@Override
	public void onPageCommitVisible(WebView view, String url) {
		originalWebClient.onPageCommitVisible(view, url);
	}

	@Nullable
	@Override
	public WebResourceResponse shouldInterceptRequest(
			WebView view, String url) {
		return originalWebClient.shouldInterceptRequest(view, url);
	}

	@Nullable
	@Override
	public WebResourceResponse shouldInterceptRequest(
			WebView view, WebResourceRequest request) {
		return originalWebClient.shouldInterceptRequest(view, request);
	}

	@Override
	public void onTooManyRedirects(WebView view, Message cancelMsg,
	                               Message continueMsg) {
		originalWebClient.onTooManyRedirects(view, cancelMsg,
				continueMsg);
	}

	@Override
	public void onReceivedError(WebView view, int errorCode,
	                            String description,
	                            String failingUrl) {
		originalWebClient.onReceivedError(view, errorCode,
				description, failingUrl);
	}

	@Override
	public void onReceivedError(WebView view, WebResourceRequest request,
	                            WebResourceError error) {
		originalWebClient.onReceivedError(view, request, error);
	}

	@Override
	public void onReceivedHttpError(WebView view, WebResourceRequest request,
	                                WebResourceResponse errorResponse) {
		originalWebClient.onReceivedHttpError(view, request, errorResponse);
	}

	@Override
	public void onFormResubmission(WebView view, Message dontResend,
	                               Message resend) {
		originalWebClient.onFormResubmission(view, dontResend, resend);
	}

	@Override
	public void doUpdateVisitedHistory(WebView view, String url,
	                                   boolean isReload) {
		originalWebClient.doUpdateVisitedHistory(view, url, isReload);
	}

	@Override
	public void onReceivedSslError(WebView view, SslErrorHandler handler,
	                               SslError error) {
		originalWebClient.onReceivedSslError(view, handler, error);
	}

	@Override
	public void onReceivedClientCertRequest(WebView view,
	                                        ClientCertRequest request) {
		originalWebClient.onReceivedClientCertRequest(view, request);
	}

	@Override
	public void onReceivedHttpAuthRequest(WebView view,
	                                      HttpAuthHandler handler,
	                                      String host, String realm) {
		originalWebClient.onReceivedHttpAuthRequest(
				view, handler, host, realm);
	}

	@Override
	public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
		return originalWebClient.shouldOverrideKeyEvent(view, event);
	}

	@Override
	public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
		originalWebClient.onUnhandledKeyEvent(view, event);
	}

	@Override
	public void onScaleChanged(WebView view, float oldScale,
	                           float newScale) {
		originalWebClient.onScaleChanged(view, oldScale, newScale);
	}

	@Override
	public void onReceivedLoginRequest(WebView view, String realm,
			@Nullable String account, String args) {
		originalWebClient.onReceivedLoginRequest(view, realm,
				account, args);
	}

	@Override
	public boolean onRenderProcessGone(
			WebView view, RenderProcessGoneDetail detail) {
		return originalWebClient.onRenderProcessGone(view, detail);
	}

	@Override
	public void onSafeBrowsingHit(WebView view, WebResourceRequest request,
	        int threatType, SafeBrowsingResponse callback) {
		originalWebClient.onSafeBrowsingHit(view, request,
				threatType, callback);
	}

	//endregion
}
