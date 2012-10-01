package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.droiuby.client.core.ActivityBuilder;

public class ViewGroupBuilder extends ViewBuilder {

	public ViewGroupBuilder(ActivityBuilder builder, Context context) {
		super(builder, context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public View getView() {
		// TODO Auto-generated method stub
		return super.getView();
	}

	@Override
	protected View setParams(View child, Element e) {
		// TODO Auto-generated method stub
		return super.setParams(child, e);
	}

	public boolean hasSubElements() {
		return true;
	}
}
