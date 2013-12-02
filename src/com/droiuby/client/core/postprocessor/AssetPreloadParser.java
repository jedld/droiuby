package com.droiuby.client.core.postprocessor;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.droiuby.client.core.ActivityBuilder;
import com.droiuby.client.core.AssetDownloadCompleteListener;
import com.droiuby.client.core.ExecutionBundle;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class AssetPreloadParser implements AssetDownloadCompleteListener {

	String id;
	String type;
	ActivityBuilder builder;
	
	public AssetPreloadParser(String id, String type, ActivityBuilder builder) {
		this.id = id;
		this.type = type;
		this.builder = builder;
	}
	
	public Object onComplete(ExecutionBundle bundle, String name, Object result) {
		if (type.equals("image")) {
			this.builder.getPreloadedResource().put(id, (Drawable)result);
		} 
		return null;
	}

}
