package com.droiuby.client.core.builder;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jruby.Ruby;
import org.jruby.RubyString;
import org.jruby.javasupport.JavaObject;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.droiuby.client.core.AssetDownloadCompleteListener;
import com.droiuby.client.core.AssetDownloadWorker;
import com.droiuby.client.core.CssRules;
import com.droiuby.client.core.ExecutionBundle;
import com.droiuby.client.core.ViewExtras;
import com.droiuby.client.core.postprocessor.AssetPreloadParser;
import com.droiuby.client.core.postprocessor.CssPreloadParser;
import com.droiuby.client.core.utils.Utils;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

class ReverseIdResolver {
	static ReverseIdResolver instance;
	Context context;

	SparseArray<String> resolveCache;

	protected ReverseIdResolver(Context context) {
		this.context = context;
	}

	public static ReverseIdResolver getInstance(Context context) {
		if (instance == null) {
			instance = new ReverseIdResolver(context);
		}
		return instance;
	}

	@SuppressWarnings("rawtypes")
	public String resolve(int id) {
		String packageName = context.getApplicationContext().getPackageName();
		if (resolveCache == null) {
			// Log.d(this.getClass().toString(),
			// "Initializing resolve cache ... ");
			resolveCache = new SparseArray<String>();
			Class c;
			try {
				c = Class.forName(packageName + ".R");
				for (Class sc : c.getDeclaredClasses()) {
					Log.d(this.getClass().toString(), "class = " + sc.getName());
					if (sc.getName().equals(packageName + ".R$id")) {
						for (Field f : sc.getFields()) {
							String name = f.getName();
							int key = f.getInt(sc.newInstance());
							// Log.d(this.getClass().toString(), "Storing " +
							// name
							// + " = " + key);
							resolveCache.put(key, name);
						}
					}
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			}

		}
		if (resolveCache.get(id) != null) {
			return resolveCache.get(id);
		}
		return null;

	}

}

@SuppressLint("UseValueOf")
public class ActivityBuilder {

	public static final int PARTIAL_REPLACE = 1;
	public static final int PARTIAL_REPLACE_CHILDREN = 2;
	public static final int PARTIAL_APPEND_CHILDREN = 3;

	Activity currentActivity;

	public Activity getContext() {
		return currentActivity;
	}

	public void setCurrentActivity(Activity context) {
		this.currentActivity = context;
	}

	Element rootElement;
	View topView;
	String baseUrl;

	@SuppressWarnings("rawtypes")
	static Class idClass;

	@SuppressWarnings("rawtypes")
	static Class drawableClass;

	@SuppressWarnings("rawtypes")
	static Class styleClass;

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public View getTopView() {
		return topView;
	}

	public View getRootView() {
		return currentActivity.findViewById(android.R.id.content);
	}

	ViewGroup target;
	HashMap<String, Object> preloadedResource = new HashMap<String, Object>();
	static HashMap<String, Object> rcache = new HashMap<String, Object>();
	SparseIntArray namedViewDictionary = new SparseIntArray();
	SparseArray<ArrayList<Integer>> classViewDictionary = new SparseArray<ArrayList<Integer>>();
	HashMap<String, ArrayList<Integer>> tagViewDictionary = new HashMap<String, ArrayList<Integer>>();
	ArrayList<String> viewErrors = new ArrayList<String>();

	public void addViewError(String error_msg) {
		viewErrors.add(error_msg);
		Log.e(this.getClass().toString(), error_msg);
	}

	public SparseArray<ArrayList<Integer>> getClassViewDictionary() {
		return classViewDictionary;
	}

	public void setClassViewDictionary(
			SparseArray<ArrayList<Integer>> classViewDictionary) {
		this.classViewDictionary = classViewDictionary;
	}

	public SparseIntArray getNamedViewDictionary() {
		return namedViewDictionary;
	}

	public void setNamedViewDictionary(SparseIntArray namedViewDictionary) {
		this.namedViewDictionary = namedViewDictionary;
	}

	public ActivityBuilder(Document document, Activity context, String baseUrl,
			int resId) {
		setup(document, context, baseUrl,
				(ViewGroup) context.findViewById(resId));
	}

	public ActivityBuilder(Document document, Activity context, String baseUrl) {
		setup(document, context, baseUrl, null);
	}

	public ActivityBuilder(Document document, Activity context, String baseUrl,
			ViewGroup target) {
		setup(document, context, baseUrl, target);
	}

	private void setup(Document document, Activity context, String baseUrl,
			ViewGroup target) {
		this.target = target;
		this.currentActivity = context;
		this.rootElement = document.getRootElement();
		this.baseUrl = baseUrl;
	}

	public ArrayList<Object> preload(Context context, ExecutionBundle bundle) {
		List<Element> children = rootElement.getChildren("preload");
		ArrayList<Object> resultBundle = new ArrayList<Object>();
		ExecutorService thread_pool = Executors.newFixedThreadPool(Runtime
				.getRuntime().availableProcessors() + 1);

		for (Element elem : children) {
			String name = elem.getAttributeValue("id");
			String type = elem.getAttributeValue("type");
			String src = elem.getAttributeValue("src");

			Log.d(this.getClass().toString(), "downloading " + src + " ...");
			AssetDownloadCompleteListener parser = null;

			int asset_type = Utils.ASSET_TYPE_TEXT;

			if (type.equals("image")) {
				asset_type = Utils.ASSET_TYPE_IMAGE;
				parser = new AssetPreloadParser(name, type, this);
			} else if (type.equals("css")) {
				asset_type = Utils.ASSET_TYPE_CSS;
				parser = new CssPreloadParser();
			} else if (type.equals("font") || type.equals("typeface")) {
				asset_type = Utils.ASSET_TYPE_TYPEFACE;
				parser = new AssetPreloadParser(name, type, this);
			} else {
				asset_type = Utils.ASSET_TYPE_BINARY;
				parser = new AssetPreloadParser(name, type, this);
			}

			AssetDownloadWorker worker = new AssetDownloadWorker(context,
					bundle.getPayload().getActiveApp(), bundle, src,
					asset_type, resultBundle, parser, Utils.HTTP_GET);
			thread_pool.execute(worker);
		}
		thread_pool.shutdown();
		try {
			Log.d(this.getClass().toString(),
					"Waiting for download workers to finish.....");
			thread_pool.awaitTermination(240, TimeUnit.SECONDS);
			Log.d(this.getClass().toString(), "Download workers .... done.");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return resultBundle;
	}

	public HashMap<String, Object> getPreloadedResource() {
		return preloadedResource;
	}

	public void setPreloadedResource(HashMap<String, Object> preloadedResource) {
		this.preloadedResource = preloadedResource;
	}

	public View prepare(ExecutionBundle bundle) {
		FrameLayout framelayout = new FrameLayout(currentActivity);
		framelayout.setLayoutParams(new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT, 0));
		try {
			parse(rootElement, framelayout, bundle);
		} catch (Exception e) {
			e.printStackTrace();
			TextView view = new TextView(currentActivity);
			view.setText(e.getMessage());
			view.setTextColor(Color.RED);
			framelayout.addView(view);
		}
		return framelayout;
	}

	public View setPreparedView(View view) {

		if (target == null) {
			target = (ViewGroup) currentActivity.getWindow().getDecorView()
					.findViewById(android.R.id.content);
		}

		target.removeAllViews();
		target.addView(view);
		return target;
	}

	public void setMargins(View v, Element e) {

	}

	public String normalizeUrl(String url) {
		// prepend base url if not full url
		if (!url.startsWith("http:") && !url.startsWith("https:")) {
			String base_url = this.baseUrl;
			if (!baseUrl.endsWith("/")) {
				base_url = base_url + "/";
			}
			if (url.startsWith("/")) {
				url = url.substring(1);
			}
			url = base_url + url;
		}
		return url;
	}

	public boolean checkParent(View view, View parent) {
		do {
			view = (View) view.getParent();

			if (parent.equals(view)) {
				return true;
			}
		} while (view != null);
		return false;
	}

	public Object findViewByName(String selector, View parentView,
			boolean inclusive) {
		Object result = (Object) findViewByName(selector);

		if (parentView == null)
			return result;

		if (result instanceof ArrayList) {
			@SuppressWarnings("unchecked")
			ArrayList<View> object_list = (ArrayList<View>) result;
			ArrayList<View> result_list = new ArrayList<View>();
			for (View v : object_list) {
				if (checkParent(v, parentView)) {
					result_list.add(v);
				}
			}
			return result_list;
		} else if (result instanceof View) {

			View view = (View) result;

			if (checkParent(view, parentView)) {
				return result;
			}
			return null;
		}

		return null;
	}

	public static Object resolveResource(Activity currentActivity,
			String selector) {

		if (rcache.containsKey(selector)) {
			return rcache.get(selector);
		}

		String name;
		Class<?> resourceClass;
		String selector_array[];

		int ptr = 0;
		
		if (selector.startsWith("android.R")) {
			selector_array = StringUtils.split(selector, ".");
			name = selector_array[2];

			resourceClass = getAndroidResourceComponentClass(currentActivity,
					name);

			if (selector_array.length <= 3) {
				rcache.put(selector, resourceClass);
				return resourceClass;
			}
			ptr = 3;

		} else {
			selector_array = StringUtils.split(selector, ".");
			name = selector_array[1];

			resourceClass = getResourceComponentClass(currentActivity, name);
			Log.v(ActivityBuilder.class.toString(), "R resolver " + name);

			Log.v(ActivityBuilder.class.toString(), "resource class "
					+ resourceClass.toString());

			if (selector_array.length <= 2) {
				rcache.put(selector, resourceClass);
				return resourceClass;
			}
			
			ptr = 2;
		}

		while (ptr < selector_array.length - 1) {
			
			String class_name = selector_array[ptr++];
			
			for (Class<?> klass : resourceClass.getDeclaredClasses()) {
				if (klass.getName().equals(class_name)) {
					resourceClass = klass;
					break;
				}
			}
		}
		
		String target_id = selector_array[ptr];

		Log.v(ActivityBuilder.class.toString(), "Class found resolving " + target_id);

		// match id with possible candidates
		for (Class<?> klass : resourceClass.getDeclaredClasses()) {
			if (klass.getName().equals(target_id)) {
				rcache.put(selector, klass);
				return klass;
			}
		}

		// search constants
		try {

			Object result = findConstantField(selector, target_id,
					resourceClass.getDeclaredFields());

			if (result != null)
				return result;

			result = findConstantField(selector, target_id, resourceClass.getFields());

			return result;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return null;
	}

	public Object findViewByName(String selector) {
		selector = selector.trim();
		if (selector.startsWith("#")) {
			String name = selector.substring(1);
			if (namedViewDictionary.get(name.hashCode()) != 0) {
				int id = namedViewDictionary.get(name.hashCode());
				View view = currentActivity.findViewById(id);
				if (view == null) {
					Log.w(this.getClass().toString(),
							"findViewById cannot locate " + name);
				}
				return view;
			}
			Log.w(this.getClass().toString(), "unknown id selector " + name);
			return null;
		} else if (selector.startsWith("@drawable:")) {
			String name = selector.substring(10);
			int id = getDrawableId(name);
			return currentActivity.findViewById(id);
		} else if (selector.startsWith("@preload:")) {
			String name = selector.substring(9);
			return preloadedResource.get(name);
		} else if (selector.startsWith(".")) {
			String name = selector.substring(1);
			ArrayList<View> object_list = new ArrayList<View>();
			if (classViewDictionary.get(name.hashCode()) != null) {
				ArrayList<Integer> list = classViewDictionary.get(name
						.hashCode());
				for (int id : list) {
					object_list.add(currentActivity.findViewById(id));
				}
			}
			return object_list;
		} else if (selector.startsWith("^")) {
			String name = selector.substring(1);
			int id = getViewById(currentActivity, name);
			if (id != 0) {
				return currentActivity.findViewById(id);
			} else {
				return null;
			}
		} else if (selector.startsWith("R.") || selector.startsWith("android.R.")) {
			return ActivityBuilder.resolveResource(currentActivity, selector);
		} else if (selector.startsWith("+")) {
			String name = selector.substring(1);
			int id = getDrawableId(name);
			if (id != 0) {
				return currentActivity.findViewById(id);
			} else {
				try {
					return currentActivity.findViewById(Integer.parseInt(name));
				} catch (java.lang.NumberFormatException e) {
					e.printStackTrace();
					return null;
				}
			}
		} else {
			ArrayList<View> object_list = new ArrayList<View>();
			if (tagViewDictionary.containsKey(selector)) {
				ArrayList<Integer> list = tagViewDictionary.get(selector);
				for (int id : list) {
					object_list.add(currentActivity.findViewById(id));
				}
			}
			return object_list;
		}
	}

	private static Object findConstantField(String selector, String id,
			Field[] fields) throws IllegalAccessException {
		for (Field f : fields) {

			Log.v(ActivityBuilder.class.toString(), " f " + f.getName());

			if (Modifier.isStatic(f.getModifiers()) && f.getName().equals(id)) {

				Object value = f.get(null);
				rcache.put(selector, value);
				return value;
			}
		}
		return null;
	}

	public void parsePartialReplaceChildren(ViewGroup view, String partial,
			ExecutionBundle bundle) {
		parsePartial(view, partial, PARTIAL_REPLACE_CHILDREN, bundle);
	}

	public void parsePartialAppendChildren(ViewGroup view, String partial,
			ExecutionBundle bundle) {
		parsePartial(view, partial, PARTIAL_APPEND_CHILDREN, bundle);
	}

	public void appendChild(ViewGroup group, View view) {
		group.addView(view);
	}

	public void parsePartial(ViewGroup view, String partial, int operation,
			ExecutionBundle bundle) {
		SAXBuilder sax = new SAXBuilder();
		try {
			Document doc = sax.build(new StringReader("<partial>" + partial
					+ "</partial>"));
			Element e = doc.getRootElement();
			if (operation == PARTIAL_REPLACE_CHILDREN) {
				view.removeAllViews();
				parse(e, view, bundle);
			} else if (operation == PARTIAL_APPEND_CHILDREN) {
				parse(e, view, bundle);
			}
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
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

	public String reverseLookupId(int id) {
		return ReverseIdResolver.getInstance(currentActivity).resolve(id);
	}

	private void addRule(Element e, RelativeLayout.LayoutParams params,
			String attribute, int property) {
		String attributeString = e.getAttributeValue(attribute);
		if (attributeString != null) {
			View view = (View) this.findViewByName(attributeString);
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

	public RelativeLayout.LayoutParams setRelativeLayoutParams(Element e) {
		int width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
		int height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
		int leftMargin = 0, rightMargin = 0, topMargin = 0, bottomMargin = 0;

		if (e.getAttributeValue("height") != null) {

			if (e.getAttributeValue("height").equalsIgnoreCase("match")) {
				height = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
			} else {
				height = toPixels(e.getAttributeValue("height"));
			}
		}

		if (e.getAttributeValue("width") != null) {
			if (e.getAttributeValue("width").equalsIgnoreCase("match")) {
				width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
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

		addRule(e, params, "left_of", RelativeLayout.LEFT_OF);
		addRule(e, params, "right_of", RelativeLayout.RIGHT_OF);
		addRule(e, params, "below", RelativeLayout.BELOW);
		addRule(e, params, "above", RelativeLayout.ABOVE);

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

	public TableRow.LayoutParams setTableParams(Element e, LayoutParams p) {
		TableRow.LayoutParams tableLayoutParams = new TableRow.LayoutParams(p);

		String column = e.getAttributeValue("column");
		if (column != null) {
			tableLayoutParams.column = Integer.parseInt(column);
		}

		String span = e.getAttributeValue("span");
		if (span != null) {
			tableLayoutParams.span = Integer.parseInt(span);
		}

		return tableLayoutParams;
	}

	public static int parseGravity(String gravityStr) {
		int gravity = Gravity.NO_GRAVITY;
		if (gravityStr.equalsIgnoreCase("left")) {
			gravity |= Gravity.LEFT;
		} else if (gravityStr.equalsIgnoreCase("right")) {
			gravity |= Gravity.RIGHT;
		} else if (gravityStr.equalsIgnoreCase("top")) {
			gravity |= Gravity.TOP;
		} else if (gravityStr.equalsIgnoreCase("bottom")) {
			gravity |= Gravity.BOTTOM;
		} else if (gravityStr.equalsIgnoreCase("center")) {
			gravity |= Gravity.CENTER;
		} else if (gravityStr.equalsIgnoreCase("center_horizontal")) {
			gravity |= Gravity.CENTER_HORIZONTAL;
		} else if (gravityStr.equalsIgnoreCase("center_vertical")) {
			gravity |= Gravity.CENTER_VERTICAL;
		}
		return gravity;
	}

	public LayoutParams setParams(Element e) {
		int width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
		int height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

		float weight = 0;
		int leftMargin = 0, rightMargin = 0, topMargin = 0, bottomMargin = 0;
		int gravity = Gravity.NO_GRAVITY;

		if (e.getAttributeValue("height") != null) {

			if (e.getAttributeValue("height").equalsIgnoreCase("match")) {
				height = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
			} else if (e.getAttributeValue("height").equalsIgnoreCase("wrap")) {
				height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
			} else {
				height = toPixels(e.getAttributeValue("height"));
			}
		}

		if (e.getAttributeValue("width") != null) {
			if (e.getAttributeValue("width").equalsIgnoreCase("match")) {
				width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
			} else if (e.getAttributeValue("width").equalsIgnoreCase("wrap")) {
				width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
			} else {
				width = toPixels(e.getAttributeValue("width"));
			}
		}

		if (e.getAttributeValue("weight") != null) {
			weight = Float.parseFloat(e.getAttributeValue("weight"));
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

		if (e.getAttributeValue("g") != null) {
			gravity = parseGravity(e.getAttributeValue("g"));
		}

		LayoutParams params = new LayoutParams(width, height, weight);
		params.leftMargin = leftMargin;
		params.topMargin = topMargin;
		params.bottomMargin = bottomMargin;
		params.rightMargin = rightMargin;
		params.gravity = gravity;
		return params;
	}

	@SuppressWarnings("rawtypes")
	static Class getResourceClass(Activity context) {
		String packageName = context.getApplication().getPackageName();
		try {
			return Class.forName(packageName + ".R");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	static Class getAndroidResourceClass(Activity context) {
		try {
			return Class.forName("android.R");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	static Class<?> getAndroidResourceComponentClass(Activity context,
			String name) {
		for (Class<?> subclass : getAndroidResourceClass(context)
				.getDeclaredClasses()) {
			if (subclass.getName().equals("android.R$" + name)) {
				return subclass;
			}
		}
		return null;
	}

	static Class<?> getResourceComponentClass(Activity context, String name) {
		String packageName = context.getApplication().getPackageName();
		for (Class<?> subclass : getResourceClass(context).getDeclaredClasses()) {
			if (subclass.getName().equals(packageName + ".R$" + name)) {
				return subclass;
			}
		}
		return null;
	}

	static Class<?> getStyleClass(Activity context) {
		if (ActivityBuilder.styleClass == null) {
			ActivityBuilder.styleClass = getResourceComponentClass(context,
					"style");
		}
		return styleClass;
	}

	public static Class<?> getIdClass(Activity context) {
		if (ActivityBuilder.idClass == null) {
			ActivityBuilder.idClass = getResourceComponentClass(context, "id");
		}
		return idClass;
	}

	Class<?> getDrawableClass(Activity context) {
		if (ActivityBuilder.drawableClass == null) {
			ActivityBuilder.drawableClass = getResourceComponentClass(context,
					"drawable");
		}
		return ActivityBuilder.drawableClass;
	}

	public int getStyleById(String styleId) {
		Field f;
		try {
			f = ActivityBuilder.getStyleClass(currentActivity)
					.getField(styleId);
			return f.getInt(ActivityBuilder.getStyleClass(currentActivity)
					.newInstance());
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static int getViewById(Activity context, String viewId) {
		Field f;
		try {
			f = getIdClass(context).getField(viewId);
			return f.getInt(getIdClass(context).newInstance());
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public int getDrawableId(String drawable) {
		try {
			Field f = this.getDrawableClass(currentActivity).getField(drawable);
			return f.getInt(this.getDrawableClass(currentActivity)
					.newInstance());
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public void handleIconDrawable(Element e, ImageButton child) {
		String src = e.getAttributeValue("background");
		if (src != null) {
			if (src.indexOf("@drawable:") != -1) {
				String drawable = src.substring(10);
				int resId = getDrawableId(drawable);
				if (resId != 0) {
					child.setImageResource(resId);
				}
			} else {
				UrlImageViewHelper.setUrlDrawable(child, normalizeUrl(src),
						"setBackgroundDrawable");
			}
		}
	}

	public static int toDeviceIndependentPixels(Context context, Integer px) {
		return Math.round(toDeviceIndependentPixels(context, (float) px));
	}

	public static float toDeviceIndependentPixels(Context context, Float px) {
		Resources r = context.getResources();
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px,
				r.getDisplayMetrics());
	}

	public static float toPixelsFromDip(Context context, Float px) {
		Resources r = context.getResources();
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px,
				r.getDisplayMetrics());
	}

	public static int toPixelsFromDip(Context context, int px) {
		return Math.round(toPixelsFromDip(context, (float) px));
	}

	public static int toPixels(Context context, String measurement) {
		int minWidth = 0;
		if (measurement.endsWith("dp") || measurement.endsWith("dip")) {
			int s = 2;
			if (measurement.endsWith("dip")) {
				s = 3;
			}
			minWidth = Math.round(ActivityBuilder.toDeviceIndependentPixels(
					context,
					Float.parseFloat(measurement.substring(0,
							measurement.length() - s))));
		} else {
			minWidth = Integer.parseInt(measurement);
		}
		return minWidth;
	}

	private int toPixels(String measurement) {
		return ActivityBuilder.toPixels(currentActivity, measurement);
	}

	@SuppressWarnings("unchecked")
	private void registerView(ViewGroup group, View child, Element e,
			ViewBuilder builder) {

		ViewExtras extras = new ViewExtras();

		int hash_code = Math.abs((int) (Math.random() * Integer.MAX_VALUE));
		child.setId(hash_code);

		if (e.getAttributeValue("id") != null) {
			String attr_name = e.getAttributeValue("id");
			extras.setView_id(attr_name);
			Log.d(this.getClass().toString(), attr_name + " = " + hash_code
					+ " stored.");
			namedViewDictionary.put(attr_name.hashCode(), hash_code);
		} else {
			extras.setView_id(Integer.toString(hash_code));
			namedViewDictionary.put(Integer.toString(hash_code).hashCode(),
					hash_code);
		}

		if (e.getAttributeValue("name") != null) {
			String name = e.getAttributeValue("name");
			extras.setView_name(name);
		}

		if (e.getAttributeValue("class") != null) {
			String class_name = e.getAttributeValue("class");
			for (String item : class_name.split(" ")) {
				ArrayList<Integer> list = classViewDictionary.get(item
						.hashCode());
				if (list == null) {
					list = new ArrayList<Integer>();
					classViewDictionary.put(item.hashCode(), list);
				}
				list.add(child.getId());
			}
			extras.setView_class(class_name);
		}

		ArrayList<Integer> list = tagViewDictionary.get(e.getName());
		if (list == null) {
			list = new ArrayList<Integer>();
			tagViewDictionary.put(e.getName(), list);
		}
		list.add(child.getId());

		for (Attribute attribute : e.getAttributes()) {

			String attribute_name = attribute.getName();
			String attribute_value = attribute.getValue();

			if (attribute_name.startsWith("data-")) {
				HashMap<String, String> dataAttributes = extras
						.getDataAttributes();
				dataAttributes
						.put(attribute_name.substring(5), attribute_value);
			}

		}

		extras.setPropertyMap(ViewBuilder.toPropertyMap(e));
		extras.setBuilder((Class<ViewBuilder>) builder.getClass());
		child.setTag(extras);
		// Log.d(this.getClass().toString(), "Adding "
		// + child.getClass().toString() + " to "
		// + group.getClass().toString());
		// RelativeLayout specific stuff
		group.addView(child);

		if (this.topView == null) {
			this.topView = group;
		}

	}

	public void applyStyle(View view, ArrayList<Object> resultBundle) {
		long css_start = System.currentTimeMillis();

		for (Object bundle : resultBundle) {
			if (bundle instanceof CssRules) {
				CssRules rules = (CssRules) bundle;
				rules.apply(this, this.currentActivity);
			}
		}
		long elapsed = System.currentTimeMillis() - css_start;

		Log.d(this.getClass().toString(), "apply css: elapsed time = "
				+ elapsed + "ms");
	}

	public void applyProperties(View view) {

		if (view != null) {
			Object tag = view.getTag();
			if (tag != null && tag instanceof ViewExtras) {
				ViewExtras viewExtras = (ViewExtras) tag;
				ViewBuilder builder = ViewBuilder.getBuilderForView(view,
						currentActivity, this);
				builder.setParamsFromProperty(view, viewExtras.getPropertyMap());
			}
		}
	}

	public void parse(Element element, ViewGroup view, ExecutionBundle bundle) {
		List<Element> elems = element.getChildren();
		for (Element e : elems) {

			String node_name = e.getName().toLowerCase(Locale.ENGLISH);
			ViewBuilder builder = null;

			if (node_name.equals("div") || node_name.equals("span")) {
				builder = new FrameLayoutBuilder();
			} else if (node_name.equals("layout")) {

				String type = "linear";

				if (e.getAttributeValue("type") != null) {
					type = e.getAttributeValue("type").toLowerCase(
							Locale.ENGLISH);
				}
				;

				if (type.equals("frame")) {
					builder = new FrameLayoutBuilder();
				} else if (type.equals("linear")) {
					builder = new LinearLayoutBuilder();
				} else if (type.equals("relative")) {
					builder = new RelativeLayoutBuilder();
				} else if (type.equals("scroll")) {
					builder = new ScrollViewBuilder();
				}
			} else if (node_name.equals("table")) {
				builder = new TableBuilder();
			} else if (node_name.equals("row")) {
				builder = new TableRowBuilder();
			} else if (node_name.equals("web")) {
				builder = new WebViewBuilder();
			} else if (node_name.equals("list")) {
				builder = new ListViewBuilder();
			} else if (node_name.equals("t")) {
				builder = new TextViewBuilder();
			} else if (node_name.equals("button")) {
				builder = new ButtonViewBuilder();
			} else if (node_name.equals("image_button")) {
				builder = new ImageButtonBuilder();
			} else if (node_name.equals("checkbox")) {
				builder = new CheckBoxBuilder();
			} else if (node_name.equals("input")
					|| node_name.equals("edit_text")) {
				builder = new EditTextBuilder();
			} else if (node_name.equals("img") || node_name.equals("image")) {
				builder = new ImageViewBuilder();
			} else if (node_name.equals("preload")) {
				continue;
			} else {
				builder = null;
			}

			View currentView = null;

			if (builder != null) {
				// build and add the view to its parent
				builder.setContext(currentActivity);
				builder.setBuilder(this);
				currentView = builder.build(e);
			} else {
				
				
				String module = e.getAttributeValue("module");
				if (module!=null) {
					node_name = module + "#" + node_name;
				}
				
				Log.d(this.getClass().toString(),"Module = " + module + " Custom View = " + node_name);
				
				IRubyObject framework = (IRubyObject) bundle.getContainer()
						.runScriptlet("$framework");
				Ruby runtime = bundle.getContainer().getProvider().getRuntime();
				IRubyObject wrapped_param1 = RubyString.newString(runtime,
						node_name);
				IRubyObject wrapped_param2 = JavaUtil.convertJavaToRuby(
						runtime, e);
				IRubyObject[] args = new IRubyObject[] { wrapped_param1,
						wrapped_param2 };
				IRubyObject result = framework.callMethod(
						runtime.getCurrentContext(), "resolve_view", args);

				if (result == null)
					continue;

				if (result.isNil())
					continue;

				if (result instanceof JavaObject) {
					currentView = (View) ((JavaObject) result).getValue();
				} else {
					continue;
				}
			}

			if (currentView != null) {
				registerView(view, currentView, e, builder);
				builder.setParams(currentView, e);
				// handle ViewGroups which can have subelements
				if (builder.hasSubElements()) {
					parse(e, (ViewGroup) currentView, bundle);
				}
			}

		}
	}
}
