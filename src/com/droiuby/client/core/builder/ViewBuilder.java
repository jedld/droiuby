package com.droiuby.client.core.builder;

import org.jdom2.Attribute;
import org.jdom2.Element;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;

import com.droiuby.client.core.ActivityBuilder;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class ViewBuilder {

	Context context;
	ActivityBuilder builder;

	public ViewBuilder(ActivityBuilder builder, Context context) {
		this.context = context;
		this.builder = builder;
	}

	public View getView() {
		return new View(context);
	}

	protected View setParams(View child, Element e) {

		String background_color = e.getAttributeValue("background_color");
		if (background_color != null) {
			child.setBackgroundColor(Color.parseColor(background_color));
		}

		if (e.getAttributeValue("rotation") != null) {
			float rotation = Float.parseFloat(e.getAttributeValue("rotation"));
			child.setRotation(rotation);
		}

		if (e.getAttributeValue("rotation_x") != null) {
			float rotation_x = Float.parseFloat(e
					.getAttributeValue("rotation_x"));
			child.setRotationX(rotation_x);
		}

		if (e.getAttributeValue("rotation_y") != null) {
			float rotation_y = Float.parseFloat(e
					.getAttributeValue("rotation_y"));
			child.setRotationY(rotation_y);
		}

		if (e.getAttributeValue("pivot_x") != null) {
			float pivot_x = Float.parseFloat(e.getAttributeValue("pivot_x"));
			child.setPivotX(pivot_x);
		}

		if (e.getAttributeValue("pivot_y") != null) {
			float pivot_y = Float.parseFloat(e.getAttributeValue("pivot_y"));
			child.setPivotY(pivot_y);
		}

		if (e.getAttribute("camera_distance") != null) {
			float camera_distance = Float.parseFloat(e
					.getAttributeValue("camera_distance"));
			child.setCameraDistance(camera_distance);
		}

		for (Attribute attribute : e.getAttributes()) {

			String attribute_name = attribute.getName();
			String attribute_value = attribute.getValue();

			if (attribute_name.equals("x")) {
				child.setX(Float.parseFloat(attribute_value));
			} else if (attribute_name.equals("y")) {
				child.setY(Float.parseFloat(attribute_value));
			} else if (attribute_name.equals("bottom")) {
				child.setBottom(toPixels(attribute_value));
			} else if (attribute_name.equals("min_height")) {
				child.setMinimumHeight(toPixels(attribute_value));
			} else if (attribute_name.equals("min_width")) {
				child.setMinimumWidth(toPixels(attribute_value));
			} else if (attribute_name.equals("background")) {
				if (attribute_value != null) {
					if (attribute_value.startsWith("@drawable:")) {
						String drawable = attribute_value.substring(10);
						int resId = builder.getDrawableId(drawable);
						if (resId != 0) {
							child.setBackgroundResource(resId);
						}
					} else if (attribute_value.startsWith("@preload:")) {
						Drawable drawable = (Drawable) builder
								.findViewByName(attribute_value);
						child.setBackgroundDrawable(drawable);
					} else {
						UrlImageViewHelper.setUrlDrawable(child,
								attribute_value, "setBackgroundDrawable");
					}
				}
			} else if (attribute_name.equals("enabled")) {
				child.setEnabled(attribute_value.equalsIgnoreCase("false") ? false
						: true);
			} else if (attribute_name.equals("visibility")) {
				if (attribute_value.equalsIgnoreCase("hidden")
						|| attribute_value.equalsIgnoreCase("gone")) {
					child.setVisibility(View.GONE);
				} else if (attribute_name.equals("invisible")) {
					child.setVisibility(View.INVISIBLE);
				} else if (attribute_name.equals("visible")) {
					child.setVisibility(View.VISIBLE);
				}
			}

		}

		setAlpha(child, e);

		return child;
	}

	public View build(Element element) {
		View view = getView();
		setParams(view, element);
		return view;
	}

	protected void setAlpha(View v, Element e) {
		float alpha = 1;
		if (e.getAttributeValue("alpha") != null) {
			alpha = Float.parseFloat(e.getAttributeValue("alpha"));
		}

		v.setAlpha(alpha);
	}

	protected int toPixels(String measurement) {
		int minWidth = 0;
		if (measurement.endsWith("dp") || measurement.endsWith("dip")) {
			int s = 2;
			if (measurement.endsWith("dip")) {
				s = 3;
			}
			Resources r = context.getResources();
			minWidth = Math.round(TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP,
					Float.parseFloat(measurement.substring(0,
							measurement.length() - s)), r.getDisplayMetrics()));
		} else {
			minWidth = Integer.parseInt(measurement);
		}
		return minWidth;
	}
}
