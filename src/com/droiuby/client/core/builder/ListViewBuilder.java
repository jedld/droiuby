package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.content.Context;
import android.view.View;
import android.widget.ListView;


public class ListViewBuilder extends ViewGroupBuilder {

	@Override
	public View getView() {
		return new ListView(context);
	}

	@Override
	public View setParams(View child, Element e) {
		return super.setParams(child, e);
	}

}
