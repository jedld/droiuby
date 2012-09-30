package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import com.droiuby.client.core.ActivityBuilder;

public class FrameLayoutBuilder extends ViewGroupBuilder {

	
	public FrameLayoutBuilder(ActivityBuilder builder, Context context) {
		super(builder, context);
	}

	@Override
	public View getView() {
		return new FrameLayout(context);
	}

	@Override
	protected View setParams(View child, Element e) {
		FrameLayout layout = (FrameLayout)child;
		if ((e.getAttributeValue("foreground_gravity") != null)) {
			layout.setForegroundGravity(builder.parseGravity(e.getAttributeValue("foreground_gravity")));
		}
		return super.setParams(child, e);
	}

}
