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
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.droiuby.client.core.ActivityBuilder;
import com.droiuby.client.core.PropertyValue;
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

	public View setParams(View child, Element elem) {
		HashMap<String, String> propertyMap = new HashMap<String, String>();
		for (Attribute attribute : elem.getAttributes()) {
			propertyMap.put(attribute.getName(), attribute.getValue());
		}
		setParamsFromProperty(child, propertyMap);

		return child;
	}

	public int parseGravity(String gravityStr) {
		int gravity = Gravity.NO_GRAVITY;
		if (gravityStr.equalsIgnoreCase("left")) {
			gravity |= Gravity.LEFT;
		} else if (gravityStr.equalsIgnoreCase("right")) {
			gravity |= Gravity.RIGHT;
		}

		if (gravityStr.equalsIgnoreCase("top")) {
			gravity |= Gravity.TOP;
		} else if (gravityStr.equalsIgnoreCase("bottom")) {
			gravity |= Gravity.BOTTOM;
		}

		if (gravityStr.equalsIgnoreCase("center")) {
			gravity |= Gravity.CENTER;
		}
		return gravity;
	}

	private void addRule(HashMap<String, String> propertyMap, RelativeLayout.LayoutParams params,
			String attribute, int property) {
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
	
	public RelativeLayout.LayoutParams setRelativeLayoutParams(HashMap<String, String> propertyMap) {
		int width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
		int height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
		int leftMargin = 0, rightMargin = 0, topMargin = 0, bottomMargin = 0;

		for (String key : propertyMap.keySet()) {
			String attribute_name = key;
			String attribute_value = propertyMap.get(key);
			
			if (attribute_name.equals("height")) {
				if (attribute_value.equalsIgnoreCase("match")) {
					height = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
				} else {
					height = toPixels(attribute_value);
				}
			} else
			if (attribute_name.equals("width")) {
				if (attribute_value.equalsIgnoreCase("match")) {
					width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
				} else {
					width = toPixels(attribute_value);
				}
			} else
				if (attribute_name.equals("left_margin") || attribute_name.equals("margin-left")) {
				leftMargin = toPixels(attribute_value);
				} else

			if (attribute_name.equals("right_margin") || attribute_name.equals("margin-right")) {
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
	
	public LayoutParams setLayoutParams(HashMap<String, String> propertyMap) {
		int width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
		int height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

		float weight = 0;
		int leftMargin = 0, rightMargin = 0, topMargin = 0, bottomMargin = 0;
		int gravity = Gravity.NO_GRAVITY;

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
			} else if (attribute_name.equals("g")
					|| attribute_name.equals("float")) {
				gravity = parseGravity(attribute_value);
			}

		}

		LayoutParams params = new LayoutParams(width, height, weight);
		params.leftMargin = leftMargin;
		params.topMargin = topMargin;
		params.bottomMargin = bottomMargin;
		params.rightMargin = rightMargin;
		params.gravity = gravity;
		return params;
	}

	public View setParamsFromProperty(View child,
			HashMap<String, String> propertyMap) {

		for (String key : propertyMap.keySet()) {
			String attribute_name = key;
			String attribute_value = propertyMap.get(key);
			if (attribute_name.equals("alpha")) {
				float alpha = Float.parseFloat(attribute_value);
				child.setAlpha(alpha);
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
			} else if (attribute_name.equals("background_color")) {
				child.setBackgroundColor(Color.parseColor(attribute_value));
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
					if (attribute_value.startsWith("#")) {
						child.setBackgroundColor(Color
								.parseColor(attribute_value));
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

		ViewParent parent = child.getParent();
		
		if (parent!=null) {
			if (parent instanceof RelativeLayout) {
				child.setLayoutParams(setRelativeLayoutParams(propertyMap));
			} else if (parent instanceof FrameLayout) {
				child.setLayoutParams(new FrameLayout.LayoutParams(setLayoutParams(propertyMap)));
			} else if (parent instanceof TableLayout) {
				child.setLayoutParams(new TableLayout.LayoutParams(setLayoutParams(propertyMap)));
			} else if (parent instanceof TableRow) {
				// Do not set Layout
			} else {
				child.setLayoutParams(setLayoutParams(propertyMap));	
			}
		} else {
			child.setLayoutParams(setLayoutParams(propertyMap));	
		}
		
		return child;
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

	public boolean hasSubElements() {
		return false;
	}

	public static ViewBuilder getBuilderForView(View view, Context c,
			ActivityBuilder builder) {
		return new ViewBuilder(builder, c);
	}
}
