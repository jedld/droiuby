package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.droiuby.client.R;
import com.droiuby.client.core.ActivityBuilder;


public class TextViewBuilder extends ViewBuilder {

	public TextViewBuilder(ActivityBuilder builder, Context context) {
		super(builder, context);
	}

	@Override
	public View getView() {
		return new TextView(context);
	}

	@Override
	protected View setParams(View child, Element e) {
		super.setParams(child, e);
		
		TextView textView = (TextView)child;
		
		String fontSize = e.getAttributeValue("size");
		if (fontSize != null) {
			textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
					Float.parseFloat(fontSize));
		}

		String gravityStr = e.getAttributeValue("gravity");
		if (gravityStr != null) {
			int gravity = this.builder.parseGravity(gravityStr);
			textView.setGravity(gravity);
		}

		String color = e.getAttributeValue("color");
		if (color != null) {
			textView.setTextColor(Color.parseColor(color));
		}

		String style = e.getAttributeValue("style");
		if (style != null) {
			if (style.equalsIgnoreCase("bold")) {
				textView.setTextAppearance(context, R.style.boldText);
			} else if (style.equalsIgnoreCase("italic")) {
				textView.setTextAppearance(context, R.style.italicText);
			} else if (style.equalsIgnoreCase("normal")) {
				textView.setTextAppearance(context, R.style.normalText);
			}
		}

		String content = e.getTextTrim() != null ? e.getTextTrim() : "";
		textView.setText(content);
		return child;
	}
	
	
}
