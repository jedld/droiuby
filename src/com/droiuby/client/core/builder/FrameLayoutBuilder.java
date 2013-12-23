package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;


public class FrameLayoutBuilder extends ViewGroupBuilder {

	@Override
	public View getView() {
		return new FrameLayout(context);
	}

	@Override
	public View setParams(View child, Element e) {
		FrameLayout layout = (FrameLayout)child;
		if ((e.getAttributeValue("foreground_gravity") != null)) {
			layout.setForegroundGravity(builder.parseGravity(e.getAttributeValue("foreground_gravity")));
		}
		return super.setParams(child, e);
	}

}
