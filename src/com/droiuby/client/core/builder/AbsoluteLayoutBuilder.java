package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.content.Context;
import android.view.View;
import android.widget.AbsoluteLayout;


@SuppressWarnings("deprecation")
public class AbsoluteLayoutBuilder extends ViewGroupBuilder {

	@SuppressWarnings("deprecation")
	@Override
	public View getView() {
		// TODO Auto-generated method stub
		return new AbsoluteLayout(context);
	}

	@Override
	public View setParams(View child, Element e) {
		// TODO Auto-generated method stub
		return super.setParams(child, e);
	}

}
