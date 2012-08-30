package com.dayosoft.activeapp.core;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.dayosoft.activeapp.R;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import android.app.Activity;
import android.text.Layout;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

public class ActivityBuilder {

	public static final int PARTIAL_REPLACE = 1;
	public static final int PARTIAL_REPLACE_CHILDREN = 2;
	Activity context;
	Element rootElement;
	ViewGroup target;
	HashMap<String, Integer> namedViewDictionary = new HashMap<String, Integer>();
	HashMap<String, ArrayList<Integer>> classViewDictionary = new HashMap<String, ArrayList<Integer>>();

	public HashMap<String, ArrayList<Integer>> getClassViewDictionary() {
		return classViewDictionary;
	}

	public void setClassViewDictionary(
			HashMap<String, ArrayList<Integer>> classViewDictionary) {
		this.classViewDictionary = classViewDictionary;
	}

	public HashMap<String, Integer> getNamedViewDictionary() {
		return namedViewDictionary;
	}

	public void setNamedViewDictionary(
			HashMap<String, Integer> namedViewDictionary) {
		this.namedViewDictionary = namedViewDictionary;
	}

	public ActivityBuilder(Document document, Activity context) {
		this.context = context;
		this.rootElement = document.getRootElement();
		this.target = (ViewGroup) context.findViewById(R.id.mainLayout);
	}

	public ActivityBuilder(Document document, Activity context, ViewGroup target) {
		this.target = target;
		this.context = context;
		this.rootElement = document.getRootElement();
	}

	public void build() {
		target.removeAllViews();
		parse(rootElement, target);
	}

	public void setMargins(View v, Element e) {

	}

	public View findViewByName(String selector) {
		selector = selector.trim();
		if (selector.startsWith("#")) {
			String name = selector.substring(1);
			int id = namedViewDictionary.get(name);
			return context.findViewById(id);
		} else if (selector.startsWith("@")) {
			String name = selector.substring(1);
			int id = getDrawable(name);
			return context.findViewById(id);
		}
		return null;
	}

	public void parsePartialReplaceChildren(ViewGroup view, String partial) {
		parsePartial(view, partial, PARTIAL_REPLACE_CHILDREN);
	}

	public void parsePartial(ViewGroup view, String partial, int operation) {
		SAXBuilder sax = new SAXBuilder();
		try {
			Document doc = sax.build(new StringReader("<partial>" + partial
					+ "</partial>"));
			Element e = doc.getRootElement();
			if (operation == PARTIAL_REPLACE_CHILDREN) {
				view.removeAllViews();
				parse(e, view);
			}
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setAlpha(View v, Element e) {
		float alpha = 1;
		if (e.getAttributeValue("alpha") != null) {
			alpha = Float.parseFloat(e.getAttributeValue("alpha"));
		}

		v.setAlpha(alpha);
	}

	public LayoutParams setParams(Element e) {
		int width = LayoutParams.WRAP_CONTENT;
		int height = LayoutParams.WRAP_CONTENT;
		float weight = 0;
		int leftMargin = 0, rightMargin = 0, topMargin = 0, bottomMargin = 0;
		int gravity = Gravity.NO_GRAVITY;

		if (e.getAttributeValue("height") != null) {

			if (e.getAttributeValue("height").equalsIgnoreCase("match")) {
				height = LayoutParams.MATCH_PARENT;
			} else {
				height = Integer.parseInt(e.getAttributeValue("height"));
			}
		}

		if (e.getAttributeValue("width") != null) {
			if (e.getAttributeValue("width").equalsIgnoreCase("match")) {
				width = LayoutParams.MATCH_PARENT;
			} else {
				width = Integer.parseInt(e.getAttributeValue("width"));
			}
		}

		if (e.getAttributeValue("weight") != null) {
			weight = Float.parseFloat(e.getAttributeValue("weight"));
		}

		if (e.getAttributeValue("gravity") != null) {
			String gravityStr = e.getAttributeValue("gravity");

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
		}

		// Margins
		String lm = e.getAttributeValue("left_margin");
		if (lm != null) {
			leftMargin = Integer.parseInt(lm);
		}

		String rm = e.getAttributeValue("right_margin");
		if (rm != null) {
			rightMargin = Integer.parseInt(rm);
		}

		String tm = e.getAttributeValue("top_margin");
		if (tm != null) {
			topMargin = Integer.parseInt(tm);
		}

		String bm = e.getAttributeValue("bottom_margin");
		if (bm != null) {
			bottomMargin = Integer.parseInt(bm);
		}

		LayoutParams params = new LayoutParams(width, height, weight);
		params.leftMargin = leftMargin;
		params.topMargin = topMargin;
		params.bottomMargin = bottomMargin;
		params.rightMargin = rightMargin;
		params.gravity = gravity;
		return params;
	}

	private int getDrawable(String drawable) {
		try {
			Class c = Class.forName("R.drawable");
			Field f = c.getField(drawable);
			return f.getInt(new R.drawable());
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	private void registerView(ViewGroup group, View child, Element e) {

		if (e.getAttributeValue("id") != null) {
			String attr_name = e.getAttributeValue("id");
			int hash_code = Math.abs(attr_name.hashCode());
			child.setId(hash_code);
			namedViewDictionary.put(attr_name, hash_code);
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

		if (e.getAttributeValue("class") != null) {
			String class_name = e.getAttributeValue("class");
			if (child.getId() == 0) {
				int id = 0;
				do {
					id = (int) (Math.random() * Integer.MAX_VALUE);
				} while (namedViewDictionary.containsValue(id));
				child.setId(id);
			}
			for (String item : class_name.split(" ")) {
				ArrayList<Integer> list = classViewDictionary.get(item);
				if (list == null) {
					list = new ArrayList<Integer>();
					classViewDictionary.put(item, list);
				}
				list.add(child.getId());
			}
		}
		if (e.getAttribute("enabled") != null) {
			String enabled = e.getAttributeValue("enabled");
			child.setEnabled(enabled.equalsIgnoreCase("false") ? false : true);
		}

		if (e.getAttribute("background") != null) {
			String src = e.getAttributeValue("background");
			ImageView imageView = new ImageView(context);
			if (src != null) {
				if (src.indexOf("@drawable:") != -1) {
					String drawable = src.substring(10);
					int resId = getDrawable(drawable);
					if (resId != 0) {
						child.setBackgroundResource(resId);
					}
				} else {
					UrlImageViewHelper.setUrlDrawable(imageView, src);
					child.setBackgroundDrawable(imageView.getDrawable());
				}
			}
		}

		setAlpha(child, e);
		group.addView(child, setParams(e));
	}

	public void parse(Element element, ViewGroup view) {
		List<Element> elems = element.getChildren();
		for (Element e : elems) {
			String elemName = e.getName().toLowerCase();
			if (elemName.equals("div") || elemName.equals("span")) {
				FrameLayout layout = new FrameLayout(context);
				if ((e.getAttributeValue("foreground_gravity") != null)) {
					int gravity = Integer.parseInt(e
							.getAttributeValue("foreground_gravity"));
					layout.setForegroundGravity(gravity);
				}
				registerView(view, layout, e);
				parse(e, layout);
			} else if (elemName.equals("layout")) {
				String type = e.getAttributeValue("type").toLowerCase();
				if (type.equals("frame")) {
					FrameLayout layout = new FrameLayout(context);
					if ((e.getAttributeValue("foreground_gravity") != null)) {
						int gravity = Integer.parseInt(e
								.getAttributeValue("foreground_gravity"));
						layout.setForegroundGravity(gravity);
					}
					registerView(view, layout, e);
					parse(e, layout);
				} else if (type.equals("linear")) {
					int orientation = LinearLayout.VERTICAL;
					if ((e.getAttributeValue("orientation") != null)
							&& e.getAttributeValue("orientation")
									.equalsIgnoreCase("horizontal")) {
						orientation = LinearLayout.HORIZONTAL;
					}
					LinearLayout layout = new LinearLayout(context);
					layout.setOrientation(orientation);
					registerView(view, layout, e);

					parse(e, layout);
				} else if (type.equals("scroll")) {
					ScrollView scroll_view = new ScrollView(context);
					view.addView(scroll_view, setParams(e));
					parse(e, scroll_view);
				}
			} else if (elemName.equals("t")) {
				TextView textView = new TextView(context);
				String fontSize = e.getAttributeValue("size");
				if (fontSize != null) {
					textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
							Float.parseFloat(fontSize));
				}
				String content = e.getTextTrim() != null ? e.getTextTrim() : "";
				textView.setText(content);
				registerView(view, textView, e);
			} else if (elemName.equals("button")) {
				Button button = new Button(context);
				String content = e.getTextTrim() != null ? e.getTextTrim() : "";
				button.setText(content);
				registerView(view, button, e);
			} else if (elemName.equals("input")) {
				EditText editText = new EditText(context);

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
				registerView(view, editText, e);
			} else if (elemName.equals("img")) {
				ImageView img = new ImageView(context);
				String src = e.getAttributeValue("src");

				if (src != null) {
					if (src.startsWith("@drawable:")) {
						String drawable = src.substring(10);
						int resId = getDrawable(drawable);
						if (resId != 0) {
							img.setImageResource(resId);
						}
					} else {
						UrlImageViewHelper.setUrlDrawable(img, src);
					}
				}
				registerView(view, img, e);
			}
		}
	}
}
