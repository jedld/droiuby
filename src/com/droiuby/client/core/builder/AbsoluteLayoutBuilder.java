package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.content.Context;
import android.view.View;
import android.widget.AbsoluteLayout;

import com.droiuby.client.core.ActivityBuilder;

public class AbsoluteLayoutBuilder extends ViewGroupBuilder {

	protected AbsoluteLayoutBuilder(ActivityBuilder builder, Context context) {
		super(builder, context);
	}

	@SuppressWarnings("deprecation")
	@Override
	public View getView() {
		// TODO Auto-generated method stub
		return new AbsoluteLayout(context);
	}

	@Override
	protected View setParams(View child, Element e) {
		// TODO Auto-generated method stub
		return super.setParams(child, e);
	}

}