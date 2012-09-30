package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.droiuby.client.core.ActivityBuilder;

public class LinearLayoutBuilder extends ViewGroupBuilder {
	
	public LinearLayoutBuilder(ActivityBuilder builder, Context context) {
		super(builder, context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public View getView() {
		return new LinearLayout(context);
	}

	@Override
	protected View setParams(View child, Element e) {
		int orientation = LinearLayout.VERTICAL;
		LinearLayout layout = (LinearLayout)child;
		if ((e.getAttributeValue("orientation") != null)
				&& e.getAttributeValue("orientation")
						.equalsIgnoreCase("horizontal")) {
			orientation = LinearLayout.HORIZONTAL;
		}
		layout.setOrientation(orientation);
		return super.setParams(child, e);
	}
	
	
}
