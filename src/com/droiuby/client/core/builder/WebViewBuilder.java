package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.content.Context;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.droiuby.client.core.ActivityBuilder;

public class WebViewBuilder extends AbsoluteLayoutBuilder {

	static WebViewBuilder instance;
	
	protected WebViewBuilder(ActivityBuilder builder, Context context) {
		super(builder, context);
		// TODO Auto-generated constructor stub
	}
	
	public static WebViewBuilder getInstance(ActivityBuilder builder,
			Context context) {
		if (instance == null) {
			instance = new WebViewBuilder(builder, context);
		}
		return instance;
	}

	@Override
	public View getView() {
		// TODO Auto-generated method stub
		return new WebView(context);
	}

	@Override
	protected View setParams(View child, Element e) {
		WebView webview = (WebView)child;
		String url = e.getAttributeValue("src");
		if (url != null) {
			webview.loadUrl(url);
		}
		webview.loadUrl(url);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view,
					String url) {
				view.loadUrl(url);
				return false;

			}
		});
		return super.setParams(child, e);
	}
	
	
}
