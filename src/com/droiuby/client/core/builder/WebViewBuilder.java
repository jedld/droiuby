package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.droiuby.client.core.ActivityBuilder;

public class WebViewBuilder extends AbsoluteLayoutBuilder {

	public WebViewBuilder(ActivityBuilder builder, Context context) {
		super(builder, context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public View getView() {
		// TODO Auto-generated method stub
		return new WebView(context);
	}

	@SuppressLint("SetJavaScriptEnabled")
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
