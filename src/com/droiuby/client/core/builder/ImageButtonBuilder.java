package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;

import com.droiuby.client.core.ActivityBuilder;

public class ImageButtonBuilder extends ImageViewBuilder {

	
	public ImageButtonBuilder(ActivityBuilder builder, Context context) {
		super(builder, context);
	}

	@Override
	public View getView() {
		return new ImageButton(context);
	}

	@Override
	public View setParams(View child, Element e) {
		builder.handleIconDrawable(e, (ImageButton)child);
		return super.setParams(child, e);
	}
	
	
}
