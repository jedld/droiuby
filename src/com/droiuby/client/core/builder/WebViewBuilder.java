package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.annotation.SuppressLint;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class WebViewBuilder extends AbsoluteLayoutBuilder {

	@Override
	public View getView() {
		// TODO Auto-generated method stub
		return new WebView(context);
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public View setParams(View child, Element e) {
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
