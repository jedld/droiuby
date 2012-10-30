package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.droiuby.client.core.ActivityBuilder;

public class ScrollViewBuilder extends ViewGroupBuilder {

	public ScrollViewBuilder(ActivityBuilder builder, Context context) {
		super(builder, context);
	}
	
	@Override
	public View getView() {
		return new ScrollView(context);
	}

	@Override
	public View setParams(View child, Element e) {
		return super.setParams(child, e);
	}
	

}
