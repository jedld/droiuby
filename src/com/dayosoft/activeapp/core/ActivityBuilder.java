package com.dayosoft.activeapp.core;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.EvalFailedException;
import org.jruby.embed.ScriptingContainer;

import com.dayosoft.activeapp.R;
import com.dayosoft.activeapp.utils.Utils;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.Layout;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

class ActivityBootstrapper extends AsyncTask<Void, Void, ActivityBuilder> {

	ActiveApp app;
	Activity targetActivity;
	Document mainActivityDocument;
	ScriptingContainer scriptingContainer;
	String baseUrl;
	String controller;
	String pageUrl;
	ExecutionBundle executionBundle;
	private EmbedEvalUnit preParsedScript;
	SAXBuilder sax = new SAXBuilder();
	int method;

	public ActivityBootstrapper(ExecutionBundle executionBundle, ActiveApp app,
			String pageUrl, int method, Activity targetActivity) {
		this.app = app;
		this.pageUrl = pageUrl;
		this.executionBundle = executionBundle;
		this.targetActivity = targetActivity;
		this.baseUrl = app.getBaseUrl();
		this.scriptingContainer = executionBundle.getContainer();
		this.method = method;
	}

	public String loadAsset(String asset_name, int method) {
		if (asset_name.startsWith("asset:")) {
			return Utils.loadAsset(targetActivity, asset_name);
		} else {
			if (baseUrl.indexOf("asset:") != -1) {
				return Utils.loadAsset(targetActivity, baseUrl + asset_name);
			} else if (baseUrl.indexOf("file:") != -1) {
				return Utils.loadFile(asset_name);
			} else if (baseUrl.indexOf("sdcard:") != -1) {
				File directory = Environment.getExternalStorageDirectory();
				try {
					String asset_path = directory.getCanonicalPath()
							+ asset_name;
					return Utils.loadFile(asset_path);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			} else {
				if (asset_name.startsWith("/")) {
					asset_name = asset_name.substring(1);
				}
				return Utils.query(baseUrl + "/" + asset_name, targetActivity,
						method);
			}
		}
	}

	@Override
	protected ActivityBuilder doInBackground(Void... params) {
		String responseBody = loadAsset(pageUrl, method);
		Log.d(this.getClass().toString(), responseBody);
		try {
			mainActivityDocument = sax.build(new StringReader(responseBody));
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		controller = mainActivityDocument.getRootElement().getAttributeValue(
				"controller");
		String baseUrl = app.getBaseUrl();

		if (controller != null) {
			Log.d("Activity loader", "loading controller file " + baseUrl
					+ controller);
			String controller_content = "class MainActivity < ActivityWrapper\n"
					+ loadAsset(controller, Utils.HTTP_GET) + "\n end\n";
			preParsedScript = Utils.preParseRuby(scriptingContainer,
					controller_content, targetActivity);
		}
		ActivityBuilder builder = new ActivityBuilder(mainActivityDocument,
				targetActivity);
		executionBundle.getPayload().setActivityBuilder(builder);
		executionBundle.getPayload().setExecutionBundle(executionBundle);
		executionBundle.getPayload().setActiveApp(app);
		AssetManager manager = targetActivity.getAssets();
		try {
			scriptingContainer.parse(manager.open("lib/bootstrap.rb"),
					"lib/bootstrap.rb").run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		builder.preload();
		return builder;
	}

	@Override
	protected void onPostExecute(ActivityBuilder result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		result.build();
		try {
			if (preParsedScript != null) {
				long start = System.currentTimeMillis();
				preParsedScript.run();
				scriptingContainer
						.runScriptlet("$main_activty = MainActivity.new; $main_activty.on_create");
				long elapsed = System.currentTimeMillis() - start;
				Log.d(this.getClass().toString(),
						"ruby segment: on_create() elapsed time = " + elapsed
								+ "ms");
			}
		} catch (EvalFailedException e) {
			Log.e(this.getClass().toString(), e.getMessage());
		}

	}

}

public class ActivityBuilder {

	public static final int PARTIAL_REPLACE = 1;
	public static final int PARTIAL_REPLACE_CHILDREN = 2;
	public static final int PARTIAL_APPEND_CHILDREN = 3;

	Activity context;
	Element rootElement;
	ViewGroup target;
	HashMap<String, Drawable> preloadedResource = new HashMap<String, Drawable>();
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

	public void preload() {
		List<Element> children = rootElement.getChildren("preload");
		for (Element elem : children) {
			String name = elem.getAttributeValue("id");
			String type = elem.getAttributeValue("type");
			String src = elem.getAttributeValue("src");
			if (type.equals("image")) {
				Log.d(this.getClass().toString(), "preloading " + src + " ... ");
				Drawable drawable = UrlImageViewHelper.downloadFromUrl(context,
						src, UrlImageViewHelper.CACHE_DURATION_ONE_DAY);
				this.preloadedResource.put(name, drawable);
			}
		}
	}

	public static void loadLayout(ExecutionBundle executionBundle,
			ActiveApp app, String pageUrl, int method, Activity targetActivity) {

		ActivityBootstrapper bootstrapper = new ActivityBootstrapper(
				executionBundle, app, pageUrl, method, targetActivity);
		bootstrapper.execute();
	}

	public void build() {
		target.removeAllViews();
		try {
			parse(rootElement, target);
		} catch (Exception e) {
			TextView view = new TextView(context);
			view.setText(e.getMessage());
			view.setTextColor(Color.RED);
			target.addView(view);
		}
	}

	public void setMargins(View v, Element e) {

	}

	public Object findViewByName(String selector) {
		selector = selector.trim();
		if (selector.startsWith("#")) {
			String name = selector.substring(1);
			if (namedViewDictionary.containsKey(name)) {
				int id = namedViewDictionary.get(name);
				return context.findViewById(id);
			}
			return null;
		} else if (selector.startsWith("@drawable:")) {
			String name = selector.substring(10);
			int id = getDrawableId(name);
			return context.findViewById(id);
		} else if (selector.startsWith("@preload:")) {
			String name = selector.substring(9);
			return preloadedResource.get(name);
		} else {
			String name = selector.substring(1);
			int id = getDrawableId(name);
			return context.findViewById(id);
		}
	}

	public void parsePartialReplaceChildren(ViewGroup view, String partial) {
		parsePartial(view, partial, PARTIAL_REPLACE_CHILDREN);
	}

	public void parsePartialAppendChildren(ViewGroup view, String partial) {
		parsePartial(view, partial, PARTIAL_APPEND_CHILDREN);
	}

	public void appendChild(ViewGroup group, View view) {
		group.addView(view);
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
			} else if (operation == PARTIAL_APPEND_CHILDREN) {
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

	public RelativeLayout.LayoutParams setRelativeLayoutParams(Element e) {
		int width = LayoutParams.WRAP_CONTENT;
		int height = LayoutParams.WRAP_CONTENT;
		int leftMargin = 0, rightMargin = 0, topMargin = 0, bottomMargin = 0;

		if (e.getAttributeValue("height") != null) {

			if (e.getAttributeValue("height").equalsIgnoreCase("match")) {
				height = LayoutParams.MATCH_PARENT;
			} else {
				height = toPixels(e.getAttributeValue("height"));
			}
		}

		if (e.getAttributeValue("width") != null) {
			if (e.getAttributeValue("width").equalsIgnoreCase("match")) {
				width = LayoutParams.MATCH_PARENT;
			} else {
				width = toPixels(e.getAttributeValue("width"));
			}
		}

		// Margins
		String lm = e.getAttributeValue("left_margin");
		if (lm != null) {
			leftMargin = toPixels(lm);
		}

		String rm = e.getAttributeValue("right_margin");
		if (rm != null) {
			rightMargin = toPixels(rm);
		}

		String tm = e.getAttributeValue("top_margin");
		if (tm != null) {
			topMargin = toPixels(tm);
		}

		String bm = e.getAttributeValue("bottom_margin");
		if (bm != null) {
			bottomMargin = toPixels(bm);
		}

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				width, height);

		String left_of = e.getAttributeValue("left_of");
		if (left_of != null) {
			View view = (View) this.findViewByName(left_of);
			params.addRule(RelativeLayout.LEFT_OF, view.getId());
		}

		String right_of = e.getAttributeValue("right_of");
		if (right_of != null) {
			View view = (View) this.findViewByName(right_of);
			params.addRule(RelativeLayout.RIGHT_OF, view.getId());
		}

		String below = e.getAttributeValue("below");
		if (below != null) {
			View view = (View) this.findViewByName(below);
			params.addRule(RelativeLayout.BELOW, view.getId());
		}

		String above = e.getAttributeValue("above");
		if (above != null) {
			View view = (View) this.findViewByName(above);
			params.addRule(RelativeLayout.ABOVE, view.getId());
		}

		String parent_left = e.getAttributeValue("parent_left");
		if (parent_left != null) {
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, -1);
		}

		String parent_right = e.getAttributeValue("parent_right");
		if (parent_right != null) {
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, -1);
		}

		String parent_center = e.getAttributeValue("parent_center");
		if (parent_center != null) {
			params.addRule(RelativeLayout.CENTER_IN_PARENT, -1);
		}

		String parent_bottom = e.getAttributeValue("parent_bottom");
		if (parent_bottom != null) {
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, -1);
		}

		params.leftMargin = leftMargin;
		params.topMargin = topMargin;
		params.bottomMargin = bottomMargin;
		params.rightMargin = rightMargin;
		return params;
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
			} else if (e.getAttributeValue("height").equalsIgnoreCase("wrap")) {
				height = LayoutParams.WRAP_CONTENT;
			} else {
				height = toPixels(e.getAttributeValue("height"));
			}
		}

		if (e.getAttributeValue("width") != null) {
			if (e.getAttributeValue("width").equalsIgnoreCase("match")) {
				width = LayoutParams.MATCH_PARENT;
			} else if (e.getAttributeValue("width").equalsIgnoreCase("wrap")) {
				width = LayoutParams.WRAP_CONTENT;
			} else {
				width = toPixels(e.getAttributeValue("width"));
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
			leftMargin = toPixels(lm);
		}

		String rm = e.getAttributeValue("right_margin");
		if (rm != null) {
			rightMargin = toPixels(rm);
		}

		String tm = e.getAttributeValue("top_margin");
		if (tm != null) {
			topMargin = toPixels(tm);
		}

		String bm = e.getAttributeValue("bottom_margin");
		if (bm != null) {
			bottomMargin = toPixels(bm);
		}

		LayoutParams params = new LayoutParams(width, height, weight);
		params.leftMargin = leftMargin;
		params.topMargin = topMargin;
		params.bottomMargin = bottomMargin;
		params.rightMargin = rightMargin;
		params.gravity = gravity;
		return params;
	}

	private int getDrawableId(String drawable) {
		try {
			Field f = R.drawable.class.getField(drawable);
			return f.getInt(new R.drawable());
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

	private void handleIconDrawable(Element e, ImageButton child) {
		String src = e.getAttributeValue("background");
		if (src != null) {
			if (src.indexOf("@drawable:") != -1) {
				String drawable = src.substring(10);
				int resId = getDrawableId(drawable);
				if (resId != 0) {
					child.setImageResource(resId);
				}
			} else {
				UrlImageViewHelper.setUrlDrawable(child, src,
						"setBackgroundDrawable");
			}
		}
	}

	private void handleDrawable(Element e, View child) {
		String src = e.getAttributeValue("background");
		ImageView imageView = new ImageView(context);
		if (src != null) {
			if (src.indexOf("@drawable:") != -1) {
				String drawable = src.substring(10);
				int resId = getDrawableId(drawable);
				if (resId != 0) {
					child.setBackgroundResource(resId);
				}
			} else {
				UrlImageViewHelper.setUrlDrawable(imageView, src,
						"setBackgroundDrawable");
			}
		}
	}

	private void registerTextView(ViewGroup group, TextView child, Element e) {
		String drawable_left = e.getAttributeValue("drawable_left");
		Drawable drawableLeft = null, drawableTop = null, drawableRight = null, drawableBottom = null;
		// if (drawable_left!=null) {
		// drawableLeft = new ImageView(context);
		// UrlImageViewHelper.setUrlDrawable(drawableLeft, drawable_left);
		// }
		//

		child.setCompoundDrawables(drawableLeft, drawableTop, drawableRight,
				drawableBottom);
	}

	private int toPixels(String measurement) {
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

	private void registerView(ViewGroup group, View child, Element e) {

		ViewExtras extras = new ViewExtras();

		if (e.getAttributeValue("id") != null) {
			String attr_name = e.getAttributeValue("id");
			int hash_code = Math.abs(attr_name.hashCode());
			child.setId(hash_code);
			extras.setView_id(attr_name);
			namedViewDictionary.put(attr_name, hash_code);
		}

		if (e.getAttributeValue("name") != null) {
			String name = e.getAttributeValue("name");
			extras.setView_name(name);
		}

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
			extras.setView_class(class_name);
		}

		for (Attribute attribute : e.getAttributes()) {

			String attribute_name = attribute.getName();
			String attribute_value = attribute.getValue();

			if (attribute_name.startsWith("data-")) {
				HashMap<String, String> dataAttributes = extras
						.getDataAttributes();
				dataAttributes
						.put(attribute_name.substring(5), attribute_value);
			} else if (attribute_name.equals("min_height")) {
				child.setMinimumHeight(toPixels(attribute_value));
			} else if (attribute_name.equals("min_width")) {
				child.setMinimumWidth(toPixels(attribute_value));
			} else if (attribute_name.equals("background")) {
				if (attribute_value != null) {
					if (attribute_value.startsWith("@drawable:")) {
						String drawable = attribute_value.substring(10);
						int resId = getDrawableId(drawable);
						if (resId != 0) {
							child.setBackgroundResource(resId);
						}
					} else if (attribute_value.startsWith("@preload:")) {
						Drawable drawable = (Drawable) this
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
				if (attribute_value.equalsIgnoreCase("hidden")) {
					child.setVisibility(View.GONE);
				} else if (attribute_name.equals("invisible")) {
					child.setVisibility(View.INVISIBLE);
				} else if (attribute_name.equals("visible")) {
					child.setVisibility(View.VISIBLE);
				}
			}

		}

		child.setTag(extras);

		setAlpha(child, e);

		// RelativeLayout specific stuff
		if (group instanceof RelativeLayout) {
			((RelativeLayout) group).addView(child, setRelativeLayoutParams(e));
		} else {
			group.addView(child, setParams(e));
		}
	}

	public void parse(Element element, ViewGroup view) {
		List<Element> elems = element.getChildren();
		for (Element e : elems) {
			String node_name = e.getName().toLowerCase();
			if (node_name.equals("div") || node_name.equals("span")) {
				FrameLayout layout = new FrameLayout(context);
				if ((e.getAttributeValue("foreground_gravity") != null)) {
					int gravity = Integer.parseInt(e
							.getAttributeValue("foreground_gravity"));
					layout.setForegroundGravity(gravity);
				}
				registerView(view, layout, e);
				parse(e, layout);
			} else if (node_name.equals("layout")) {
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
				} else if (type.equals("relative")) {
					RelativeLayout layout = new RelativeLayout(context);
					registerView(view, layout, e);
					parse(e, layout);
				} else if (type.equals("scroll")) {
					ScrollView scroll_view = new ScrollView(context);
					view.addView(scroll_view, setParams(e));
					parse(e, scroll_view);
				}
			} else if (node_name.equals("web")) {
				WebView webview = new WebView(context);
				String url = e.getAttributeValue("src");
				if (url != null) {
					webview.loadUrl(url);
				}
				webview.loadUrl(url);
				webview.getSettings().setJavaScriptEnabled(true);
				webview.setWebViewClient(new WebViewClient() {
					@Override
					public boolean shouldOverrideUrlLoading(WebView view,
							String url) {
						view.loadUrl(url);
						return false;

					}
				});
				registerView(view, webview, e);
			} else if (node_name.equals("list")) {
				ListView list_view = new ListView(context);
				view.addView(list_view, setParams(e));
				parse(e, list_view);
			} else if (node_name.equals("t")) {
				TextView textView = new TextView(context);
				String fontSize = e.getAttributeValue("size");
				if (fontSize != null) {
					textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
							Float.parseFloat(fontSize));
				}

				String color = e.getAttributeValue("color");
				if (color != null) {
					textView.setTextColor(Color.parseColor(color));
				}

				String content = e.getTextTrim() != null ? e.getTextTrim() : "";
				textView.setText(content);
				registerView(view, textView, e);
			} else if (node_name.equals("button")) {
				Button button = new Button(context);
				String content = e.getTextTrim() != null ? e.getTextTrim() : "";
				button.setText(content);
				registerView(view, button, e);
			} else if (node_name.equals("image_button")) {
				ImageButton button = new ImageButton(context);

				this.handleIconDrawable(e, button);
				registerView(view, button, e);
			} else if (node_name.equals("checkbox")) {
				CheckBox checkbox = new CheckBox(context);
				registerView(view, checkbox, e);
			} else if (node_name.equals("input")) {
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
			} else if (node_name.equals("img")) {
				ImageView img = new ImageView(context);
				String src = e.getAttributeValue("src");

				if (src != null) {
					if (src.startsWith("@drawable:")) {
						String drawable = src.substring(10);
						int resId = getDrawableId(drawable);
						if (resId != 0) {
							img.setImageResource(resId);
						}
					} else if (src.startsWith("@preload:")) {
						Drawable drawable = (Drawable) this.findViewByName(src);
						if (drawable != null) {
							img.setImageDrawable(drawable);
						}
					} else {
						UrlImageViewHelper.setUrlDrawable(img, src,
								"setImageDrawable");
					}
				}
				registerView(view, img, e);
			}
		}
	}
}
