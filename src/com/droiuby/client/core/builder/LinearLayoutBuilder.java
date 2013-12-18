package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.droiuby.client.core.ActivityBuilder;

public class LinearLayoutBuilder extends ViewGroupBuilder {

	@Override
	public View getView() {
		LinearLayout layout =  new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		return layout;
	}
	
	@Override
	protected void mapAttribute(View child, String attribute_name,
			String attribute_value) {
		// TODO Auto-generated method stub
		super.mapAttribute(child, attribute_name, attribute_value);
		LinearLayout layout = (LinearLayout)child;
		if (attribute_name.equals("orientation")) {
			layout.setOrientation(LinearLayout.VERTICAL);
			if (attribute_value.equals("horizontal")) {
				layout.setOrientation(LinearLayout.HORIZONTAL);
			}
			
		} else if (attribute_name.equals("gravity")) {
			layout.setGravity(parseGravity(attribute_value));
		}
	}
	
}
