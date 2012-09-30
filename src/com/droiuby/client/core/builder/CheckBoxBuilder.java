package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;

import com.droiuby.client.core.ActivityBuilder;

public class CheckBoxBuilder extends ButtonViewBuilder {

	static CheckBoxBuilder instance;
	
	protected CheckBoxBuilder(ActivityBuilder builder, Context context) {
		super(builder, context);
		// TODO Auto-generated constructor stub
	}
	
	public static CheckBoxBuilder getInstance(ActivityBuilder builder,
			Context context) {
		if (instance == null) {
			instance = new CheckBoxBuilder(builder, context);
		}
		return instance;
	}

	@Override
	public View getView() {
		// TODO Auto-generated method stub
		return new CheckBox(context);
	}

	@Override
	protected View setParams(View child, Element e) {
		// TODO Auto-generated method stub
		return super.setParams(child, e);
	}

}