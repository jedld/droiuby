package com.droiuby.client.core.postprocessor;

import android.graphics.drawable.Drawable;

import com.droiuby.client.core.ActivityBuilder;
import com.droiuby.client.core.AssetDownloadCompleteListener;
import com.droiuby.client.core.ExecutionBundle;

public class GenericPostProcessor implements AssetDownloadCompleteListener {

	String id;
	String type;
	ActivityBuilder builder;
	
	public GenericPostProcessor(String id, String type, ActivityBuilder builder) {
		this.id = id;
		this.type = type;
		this.builder = builder;
	}
	
	public Object onComplete(ExecutionBundle bundle, String name, Object result) {
		this.builder.getPreloadedResource().put(id, (String)result);
		return result;
	}

}
