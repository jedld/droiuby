package com.droiuby.client.core.builder;

import java.util.HashMap;

import org.jdom2.Attribute;
import org.jdom2.Element;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.droiuby.client.core.PropertyValue;
import com.droiuby.client.core.ViewExtras;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class ViewBuilder {

	Context context;
	ActivityBuilder builder;
	int paddingTop = 0, paddingBottom = 0, paddingLeft = 0, paddingRight = 0;

	public ViewBuilder() {

	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public ActivityBuilder getBuilder() {
		return builder;
	}

	public void setBuilder(ActivityBuilder builder) {
		this.builder = builder;
	}

	public ViewBuilder(ActivityBuilder builder, Context context) {
		this.context = context;
		this.builder = builder;
	}

	public View getView() {
		return new View(context);
	}

	public static HashMap<String, String> toPropertyMap(Element elem) {
		HashMap<String, String> propertyMap = new HashMap<String, String>();
		for (Attribute attribute : elem.getAttributes()) {
			propertyMap.put(attribute.getName(), attribute.getValue());
		}
		return propertyMap;
	}

	public View setParams(View child, Element elem) {
		setParamsFromProperty(child, ViewBuilder.toPropertyMap(elem));
		return child;
	}

	public int parseGravity(String gravityStr) {
		return ActivityBuilder.parseGravity(gravityStr);
	}

	private void addRule(HashMap<String, String> propertyMap,
			RelativeLayout.LayoutParams params, String attribute, int property) {
		String attributeString = propertyMap.get(attribute);
		if (attributeString != null) {
			View view = (View) builder.findViewByName(attributeString);
			if (view != null) {
				Log.d(this.getClass().toString(), "loading " + attributeString
						+ " view = " + view.getClass().toString());
				params.addRule(property, view.getId());
			} else {
				Log.e(this.getClass().toString(), "cannot locate "
						+ attributeString);
			}
		}
	}

	public RelativeLayout.LayoutParams setRelativeLayoutParams(
			HashMap<String, String> propertyMap) {
		int width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
		int height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
		int leftMargin = 0, rightMargin = 0, topMargin = 0, bottomMargin = 0;

		for (String key : propertyMap.keySet()) {
			String attribute_name = key;
			String attribute_value = propertyMap.get(key);
			
			Log.v(this.getClass().toString(), "setting layout " + attribute_name + " = " + attribute_value);
			if (attribute_name.equals("height")) {
				if (attribute_value.equalsIgnoreCase("match")) {
					height = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
				} else {
					height = toPixels(attribute_value);
				}
			} else if (attribute_name.equals("width")) {
				if (attribute_value.equalsIgnoreCase("match")) {
					width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
				} else {
					width = toPixels(attribute_value);
				}
			} else if (attribute_name.equals("left_margin")
					|| attribute_name.equals("margin-left")) {
				leftMargin = toPixels(attribute_value);
			} else

			if (attribute_name.equals("right_margin")
					|| attribute_name.equals("margin-right")) {
				rightMargin = toPixels(attribute_value);
			} else if (attribute_name.equals("top_margin")) {
				topMargin = toPixels(attribute_value);
			} else if (attribute_name.equals("bottom_margin")) {
				bottomMargin = toPixels(attribute_value);
			}
		}

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				width, height);

		addRule(propertyMap, params, "left_of", RelativeLayout.LEFT_OF);
		addRule(propertyMap, params, "right_of", RelativeLayout.RIGHT_OF);
		addRule(propertyMap, params, "below", RelativeLayout.BELOW);
		addRule(propertyMap, params, "above", RelativeLayout.ABOVE);

		String parent_left = propertyMap.get("parent_left");
		if (parent_left != null) {
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, -1);
		}

		String parent_right = propertyMap.get("parent_right");
		if (parent_right != null) {
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, -1);
		}

		String parent_center = propertyMap.get("parent_center");
		if (parent_center != null) {
			params.addRule(RelativeLayout.CENTER_IN_PARENT, -1);
		}

		String parent_bottom = propertyMap.get("parent_bottom");
		if (parent_bottom != null) {
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, -1);
		}
		params.leftMargin = leftMargin;
		params.topMargin = topMargin;
		params.bottomMargin = bottomMargin;
		params.rightMargin = rightMargin;
		return params;
	}

	public android.view.ViewGroup.LayoutParams setLayoutParams(
			android.view.ViewGroup.LayoutParams layoutParams,
			HashMap<String, String> propertyMap) {
		int width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
		int height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

		float weight = 0;
		int leftMargin = 0, rightMargin = 0, topMargin = 0, bottomMargin = 0;
		int gravity = Gravity.NO_GRAVITY;

		if (layoutParams != null) {
			if (layoutParams instanceof MarginLayoutParams) {
				MarginLayoutParams linearlayoutParams = (MarginLayoutParams) layoutParams;

				leftMargin = linearlayoutParams.leftMargin;
				rightMargin = linearlayoutParams.rightMargin;
				topMargin = linearlayoutParams.topMargin;
				bottomMargin = linearlayoutParams.bottomMargin;

				if (layoutParams instanceof LinearLayout.LayoutParams) {
					LinearLayout.LayoutParams llparams = (LinearLayout.LayoutParams) layoutParams;
					weight = llparams.weight;
					gravity = llparams.gravity;
				}
			}
			width = layoutParams.width;
			height = layoutParams.height;
		}

		for (String key : propertyMap.keySet()) {
			String attribute_name = key;
			String attribute_value = propertyMap.get(key);
			if (attribute_name.equals("height")) {
				if (attribute_value.equalsIgnoreCase("match")) {
					height = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
				} else if (attribute_value.equalsIgnoreCase("wrap")) {
					height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
				} else {
					height = toPixels(attribute_value);
				}
			} else if (attribute_name.equals("width")) {
				if (attribute_value.equalsIgnoreCase("match")) {
					width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
				} else if (attribute_value.equalsIgnoreCase("wrap")) {
					width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
				} else {
					width = toPixels(attribute_value);
				}
			} else if (attribute_name.equals("weight")) {
				weight = Float.parseFloat(attribute_value);
			} else if (attribute_name.equals("left_margin")
					|| attribute_name.equals("margin-left")) {
				leftMargin = toPixels(attribute_value);
			} else if (attribute_name.equals("right_margin")
					|| attribute_name.equals("margin-right")) {
				rightMargin = toPixels(attribute_value);
			} else if (attribute_name.equals("top_margin")
					|| attribute_name.equals("margin-top")) {
				topMargin = toPixels(attribute_value);
			} else if (attribute_name.equals("bottom_margin")
					|| attribute_name.equals("margin-bottom")) {
				bottomMargin = toPixels(attribute_value);
			} else if (attribute_name.equals("g") || attribute_name.equals("layout_gravity")
					|| attribute_name.equals("float")) {
				gravity = parseGravity(attribute_value);
			}

		}

		android.view.ViewGroup.LayoutParams params = null;
		if (layoutParams != null) {
			params = layoutParams;
		} else {
			params = new LinearLayout.LayoutParams(width, height, weight);
		}

		if (layoutParams instanceof MarginLayoutParams) {
			MarginLayoutParams linearlayoutParams = (MarginLayoutParams) params;

			linearlayoutParams.leftMargin = leftMargin;
			linearlayoutParams.rightMargin = rightMargin;
			linearlayoutParams.topMargin = topMargin;
			linearlayoutParams.bottomMargin = bottomMargin;

			if (layoutParams instanceof LinearLayout.LayoutParams) {
				LinearLayout.LayoutParams llparams = (LinearLayout.LayoutParams) params;
				llparams.weight = weight;
				llparams.gravity = gravity;
			}
		}
		params.width = width;
		params.height = height;
		return params;
	}

	public View setProperty(View child, String property, String value) {
		HashMap<String, String> propertyMap = new HashMap<String, String>();
		propertyMap.put(property, value);
		return setParamsFromProperty(child, propertyMap);
	}
	
	public View setParamsFromProperty(View child,
			HashMap<String, String> propertyMap) {

		for (String key : propertyMap.keySet()) {
			String attribute_name = key;
			String attribute_value = propertyMap.get(key);
			mapAttribute(child, attribute_name, attribute_value);
		}

		ViewParent parent = child.getParent();

		if (parent != null) {
			if (parent instanceof RelativeLayout) {
				child.setLayoutParams(setRelativeLayoutParams(propertyMap));
			} else if (parent instanceof FrameLayout) {
				child.setLayoutParams(new FrameLayout.LayoutParams(
						setLayoutParams(child.getLayoutParams(), propertyMap)));
			} else if (parent instanceof TableLayout) {
				child.setLayoutParams(new TableLayout.LayoutParams(
						setLayoutParams(child.getLayoutParams(), propertyMap)));
			} else if (parent instanceof TableRow) {
				// Do not set Layout
			} else {
				child.setLayoutParams(setLayoutParams(child.getLayoutParams(),
						propertyMap));
			}
		} else {
			child.setLayoutParams(setLayoutParams(child.getLayoutParams(),
					propertyMap));
		}

		return child;
	}

	protected void mapAttribute(View child, String attribute_name,
			String attribute_value) {
		if (attribute_name.equals("alpha") || attribute_name.equals("opacity")) {
			float alpha = Float.parseFloat(attribute_value);
			child.setAlpha(alpha);
		} else if (attribute_name.equals("padding_top")) {
			paddingTop = toPixels(attribute_value);
			child.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
		} else if (attribute_name.equals("padding_bottom")) {
			paddingBottom = toPixels(attribute_value);
			child.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
		} else if (attribute_name.equals("padding_left")) {
			paddingLeft = toPixels(attribute_value);
			child.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
		} else if (attribute_name.equals("padding_right")) { 
			paddingRight = toPixels(attribute_value);
			child.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
	    } else if (attribute_name.equals("padding")) { 
	    	int pixels  = toPixels(attribute_value);
	    	paddingTop = pixels;
	    	paddingBottom  = pixels;
	    	paddingLeft = pixels;
	    	paddingRight = pixels;
	    	child.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
	    } else if (attribute_name.equals("camera_distance")) {
			float camera_distance = Float.parseFloat(attribute_value);
			child.setCameraDistance(camera_distance);
		} else if (attribute_name.equals("pivot_y")) {
			float pivot_y = Float.parseFloat(attribute_value);
			child.setPivotY(pivot_y);
		} else if (attribute_name.equals("pivot_x")) {
			float pivot_x = Float.parseFloat(attribute_value);
			child.setPivotX(pivot_x);
		} else if (attribute_name.equals("rotation_x")) {
			float rotation_x = Float.parseFloat(attribute_value);
			child.setRotationX(rotation_x);
		} else if (attribute_name.equals("rotation")) {
			float rotation = Float.parseFloat(attribute_value);
			child.setRotation(rotation);
		} else if (attribute_name.equals("x")) {
			child.setX(Float.parseFloat(attribute_value));
		} else if (attribute_name.equals("background_color")
				|| attribute_name.equals("background-color")) {
			child.setBackgroundColor(Color.parseColor(attribute_value));
		} else if (attribute_name.equals("y")) {
			child.setY(Float.parseFloat(attribute_value));
		} else if (attribute_name.equals("bottom")) {
			child.setBottom(toPixels(attribute_value));
		} else if (attribute_name.equals("min_height")) {
			child.setMinimumHeight(toPixels(attribute_value));
		} else if (attribute_name.equals("min_width")) {
			child.setMinimumWidth(toPixels(attribute_value));
		} else if (attribute_name.equals("background") || attribute_name.equals("background-image")) {
			if (attribute_value != null) {
				if (attribute_value.startsWith("#")) {
					child.setBackgroundColor(Color.parseColor(attribute_value));
				} else if (attribute_value.startsWith("@drawable:")) {
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
					if (attribute_value.startsWith("url(")) {
						attribute_value.substring(3, attribute_value.length() - 2);
					}
					UrlImageViewHelper.setUrlDrawable(child,
							builder.normalizeUrl(attribute_value),
							"setBackgroundDrawable");
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

	public View build(Element element) {
		// Log.d(this.getClass().toString(), "build.");
		View view = getView();
		return view;
	}

	protected void setAlpha(View v, Element e) {
		float alpha = 1;
		if (e.getAttributeValue("alpha") != null) {
			alpha = Float.parseFloat(e.getAttributeValue("alpha"));
		}

		v.setAlpha(alpha);
	}

	protected float toSize(String measurement) {
		float transformed_measurement = 0;
		Resources r = context.getResources();
		if (measurement.endsWith("dp") || measurement.endsWith("dip")) {
			int s = 2;
			if (measurement.endsWith("dip")) {
				s = 3;
			}
			transformed_measurement = TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP,
					Float.parseFloat(measurement.substring(0,
							measurement.length() - s)), r.getDisplayMetrics());
		} else if (measurement.endsWith("px")) {
			transformed_measurement = TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_PX,
					Float.parseFloat(measurement.substring(0,
							measurement.length() - 2)), r.getDisplayMetrics());
		} else if (measurement.endsWith("sp")) {
			transformed_measurement = TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_SP,
					Float.parseFloat(measurement.substring(0,
							measurement.length() - 2)), r.getDisplayMetrics());
		} else if (measurement.endsWith("pt")) {
			transformed_measurement = TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_PT,
					Float.parseFloat(measurement.substring(0,
							measurement.length() - 2)), r.getDisplayMetrics());
		} else {
			transformed_measurement = Float.parseFloat(measurement);
		}
		return transformed_measurement;
	}

	protected int toPixels(String measurement) {
		return Math.round(toSize(measurement));
	}

	public boolean hasSubElements() {
		return false;
	}

	public static ViewBuilder getBuilderForView(View view, Context c,
			ActivityBuilder builder) {
		Object tag = view.getTag();
		if (tag != null && tag instanceof ViewExtras) {
			ViewExtras viewExtras = (ViewExtras) tag;
			Class<ViewBuilder> builderClass = viewExtras.getBuilder();
			if (builderClass != null) {
				try {
					ViewBuilder viewBuilder = builderClass.newInstance();
					viewBuilder.setBuilder(builder);
					viewBuilder.setContext(c);
					return viewBuilder;
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}
		return null;
	}
}
