package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;

import com.droiuby.client.core.ActivityBuilder;

public class RelativeLayoutBuilder extends ViewGroupBuilder {

	public RelativeLayoutBuilder(ActivityBuilder builder, Context context) {
		super(builder, context);
		// TODO Auto-generated constructor stub
	}
	@Override
	public View getView() {
		return new RelativeLayout(context);
	}

	@Override
	protected View setParams(View child, Element e) {
		return super.setParams(child, e);
	}
	
}
