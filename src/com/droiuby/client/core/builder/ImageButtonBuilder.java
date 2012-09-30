package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;

import com.droiuby.client.core.ActivityBuilder;

public class ImageButtonBuilder extends ImageViewBuilder {

	static ImageButtonBuilder instance;
	
	protected ImageButtonBuilder(ActivityBuilder builder, Context context) {
		super(builder, context);
	}

	public static ImageButtonBuilder getInstance(ActivityBuilder builder,
			Context context) {
		if (instance == null) {
			instance = new ImageButtonBuilder(builder, context);
		}
		return instance;
	}

	@Override
	public View getView() {
		return new ImageButton(context);
	}

	@Override
	protected View setParams(View child, Element e) {
		builder.handleIconDrawable(e, (ImageButton)child);
		return super.setParams(child, e);
	}
	
	
}
