package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.content.Context;
import android.graphics.Color;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.droiuby.client.core.ActivityBuilder;

public class TextViewBuilder extends ViewBuilder {

	@Override
	public View getView() {
		return new TextView(context);
	}

	@Override
	protected void mapAttribute(View child, String attribute_name,
			String attribute_value) {
		// TODO Auto-generated method stub
		super.mapAttribute(child, attribute_name, attribute_value);
		TextView textView = (TextView) child;
		if (attribute_name.equals("size")) {
			textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
					toSize(attribute_value));
		} else if (attribute_name.equals("gravity")
				|| attribute_name.equals("text-align")) {
			int gravity = this.builder.parseGravity(attribute_value);
			textView.setGravity(gravity);
		} else if (attribute_name.equals("color")) {
			textView.setTextColor(Color.parseColor(attribute_value));
		} else if (attribute_name.equals("style")
				|| attribute_name.equals("text-decoration")) {
			if (attribute_value.equalsIgnoreCase("bold")) {
				textView.setTextAppearance(context, builder.getStyleById("boldText"));
			} else if (attribute_value.equalsIgnoreCase("italic")) {
				textView.setTextAppearance(context, builder.getStyleById("italicText"));
			} else if (attribute_value.equalsIgnoreCase("normal")) {
				textView.setTextAppearance(context, builder.getStyleById("normalText"));
			}
		} else if (attribute_name.equals("cursor_visible")
				|| attribute_name.equals("cursor")) {
			if (attribute_value.equals("true")) {
				textView.setCursorVisible(true);
			} else if (attribute_value.equals("false")
					|| attribute_value.equals("none")) {
				textView.setCursorVisible(false);
			}
		} else if (attribute_name.equals("scroll_horizontally")
				|| attribute_name.equals("horizontally_scrolling")) {
			if (attribute_value.equals("true")) {
				textView.setHorizontallyScrolling(true);
			} else if (attribute_value.equals("false")
					|| attribute_value.equals("none")) {
				textView.setCursorVisible(false);
			}
		}
	}

	@Override
	public View setParams(View child, Element e) {
		TextView textView = (TextView) child;
		String content = e.getTextTrim() != null ? e.getTextTrim() : "";
		textView.setText(content);

		super.setParams(child, e);

		String type = e.getAttributeValue("type");
		if (type != null) {
			if (type.equals("password")) {
				textView.setTransformationMethod(PasswordTransformationMethod
						.getInstance());
			}
		}

		String single_line = e.getAttributeValue("single_line");
		if (single_line != null && single_line.equals("true")) {
			textView.setSingleLine();
		}

		return child;
	}

}
