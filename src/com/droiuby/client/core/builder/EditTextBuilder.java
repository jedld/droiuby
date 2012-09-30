package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.content.Context;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;

import com.droiuby.client.core.ActivityBuilder;

public class EditTextBuilder extends TextViewBuilder {

	static EditTextBuilder instance;
	
	protected EditTextBuilder(ActivityBuilder builder, Context context) {
		super(builder, context);
	}

	@Override
	public View getView() {
		return new EditText(context);
	}
	
	public static ViewBuilder getInstance(ActivityBuilder builder,
			Context context) {
		if (instance == null) {
			instance = new EditTextBuilder(builder, context);
		}
		return instance;
	}

	@Override
	protected View setParams(View child, Element e) {
		EditText editText = (EditText)child;
		String hint = e.getAttributeValue("hint");
		if (hint != null) {
			editText.setHint(hint);
		}

		String color = e.getAttributeValue("color");
		if (color != null) {
			if (color.startsWith("#")) {
				color = color.substring(1);
			}
			int val = Integer.parseInt(color, 16);
			editText.setTextColor(val);
		}

		String type = e.getAttributeValue("type");
		if (type != null) {
			if (type.equals("password")) {
				editText.setTransformationMethod(PasswordTransformationMethod
						.getInstance());
			}
		}

		String value = e.getAttributeValue("value");
		if (value != null) {
			editText.setText(value);
		}
		return super.setParams(child, e);
	}

}
