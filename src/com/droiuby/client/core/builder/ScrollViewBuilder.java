package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.view.View;
import android.widget.ScrollView;


public class ScrollViewBuilder extends ViewGroupBuilder {

	@Override
	public View getView() {
		return new ScrollView(context);
	}

	@Override
	public View setParams(View child, Element e) {
		return super.setParams(child, e);
	}
	

}
