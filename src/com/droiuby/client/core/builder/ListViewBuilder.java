package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.content.Context;
import android.view.View;
import android.widget.ListView;

import com.droiuby.client.core.ActivityBuilder;

public class ListViewBuilder extends ViewGroupBuilder {

	static ListViewBuilder instance;
	
	protected ListViewBuilder(ActivityBuilder builder, Context context) {
		super(builder, context);
		// TODO Auto-generated constructor stub
	}

	public static ViewBuilder getInstance(ActivityBuilder builder,
			Context context) {
		if (instance == null) {
			instance = new ListViewBuilder(builder, context);
		}
		return instance;
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
