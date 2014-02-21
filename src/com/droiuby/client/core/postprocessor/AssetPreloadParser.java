package com.droiuby.client.core.postprocessor;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.droiuby.client.core.AssetDownloadCompleteListener;
import com.droiuby.client.core.ExecutionBundle;
import com.droiuby.client.core.builder.ActivityBuilder;

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
			this.builder.getPreloadedResource().put(id, (Drawable) result);
		} else if (type.equals("font") || type.equals("typeface")) {
			Log.d(this.getClass().toString(),"Seeting to typeface " + (String)result);
			Typeface myTypeface = Typeface.createFromFile((String)result);
			this.builder.getPreloadedResource().put(id, myTypeface);
		} else if (type.equals("binary")) {
			this.builder.getPreloadedResource().put(id, (String)result);
		}
		return null;
	}

}
