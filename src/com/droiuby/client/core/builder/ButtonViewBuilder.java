package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.view.View;
import android.widget.Button;


public class ButtonViewBuilder extends TextViewBuilder {

	static ButtonViewBuilder instance;

	@Override
	public View getView() {
		return new Button(context);
	}

	@Override
	public View setParams(View child, Element e) {
		return super.setParams(child, e);
	}

}
