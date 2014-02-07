package com.droiuby.client.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.JDOMParseException;
import org.jdom2.input.SAXBuilder;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.EvalFailedException;
import org.jruby.embed.ParseFailedException;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.droiuby.application.DroiubyApp;
import com.droiuby.application.DroiubyBootstrap;
import com.droiuby.client.core.builder.ActivityBuilder;
import com.droiuby.client.core.console.WebConsole;
import com.droiuby.client.core.listeners.OnPageRefreshListener;
import com.droiuby.client.core.postprocessor.CssPreloadParser;
import com.droiuby.client.core.postprocessor.ScriptPreparser;
import com.droiuby.client.core.utils.ActiveAppDownloader;
import com.droiuby.client.core.utils.OnWebConsoleReady;
import com.droiuby.client.core.utils.Utils;

class PageRefreshTask extends AsyncTask<Void,Void,PageAsset> {

	Activity currentActivity;
	ExecutionBundle bundle;
	OnPageRefreshListener listener;
	
	int refreshType;
	
	public static final int FULL_ACTIVITY_REFRESH = 0;
	public static final int QUICK_ACTIVITY_REFRESH = 1;
	
	
	public PageRefreshTask(Activity currentActivity, ExecutionBundle bundle, OnPageRefreshListener listener, int refreshType) {
		this.currentActivity = currentActivity;
		this.bundle = bundle;
		this.refreshType = refreshType;
		this.listener = listener;
	}
	
	@Override
	protected PageAsset doInBackground(Void... params) {
		DroiubyApp application = bundle.getPayload().getActiveApp();
		DroiubyLauncher.downloadAssets(currentActivity, bundle.getPayload().getActiveApp(), bundle);
		PageAsset page = DroiubyLauncher.loadPage(currentActivity, bundle, application.getMainUrl(),
				Utils.HTTP_GET);
		return page;
	}

	@Override
	protected void onPostExecute(PageAsset result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		if (refreshType == PageRefreshTask.FULL_ACTIVITY_REFRESH) {
			currentActivity.finish();
			DroiubyLauncher.startNewActivity(currentActivity, result);
		} else {
			DroiubyLauncher.runController(currentActivity, bundle, result);
		}
		if (listener!=null) {
			listener.onRefreshComplete(result);
		}
	}
}

public class DroiubyLauncher extends AsyncTask<Void, Void, PageAsset> {

	Context context;
	String url;
	Class activityClass;

	protected DroiubyLauncher(Context context, String url, Class activityClass) {
		this.context = context;
		this.url = url;
		this.activityClass = activityClass;
	}

	public static void launch(Context context, String url) {
		launch(context, url, null);
	}

	public static void launch(Context context, String url, Class activityClass) {
		try {

			if (activityClass == null) {
				activityClass = getDefaultActivityClass(context);
			}

			DroiubyLauncher launcher = new DroiubyLauncher(context, url,
					activityClass);
			launcher.execute();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

	}

	public static Class getDefaultActivityClass(Context context)
			throws ClassNotFoundException {
		Class activityClass;
		String packageName = context.getApplicationContext().getPackageName();
		activityClass = Class.forName(packageName + ".DroiubyActivity");
		return activityClass;
	}

	private DroiubyApp download(String url) {
		String responseBody = null;
		Log.d(ActiveAppDownloader.class.toString(), "loading " + url);
		if (url.startsWith("file://") || url.indexOf("asset:") != -1) {
			responseBody = Utils.loadAsset(context, url);
		} else {
			responseBody = Utils.query(url, context, null);
		}

		if (responseBody != null) {
			Log.d(ActiveAppDownloader.class.toString(), responseBody);
			SAXBuilder sax = new SAXBuilder();
			Document doc;
			try {
				doc = sax.build(new StringReader(responseBody));
				Element rootElem = doc.getRootElement();
				String appName = rootElem.getChild("name").getText();
				String appDescription = rootElem.getChild("description")
						.getText();
				String baseUrl = rootElem.getChildText("base_url");
				String mainActivity = rootElem.getChildText("main");
				String framework = rootElem.getChildText("framework");
				if (baseUrl == null || baseUrl.equals("")) {
					if (url.startsWith("file://")) {
						baseUrl = extractBasePath(url);
					} else {
						URL aURL = new URL(url);
						String adjusted_path = aURL.getPath();
						adjusted_path = extractBasePath(adjusted_path);

						baseUrl = aURL.getProtocol() + "://" + aURL.getHost()
								+ ":" + aURL.getPort() + adjusted_path;
					}
				}

				DroiubyApp app = new DroiubyApp();
				app.setDescription(appDescription);
				app.setName(appName);
				app.setBaseUrl(baseUrl);
				app.setMainUrl(mainActivity);
				app.setFramework(framework);
				app.setLaunchUrl(url);
				app.setInitiallOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

				String orientation = rootElem.getChildText("orientation");
				if (orientation != null) {
					if (orientation.equals("landscape")) {
						app.setInitiallOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					} else if (orientation.equals("portrait")
							|| orientation.equals("vertical")) {
						app.setInitiallOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					} else if (orientation.equals("sensor_landscape")) {
						app.setInitiallOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
					} else if (orientation.equals("sensor_portrait")) {
						app.setInitiallOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
					} else if (orientation.equals("auto")) {
						app.setInitiallOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
					} else {
						app.setInitiallOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
					}
				}
				if (rootElem.getChild("assets") != null) {
					List<Element> assets = rootElem.getChild("assets")
							.getChildren("resource");
					for (Element asset : assets) {
						String asset_name = asset.getAttributeValue("name");
						String asset_type = asset.getAttributeValue("type");
						Log.d("APP DOWNLOADER", "loading asset " + asset_name
								+ ".");
						int type_int = DroiubyApp.ASSET_TYPE_SCRIPT;
						if (asset_type.equals("script")) {
							type_int = DroiubyApp.ASSET_TYPE_SCRIPT;
						} else if (asset_type.equals("css")) {
							type_int = DroiubyApp.ASSET_TYPE_CSS;
						} else if (asset_type.equals("lib")) {
							type_int = DroiubyApp.ASSET_TYPE_LIB;
						} else if (asset_type.equals("font")
								|| asset_type.equals("typeface")) {
							type_int = DroiubyApp.ASSET_TYPE_TYPEFACE;
						} else if (asset_type.equals("binary")
								|| asset_type.equals("file")) {
							type_int = DroiubyApp.ASSET_TYPE_BINARY;
						} else if (asset_type.equals("vendor")) {
							type_int = DroiubyApp.ASSET_TYPE_VENDOR;
						}

						app.addAsset(asset_name, type_int);
					}
				}

				return app;
			} catch (JDOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return null;
	}

	@Override
	protected PageAsset doInBackground(Void... params) {
		DroiubyApp application = download(url);
		ExecutionBundleFactory factory = ExecutionBundleFactory
				.getInstance(DroiubyBootstrap.classLoader);
		ExecutionBundle executionBundle = factory.getNewScriptingContainer(
				context, application.getBaseUrl());

		executionBundle.getPayload().setDroiubyApp(application);
		downloadAssets(context, application, executionBundle);
		return loadPage(context, executionBundle, application.getMainUrl(),
				Utils.HTTP_GET);
	}

	@Override
	protected void onPostExecute(PageAsset result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);

		startNewActivity(context, activityClass, result);

		if (context instanceof Activity) {
			((Activity) context).finish();
		}

	}

	public static void startNewActivity(Context context, PageAsset result) {
		try {
			startNewActivity(context,
					DroiubyLauncher.getDefaultActivityClass(context), result);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void startNewActivity(Context context, Class activityClass,
			PageAsset result) {

		Intent intent = new Intent(context, activityClass);
		intent.putExtra("bundle", result.getBundle().getName());
		intent.putExtra("pageUrl", result.getUrl());
		context.startActivity(intent);
	}

	public static PageAsset loadPage(Context context, ExecutionBundle bundle,
			String pageUrl, int method) {
		PageAsset page = new PageAsset();
		DroiubyApp app = bundle.getPayload().getActiveApp();
		page.setBundle(bundle);
		page.setUrl(pageUrl);
		bundle.addPageAsset(pageUrl, page);

		String responseBody;
		SAXBuilder sax = new SAXBuilder();
		Document mainActivityDocument = null;

		ScriptingContainer scriptingContainer = bundle.getContainer();
		try {
			responseBody = (String) Utils.loadAppAsset(app, context, pageUrl,
					Utils.ASSET_TYPE_TEXT, method);

			if (responseBody == null) {
				responseBody = "<activity><t>Problem loading url " + pageUrl
						+ "</t></activity>";
			}

			if (mainActivityDocument == null) {
				mainActivityDocument = sax
						.build(new StringReader(responseBody));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			responseBody = "<activity><t>Unable to open file " + pageUrl
					+ "</t></activity>";
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

		String controller_attribute = mainActivityDocument.getRootElement()
				.getAttributeValue("controller");
		String controllerClass = null;
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
							app, context, csplit[0], Utils.ASSET_TYPE_TEXT,
							Utils.HTTP_GET);

					long start = System.currentTimeMillis();
					try {
						page.setPreParsedScript(Utils.preParseRuby(
								scriptingContainer, controller_content));
					} catch (ParseFailedException e) {
						e.printStackTrace();
						bundle.addError(e.getMessage());
					}
					long elapsed = System.currentTimeMillis() - start;
					Log.d(DroiubyLauncher.class.toString(),
							"controller preparse: elapsed time = " + elapsed
									+ "ms");
				}
			} else {
				controllerClass = csplit[0];
			}
		}

		page.setControllerClass(controllerClass);

		ActivityBuilder builder = new ActivityBuilder(mainActivityDocument,
				null, baseUrl);

		page.setBuilder(builder);

		scriptingContainer.put("$container_payload", bundle.getPayload());
		try {
			scriptingContainer.runScriptlet("$framework.before_activity_setup");
		} catch (Exception e) {
			e.printStackTrace();
		}

		ArrayList<Object> resultBundle = builder.preload(context, bundle);

		page.setAssets(resultBundle);

		return page;
	}

	public static Boolean downloadAssets(Context context, DroiubyApp app,
			ExecutionBundle executionBundle) {

		ScriptingContainer scriptingContainer = executionBundle.getPayload()
				.getContainer();
		if (!executionBundle.isLibraryInitialized()) {
			Log.d(DroiubyLauncher.class.toString(), "initializing framework");
			scriptingContainer.runScriptlet("require '" + app.getFramework()
					+ "/" + app.getFramework() + "'");
			executionBundle.setLibraryInitialized(true);

			ArrayList<Object> resultBundle = new ArrayList<Object>();
			HashMap<String, Integer> asset_map = app.getAssets();
			if (app.getAssets().size() > 0) {
				ExecutorService thread_pool = Executors
						.newFixedThreadPool(Runtime.getRuntime()
								.availableProcessors() + 1);
				for (String asset_name : app.getAssets().keySet()) {
					int asset_type = asset_map.get(asset_name);
					int download_type = Utils.ASSET_TYPE_TEXT;

					AssetDownloadCompleteListener listener = null;
					if (asset_type == DroiubyApp.ASSET_TYPE_SCRIPT) {
						listener = new ScriptPreparser();
					} else if (asset_type == DroiubyApp.ASSET_TYPE_CSS) {
						listener = new CssPreloadParser();
					} else if (asset_type == DroiubyApp.ASSET_TYPE_VENDOR) {
						List<String> loadPaths = new ArrayList<String>();
						String path = Utils.stripProtocol(app.getBaseUrl())
								+ asset_name;
						File fpath = new File(path);
						if (fpath.exists() && fpath.isDirectory()) {
							for (File file : fpath.listFiles()) {
								if (file.isDirectory()) {
									String vendorPath;
									try {
										vendorPath = file.getCanonicalPath()
												+ File.separator + "lib";
										File libDir = new File(vendorPath);
										if (libDir.exists()) {
											Log.d(DroiubyLauncher.class.toString(),
													"Adding vendor path "
															+ vendorPath);
											loadPaths.add(vendorPath);
										}
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						}

						scriptingContainer.getProvider().getRuntime()
								.getLoadService().addPaths(loadPaths);
					} else if (asset_type == DroiubyApp.ASSET_TYPE_LIB) {
						List<String> loadPaths = new ArrayList<String>();
						String path = Utils.stripProtocol(app.getBaseUrl())
								+ asset_name;
						Log.d(DroiubyLauncher.class.toString(), "examine lib at "
								+ path);
						File fpath = new File(path);
						if (fpath.isDirectory()) {
							Log.d(DroiubyLauncher.class.toString(), "Adding " + path
									+ " to load paths.");
							loadPaths.add(path);
							scriptingContainer.getProvider().getRuntime()
									.getLoadService().addPaths(loadPaths);
						}
						continue;
					} else if (asset_type == DroiubyApp.ASSET_TYPE_BINARY) {
						download_type = Utils.ASSET_TYPE_BINARY;
					} else if (asset_type == DroiubyApp.ASSET_TYPE_TYPEFACE) {
						download_type = Utils.ASSET_TYPE_TYPEFACE;
					}

					Log.d(DroiubyLauncher.class.toString(), "downloading "
							+ asset_name + " ...");
					AssetDownloadWorker worker = new AssetDownloadWorker(
							context, app, executionBundle, asset_name,
							download_type, resultBundle, listener,
							Utils.HTTP_GET);
					thread_pool.execute(worker);
				}
				thread_pool.shutdown();
				try {
					thread_pool.awaitTermination(240, TimeUnit.SECONDS);

					for (Object elem : resultBundle) {
						Log.d(DroiubyLauncher.class.toString(), "executing asset");
						if (elem instanceof EmbedEvalUnit) {
							((EmbedEvalUnit) elem).run();
						}
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	private static String extractBasePath(String adjusted_path) {
		int pos = 0;

		while ((adjusted_path.indexOf("/", pos)) != -1) {
			pos = adjusted_path.indexOf("/", pos) + 1;
		}
		adjusted_path = adjusted_path.substring(0, pos);
		return adjusted_path;
	}

	public static void runController(Activity activity, ExecutionBundle bundle,
			PageAsset page) {
		bundle.getPayload().setCurrentActivity(activity);
		bundle.getPayload().setCurrentPage(page);

		EmbedEvalUnit preParsedScript = page.getPreParsedScript();
		ScriptingContainer scriptingContainer = bundle.getContainer();
		long start = System.currentTimeMillis();
		try {
			if (preParsedScript != null) {
				preParsedScript.run();
			}

			scriptingContainer.runScriptlet("$framework.preload");
			if (preParsedScript != null) {
				Log.d(DroiubyLauncher.class.toString(),
						"class = " + page.getControllerClass());
				IRubyObject instance;
				instance = (IRubyObject) scriptingContainer
						.runScriptlet("$framework.script('"
								+ page.getControllerClass() + "')");
				bundle.setCurrentController(instance);
			}
		} catch (EvalFailedException e) {
			e.printStackTrace();
			bundle.addError(e.getMessage());
			Log.e(DroiubyLauncher.class.toString(), e.getMessage());
		} catch (RuntimeException e) {
			e.printStackTrace();
			bundle.addError(e.getMessage());
			Log.e(DroiubyLauncher.class.toString(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			bundle.addError(e.getMessage());
			Log.e(DroiubyLauncher.class.toString(), e.getMessage());
		}
		long elapsed = System.currentTimeMillis() - start;
		Log.d(DroiubyLauncher.class.toString(),
				"run Controller: elapsed time = " + elapsed + "ms");	
	}
	
	public static void refresh(Activity currentActivity, ExecutionBundle bundle, OnPageRefreshListener listener) {
		PageRefreshTask refreshTask = new PageRefreshTask(currentActivity, bundle,  listener, PageRefreshTask.QUICK_ACTIVITY_REFRESH);
		refreshTask.execute();
	}
	
	public static void runController(Activity activity, String bundleName,
			String pageUrl) {
		ExecutionBundle bundle = ExecutionBundleFactory.getBundle(bundleName);
		PageAsset page = bundle.getPage(pageUrl);
		runController(activity, bundle, page);
	}

	public static void setPage(Activity activity, String bundleName,
			String pageUrl) {
		ExecutionBundle bundle = ExecutionBundleFactory.getBundle(bundleName);
		PageAsset page = bundle.getPage(pageUrl);
		setPage(activity, bundle, page);
	}

	public static void setPage(Activity activity, ExecutionBundle bundle,
			PageAsset page) {
		long start = System.currentTimeMillis();

		bundle.getPayload().setExecutionBundle(bundle);
		bundle.getPayload().setCurrentPage(page);
		bundle.getPayload().setActivityBuilder(page.getBuilder());
		
		bundle.setCurrentUrl(page.getUrl());

		Log.d(DroiubyLauncher.class.toString(),
				"parsing and preparing views....");

		ActivityBuilder builder = page.getBuilder();
		builder.setCurrentActivity(activity);

		View preparedViews = builder.prepare(bundle);

		long elapsed = System.currentTimeMillis() - start;
		Log.d(DroiubyLauncher.class.toString(),
				"prepare activity: elapsed time = " + elapsed + "ms");
		View view = builder.setPreparedView(preparedViews);
		// apply CSS
		builder.applyStyle(view, page.getAssets());
		Log.d(DroiubyLauncher.class.toString(),
				"build activity: elapsed time = " + elapsed + "ms");
	}

	public static void setupConsole(ExecutionBundle executionBundle,
			OnWebConsoleReady listener) {
		String web_public_loc;
		Log.d(DroiubyLauncher.class.toString(), "Loading WebConsole...");
		try {
			web_public_loc = executionBundle.getPayload().getCurrentActivity()
					.getCacheDir().getCanonicalPath()
					+ "/www";
			File webroot = new File(web_public_loc);
			webroot.mkdirs();
			WebConsole console = WebConsole
					.getInstance(4000, webroot, listener);
			console.setBundle(executionBundle);
			console.setActivity(executionBundle.getPayload()
					.getCurrentActivity());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
