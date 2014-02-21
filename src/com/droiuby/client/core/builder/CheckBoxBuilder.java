package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.view.View;
import android.widget.CheckBox;


public class CheckBoxBuilder extends ButtonViewBuilder {

	static CheckBoxBuilder instance;
	
	@Override
	public View getView() {
		// TODO Auto-generated method stub
		return new CheckBox(context);
	}

	@Override
	public View setParams(View child, Element e) {
		// TODO Auto-generated method stub
		return super.setParams(child, e);
	}

}
