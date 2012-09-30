package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.content.Context;
import android.view.View;
import android.widget.ListView;

import com.droiuby.client.core.ActivityBuilder;

public class ListViewBuilder extends ViewGroupBuilder {

	public ListViewBuilder(ActivityBuilder builder, Context context) {
		super(builder, context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public View getView() {
		return new ListView(context);
	}

	@Override
	protected View setParams(View child, Element e) {
		return super.setParams(child, e);
	}

}
