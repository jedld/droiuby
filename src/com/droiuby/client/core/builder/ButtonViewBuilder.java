package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.droiuby.client.core.ActivityBuilder;

public class ButtonViewBuilder extends TextViewBuilder {

	static ButtonViewBuilder instance;
	
	public ButtonViewBuilder(ActivityBuilder builder, Context context) {
		super(builder, context);
	}
	
	@Override
	public View getView() {
		return new Button(context);
	}

	@Override
	public View setParams(View child, Element e) {
		return super.setParams(child, e);
	}

}
