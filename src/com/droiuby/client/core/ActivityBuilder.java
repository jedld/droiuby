package com.droiuby.client.core;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.JDOMParseException;
import org.jdom2.input.SAXBuilder;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.EvalFailedException;
import org.jruby.embed.ParseFailedException;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.droiuby.application.ActiveApp;
import com.droiuby.application.CanvasActivity;
import com.droiuby.callbacks.DocumentReadyListener;
import com.droiuby.callbacks.OnAppDownloadComplete;
import com.droiuby.client.core.builder.ButtonViewBuilder;
import com.droiuby.client.core.builder.CheckBoxBuilder;
import com.droiuby.client.core.builder.EditTextBuilder;
import com.droiuby.client.core.builder.FrameLayoutBuilder;
import com.droiuby.client.core.builder.ImageButtonBuilder;
import com.droiuby.client.core.builder.ImageViewBuilder;
import com.droiuby.client.core.builder.LinearLayoutBuilder;
import com.droiuby.client.core.builder.ListViewBuilder;
import com.droiuby.client.core.builder.RelativeLayoutBuilder;
import com.droiuby.client.core.builder.ScrollViewBuilder;
import com.droiuby.client.core.builder.TableBuilder;
import com.droiuby.client.core.builder.TableRowBuilder;
import com.droiuby.client.core.builder.TextViewBuilder;
import com.droiuby.client.core.builder.ViewBuilder;
import com.droiuby.client.core.builder.WebViewBuilder;
import com.droiuby.client.core.postprocessor.AssetPreloadParser;
import com.droiuby.client.core.postprocessor.CssPreloadParser;
import com.droiuby.client.core.postprocessor.GenericPostProcessor;
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
					if (sc.getName().equals("com.droiuby.client.R$id")) {
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		if (resolveCache.get(id) != null) {
			return resolveCache.get(id);
		}
		return null;

	}

}

class ActivityBootstrapper extends AsyncTask<Void, Void, ActivityBuilder> {

	ActiveApp app;
	Activity targetActivity;
	Document mainActivityDocument;
	ScriptingContainer scriptingContainer;
	String baseUrl;
	String controllerClass;
	String controller_attribute;
	String pageUrl;
	String errorMsg;
	int resId;

	ExecutionBundle executionBundle;

	DocumentReadyListener onReadyListener;
	private EmbedEvalUnit preParsedScript;
	ArrayList<Object> resultBundle;
	SAXBuilder sax = new SAXBuilder();
	int method;

	public ActivityBootstrapper(ExecutionBundle executionBundle, ActiveApp app,
			String pageUrl, int method, int resId, Activity targetActivity,
			Document cachedActivityDocument,
			DocumentReadyListener onReadyListener) {
		this.app = app;
		this.pageUrl = pageUrl;
		this.executionBundle = executionBundle;
		this.targetActivity = targetActivity;
		this.baseUrl = app.getBaseUrl();
		this.scriptingContainer = executionBundle.getContainer();
		this.mainActivityDocument = cachedActivityDocument;
		this.onReadyListener = onReadyListener;
		this.method = method;
		this.resId = resId;
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		targetActivity.setRequestedOrientation(app.getInitiallOrientation());
	}

	@Override
	protected ActivityBuilder doInBackground(Void... params) {
		String responseBody = (String) Utils.loadAppAsset(app, targetActivity,
				pageUrl, Utils.ASSET_TYPE_TEXT, method);
		if (responseBody != null) {
			// Log.d(this.getClass().toString(), responseBody);
		} else {
			Log.d(this.getClass().toString(), "response empty.");
			return null;
		}
		try {
			if (mainActivityDocument == null) {
				mainActivityDocument = sax
						.build(new StringReader(responseBody));
			}
		} catch (JDOMParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			responseBody = "<activity><t>" + e.getMessage() + "</t></activity>";
			try {
				mainActivityDocument = sax
						.build(new StringReader(responseBody));
			} catch (JDOMException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		controller_attribute = mainActivityDocument.getRootElement()
				.getAttributeValue("controller");
		String baseUrl = app.getBaseUrl();

		if (controller_attribute != null) {
			String csplit[] = org.apache.commons.lang3.StringUtils.split(
					controller_attribute, "#");
			if (csplit.length == 2) {

				if (!csplit[1].trim().equals("")) {
					controllerClass = csplit[1];
				}

				if (!csplit[0].trim().equals("")) {
					Log.d("Activity loader", "loading controller file "
							+ baseUrl + csplit[0]);
					String controller_content = (String) Utils.loadAppAsset(
							app, targetActivity, csplit[0],
							Utils.ASSET_TYPE_TEXT, Utils.HTTP_GET);

					long start = System.currentTimeMillis();
					try {
						preParsedScript = Utils.preParseRuby(
								scriptingContainer, controller_content,
								targetActivity);
					} catch (ParseFailedException e) {
						e.printStackTrace();
						executionBundle.addError(e.getMessage());
					}
					long elapsed = System.currentTimeMillis() - start;
					Log.d(this.getClass().toString(),
							"controller preparse: elapsed time = " + elapsed
									+ "ms");
				}
			} else {
				controllerClass = csplit[0];
			}
		}

		ActivityBuilder builder = new ActivityBuilder(mainActivityDocument,
				targetActivity, baseUrl, resId);
		executionBundle.getPayload().setActivityBuilder(builder);
		executionBundle.getPayload().setExecutionBundle(executionBundle);
		executionBundle.getPayload().setActiveApp(app);
		executionBundle.setCurrentUrl(pageUrl);

		scriptingContainer.put("$container_payload",
				executionBundle.getPayload());

		scriptingContainer.runScriptlet("$framework.before_activity_setup");

		resultBundle = builder.preload(executionBundle);
		System.gc();
		return builder;
	}

	@Override
	protected void onPostExecute(ActivityBuilder result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		if (result != null) {
			long start = System.currentTimeMillis();
			View view = result.build();

			if (onReadyListener != null) {
				onReadyListener.onDocumentReady(mainActivityDocument);
			}

			result.applyStyle(view, resultBundle);

			long elapsed = System.currentTimeMillis() - start;
			Log.d(this.getClass().toString(), "build activity: elapsed time = "
					+ elapsed + "ms");

			try {
				if (preParsedScript != null) {
					start = System.currentTimeMillis();
					preParsedScript.run();
				}

				scriptingContainer.runScriptlet("$framework.preload");

				if (preParsedScript != null) {
					Log.d(this.getClass().toString(), "class = "
							+ controllerClass);
					IRubyObject instance;
					instance = (IRubyObject) scriptingContainer
							.runScriptlet("$framework.script('"
									+ controllerClass + "')");
					executionBundle.setCurrentController(instance);
					elapsed = System.currentTimeMillis() - start;
					Log.d(this.getClass().toString(),
							"controller on_create(): elapsed time = " + elapsed
									+ "ms");
				}
			} catch (EvalFailedException e) {
				e.printStackTrace();
				executionBundle.addError(e.getMessage());
				Log.e(this.getClass().toString(), e.getMessage());
			} catch (RuntimeException e) {
				e.printStackTrace();
				executionBundle.addError(e.getMessage());
				Log.e(this.getClass().toString(), e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				executionBundle.addError(e.getMessage());
				Log.e(this.getClass().toString(), e.getMessage());
			}

		}
	}
}

public class ActivityBuilder {

	public static final int PARTIAL_REPLACE = 1;
	public static final int PARTIAL_REPLACE_CHILDREN = 2;
	public static final int PARTIAL_APPEND_CHILDREN = 3;

	Activity context;
	Element rootElement;
	View topView;
	String baseUrl;

	static Class idClass;
	static Class drawableClass;
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
		return context.findViewById(this.getViewById(context, "mainLayout"));
	}

	ViewGroup target;
	HashMap<String, Object> preloadedResource = new HashMap<String, Object>();
	HashMap<String, Integer> namedViewDictionary = new HashMap<String, Integer>();
	HashMap<String, ArrayList<Integer>> classViewDictionary = new HashMap<String, ArrayList<Integer>>();
	HashMap<String, ArrayList<Integer>> tagViewDictionary = new HashMap<String, ArrayList<Integer>>();

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

	public ActivityBuilder(Document document, Activity context, String baseUrl,
			int resId) {
		setup(document, context, baseUrl,
				(ViewGroup) context.findViewById(resId));
	}

	public ActivityBuilder(Document document, Activity context, String baseUrl,
			ViewGroup target) {
		setup(document, context, baseUrl, target);
	}

	private void setup(Document document, Activity context, String baseUrl,
			ViewGroup target) {
		this.target = target;
		this.context = context;
		this.rootElement = document.getRootElement();
		this.baseUrl = baseUrl;
	}

	public ArrayList<Object> preload(ExecutionBundle bundle) {
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
			} else {
				asset_type = Utils.ASSET_TYPE_BINARY;
				parser = new GenericPostProcessor();
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
			// TODO Auto-generated catch block
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

	// public static void loadApp(Context c, String applicationUrl,
	// OnAppDownloadComplete onDownloadComplete) {
	// AppDownloader downloader = new AppDownloader(c, applicationUrl,
	// CanvasActivity.class, onDownloadComplete);
	// downloader.execute();
	// }

	public static void loadApp(Context c, String applicationUrl) {
		AppDownloader downloader = new AppDownloader(c, applicationUrl,
				CanvasActivity.class);
		downloader.execute();
	}

	public static void loadLayout(ExecutionBundle executionBundle,
			ActiveApp app, String pageUrl, boolean newActivity, int method,
			Activity targetActivity, Document cachedDocument,
			DocumentReadyListener onReadyListener, int resId) {
		if (newActivity) {
			Intent intent = new Intent(targetActivity,
					targetActivity.getClass());
			intent.putExtra("application", app);
			intent.putExtra("method", method);
			intent.putExtra("startUrl", pageUrl);
			Log.d("LOADLAYOUT", "-> new Activity Page URL = " + pageUrl);
			targetActivity.startActivity(intent);
		} else {
			Log.d("LOADLAYOUT", "page URL = " + pageUrl);
			ActivityBootstrapper bootstrapper = new ActivityBootstrapper(
					executionBundle, app, pageUrl, method, resId,
					targetActivity, null, onReadyListener);
			bootstrapper.execute();
		}
	}

	public View build() {
		target.removeAllViews();
		try {
			parse(rootElement, target);
		} catch (Exception e) {
			TextView view = new TextView(context);
			view.setText(e.getMessage());
			view.setTextColor(Color.RED);
			target.addView(view);
		}
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

	public Object findViewByName(String selector) {
		selector = selector.trim();
		if (selector.startsWith("#")) {
			String name = selector.substring(1);
			if (namedViewDictionary.containsKey(name)) {
				int id = namedViewDictionary.get(name);
				View view = context.findViewById(id);
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
			return context.findViewById(id);
		} else if (selector.startsWith("@preload:")) {
			String name = selector.substring(9);
			return preloadedResource.get(name);
		} else if (selector.startsWith(".")) {
			String name = selector.substring(1);
			ArrayList<View> object_list = new ArrayList<View>();
			if (classViewDictionary.containsKey(name)) {
				ArrayList<Integer> list = classViewDictionary.get(name);
				for (int id : list) {
					object_list.add(context.findViewById(id));
				}
			}
			return object_list;
		} else if (selector.startsWith("^")) {
			String name = selector.substring(1);
			int id = getViewById(context, name);
			if (id != 0) {
				return context.findViewById(id);
			} else {
				return null;
			}
		} else if (selector.startsWith("+")) {
			String name = selector.substring(1);
			int id = getDrawableId(name);
			if (id != 0) {
				return context.findViewById(id);
			} else {
				try {
					return context.findViewById(Integer.parseInt(name));
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
					object_list.add(context.findViewById(id));
				}
			}
			return object_list;
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

	public String reverseLookupId(int id) {
		return ReverseIdResolver.getInstance(context).resolve(id);
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

	static Class getResourceClass(Activity context) {
		String packageName = context.getApplication().getPackageName();
		try {
			return Class.forName(packageName + ".R");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	static Class<?> getResourceComponenetClass(Activity context, String name) {
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
			ActivityBuilder.styleClass = getResourceComponenetClass(context,
					"style");
		}
		return styleClass;
	}

	public static Class<?> getIdClass(Activity context) {
		if (ActivityBuilder.idClass == null) {
			ActivityBuilder.idClass = getResourceComponenetClass(context, "id");
		}
		return idClass;
	}

	Class<?> getDrawableClass(Activity context) {
		if (ActivityBuilder.drawableClass == null) {
			ActivityBuilder.drawableClass = getResourceComponenetClass(context,
					"drawable");
		}
		return ActivityBuilder.drawableClass;
	}

	public int getStyleById(String styleId) {
		Field f;
		try {
			f = this.getStyleClass(context).getField(styleId);
			return f.getInt(this.getStyleClass(context).newInstance());
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	public int getDrawableId(String drawable) {
		try {
			Field f = this.getDrawableClass(context).getField(drawable);
			return f.getInt(this.getDrawableClass(context).newInstance());
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
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
			namedViewDictionary.put(attr_name, hash_code);
		} else {
			extras.setView_id(Integer.toString(hash_code));
			namedViewDictionary.put(Integer.toString(hash_code), hash_code);
		}

		if (e.getAttributeValue("name") != null) {
			String name = e.getAttributeValue("name");
			extras.setView_name(name);
		}

		if (e.getAttributeValue("class") != null) {
			String class_name = e.getAttributeValue("class");
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
		extras.setBuilder(builder.getClass());
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
				rules.apply(this, this.context);
			}
		}
		long elapsed = System.currentTimeMillis() - css_start;

		Log.d(this.getClass().toString(), "apply css: elapsed time = "
				+ elapsed + "ms");
	}

	void applyProperties(View view) {

		if (view != null) {
			Object tag = view.getTag();
			if (tag != null && tag instanceof ViewExtras) {
				ViewExtras viewExtras = (ViewExtras) tag;
				ViewBuilder builder = ViewBuilder.getBuilderForView(view,
						context, this);
				builder.setParamsFromProperty(view, viewExtras.getPropertyMap());
			}
			//
			// if (view instanceof ViewGroup) {
			// ViewGroup viewGroup = (ViewGroup) view;
			// for (int i = 0; i < viewGroup.getChildCount(); i++) {
			// View child = viewGroup.getChildAt(i);
			// applyProperties(child);
			// }
			// }
		}
	}

	public void parse(Element element, ViewGroup view) {
		List<Element> elems = element.getChildren();
		for (Element e : elems) {

			String node_name = e.getName().toLowerCase();
			ViewBuilder builder = null;

			if (node_name.equals("div") || node_name.equals("span")) {
				builder = new FrameLayoutBuilder();
			} else if (node_name.equals("layout")) {
				String type = e.getAttributeValue("type").toLowerCase();
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
			} else if (node_name.equals("input")) {
				builder = new EditTextBuilder();
			} else if (node_name.equals("img")) {
				builder = new ImageViewBuilder();
			}

			if (builder != null) {

				// build and add the view to its parent
				builder.setContext(context);
				builder.setBuilder(this);
				View currentView = builder.build(e);
				registerView(view, currentView, e, builder);
				builder.setParams(currentView, e);
				// handle ViewGroups which can have subelements
				if (builder.hasSubElements()) {
					parse(e, (ViewGroup) currentView);
				}
			}
		}
	}
}
