package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.view.View;


public class ViewGroupBuilder extends ViewBuilder {

	@Override
	public View getView() {
		// TODO Auto-generated method stub
		return super.getView();
	}

	@Override
	public View setParams(View child, Element e) {
		// TODO Auto-generated method stub
		return super.setParams(child, e);
	}

	public boolean hasSubElements() {
		return true;
	}
}
