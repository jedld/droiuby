package com.droiuby.client.core.builder;

import android.view.View;
import android.widget.EditText;


public class EditTextBuilder extends TextViewBuilder {

	static EditTextBuilder instance;

	@Override
	public View getView() {
		return new EditText(context);
	}

	@Override
	protected void mapAttribute(View child, String attribute_name,
			String attribute_value) {
		// TODO Auto-generated method stub
		super.mapAttribute(child, attribute_name, attribute_value);
		EditText editText = (EditText) child;
		if (attribute_name.equals("hint")
				|| attribute_name.equals("placeholder")) {
			editText.setHint(attribute_value);
		} else if (attribute_name.equals("value")) {
			editText.setText(attribute_value);
		}
	}

}
