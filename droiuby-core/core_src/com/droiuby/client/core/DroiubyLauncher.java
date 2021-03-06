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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.JDOMParseException;
import org.jdom2.input.SAXBuilder;
import org.jruby.Ruby;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.EvalFailedException;
import org.jruby.embed.ParseFailedException;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.droiuby.application.bootstrap.DroiubyApp;
import com.droiuby.application.bootstrap.DroiubyBootstrap;
import com.droiuby.client.core.builder.ActivityBuilder;
import com.droiuby.client.core.console.WebConsole;
import com.droiuby.client.core.listeners.OnPageRefreshListener;
import com.droiuby.client.core.postprocessor.CssPreloadParser;
import com.droiuby.client.core.postprocessor.ScriptPreparser;
import com.droiuby.client.core.utils.OnWebConsoleReady;
import com.droiuby.client.core.utils.Utils;
import com.droiuby.launcher.Options;

class PageRefreshTask extends AsyncTask<Void, Void, PageAsset> {

	Activity currentActivity;
	ExecutionBundle bundle;
	OnPageRefreshListener listener;

	int refreshType;

	public static final int FULL_ACTIVITY_REFRESH = 0;
	public static final int QUICK_ACTIVITY_REFRESH = 1;

	public PageRefreshTask(Activity currentActivity, ExecutionBundle bundle,
			OnPageRefreshListener listener, int refreshType) {
		this.currentActivity = currentActivity;
		this.bundle = bundle;
		this.refreshType = refreshType;
		this.listener = listener;
	}

	@Override
	protected PageAsset doInBackground(Void... params) {
		DroiubyApp application = bundle.getPayload().getActiveApp();
		DroiubyLauncher.downloadAssets(currentActivity, bundle.getPayload()
				.getActiveApp(), bundle);
		PageAsset page = DroiubyLauncher.loadPage(currentActivity, bundle,
				application.getMainUrl(), Utils.HTTP_GET);
		return page;
	}

	@Override
	protected void onPostExecute(PageAsset result) {
		super.onPostExecute(result);
		if (refreshType == PageRefreshTask.FULL_ACTIVITY_REFRESH) {
			currentActivity.finish();
			DroiubyLauncher.startNewActivity(currentActivity, result);
		} else {
			DroiubyLauncher
					.runController(currentActivity, bundle, result, true);
		}
		if (listener != null) {
			listener.onRefreshComplete(result);
		}
	}
}

public class DroiubyLauncher extends AsyncTask<Void, Void, PageAsset> {

	Context context;
	String url;
	Class<?> activityClass;
	Options options;

	protected DroiubyLauncher(Context context, String url,
			Class<?> activityClass, Options options) {
		this.context = context;
		this.url = url;
		this.activityClass = activityClass;
		this.options = options;
	}

	public static void launch(Context context, String url, Options options) {
		launch(context, url, null, options);
	}

	public static void launch(Context context, String url) {
		launch(context, url, null, new Options());
	}

	public static void launch(Context context, String url,
			Class<?> activityClass, Options options) {
		try {

			if (activityClass == null) {
				activityClass = getDefaultActivityClass(context);
			}

			DroiubyLauncher launcher = new DroiubyLauncher(context, url,
					activityClass, options);
			launcher.execute();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return;
		}

	}

	public static Class<?> getDefaultActivityClass(Context context)
			throws ClassNotFoundException {
		Class<?> activityClass;
		String packageName = context.getApplicationContext().getPackageName();
		activityClass = Class.forName(packageName
				+ ".activities.DroiubyActivity");
		return activityClass;
	}

	private DroiubyApp download(String url) throws FileNotFoundException,
			IOException {
		String responseBody = null;
		Log.d(DroiubyLauncher.class.toString(), "loading " + url);

		String extraction_path;

		DroiubyApp app = new DroiubyApp();

		if (url.startsWith("asset:") && url.endsWith(".zip")) {
			String asset_path = url.substring(6);
			extraction_path = Utils.processArchive(context, url, context
					.getAssets().open(asset_path), options.isOverwrite());
			url = "file://" + extraction_path + File.separator
					+ "config.droiuby";
		} else {
			String extraction_target = Utils.getAppExtractionTarget(url,
					context);
			File dir = new File(extraction_target);
			if (dir.exists()) {
				Log.d(Utils.class.toString(), "removing existing directory.");
				FileUtils.deleteDirectory(dir);
			}
			dir.mkdirs();
			app.setWorkingDirectory(extraction_target);
		}

		if (url.startsWith("file://") || url.indexOf("asset:") != -1) {
			responseBody = Utils.loadAsset(context, url);
		} else {
			responseBody = Utils.query(url, context, null);
		}

		if (responseBody != null) {
			Log.d(DroiubyLauncher.class.toString(), responseBody);
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
						app.setWorkingDirectory(Utils.stripProtocol(baseUrl));
					} else {
						URL aURL = new URL(url);
						String adjusted_path = aURL.getPath();
						adjusted_path = extractBasePath(adjusted_path);

						baseUrl = aURL.getProtocol() + "://" + aURL.getHost()
								+ ":" + aURL.getPort() + adjusted_path;
					}
				}

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
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	@Override
	protected PageAsset doInBackground(Void... params) {
		DroiubyApp application = null;
		try {
			application = download(url);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (application != null) {

			ExecutionBundleFactory factory = ExecutionBundleFactory
					.getInstance(DroiubyBootstrap.classLoader);
			ExecutionBundle executionBundle = factory.getNewScriptingContainer(
					context, application.getBaseUrl(), options.isNewRuntime());

			if (options.isRootBundle()) {
				Log.d(this.getClass().toString(),
						"Setting instance as root bundle");
				executionBundle.setRootBundle(options.isRootBundle());
			}

			if (options.getConsole() != null) {
				options.getConsole().setBundle(executionBundle);
			}

			executionBundle.getPayload().setDroiubyApp(application);
			addPath(application.getWorkingDirectory(), executionBundle);

			downloadAssets(context, application, executionBundle);
			return loadPage(context, executionBundle, application.getMainUrl(),
					Utils.HTTP_GET);
		}
		return null;
	}

	private void addPath(String path, ExecutionBundle executionBundle) {
		Log.d(this.getClass().toString(), "Adding " + path + " to load path");

		List<String> loadPaths = new ArrayList<String>();
		loadPaths.add(path);
		Ruby runtime = executionBundle.getPayload().getContainer()
				.getProvider().getRuntime();
		runtime.getLoadService().addPaths(loadPaths);
	}

	@Override
	protected void onPostExecute(PageAsset result) {
		super.onPostExecute(result);
		if (result == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage("Unable to download access app at " + url)
					.setCancelable(true).create();
		} else {
			if (options.isNewActivity()) {
				startNewActivity(context, activityClass, result);
			}

			if (options.isCloseParentActivity() && context instanceof Activity) {
				((Activity) context).finish();
			}
		}

	}

	public static void startNewActivity(Context context, PageAsset result) {
		try {
			startNewActivity(context,
					DroiubyLauncher.getDefaultActivityClass(context), result);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void startNewActivity(Context context,
			Class<?> activityClass, PageAsset result) {

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

		String controllerIdentifier = null;
		String controllerClass = null;
		String baseUrl = app.getBaseUrl();

		ScriptingContainer scriptingContainer = bundle.getContainer();

		if (pageUrl.endsWith(".xml")) {
			SAXBuilder sax = new SAXBuilder();
			Document mainActivityDocument = null;
			try {
				responseBody = (String) Utils.loadAppAsset(app, context,
						pageUrl, Utils.ASSET_TYPE_TEXT, method);

				if (responseBody == null) {
					responseBody = "<activity><t>Problem loading url "
							+ pageUrl + "</t></activity>";
				}

				if (mainActivityDocument == null) {
					mainActivityDocument = sax.build(new StringReader(
							responseBody));
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				responseBody = "<activity><t>Unable to open file " + pageUrl
						+ "</t></activity>";
				try {
					mainActivityDocument = sax.build(new StringReader(
							responseBody));
				} catch (JDOMException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			} catch (JDOMParseException e) {
				e.printStackTrace();
				responseBody = "<activity><t>" + e.getMessage()
						+ "</t></activity>";
				try {
					mainActivityDocument = sax.build(new StringReader(
							responseBody));
				} catch (JDOMException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JDOMException e) {
				e.printStackTrace();
			}

			controllerIdentifier = mainActivityDocument.getRootElement()
					.getAttributeValue("controller");
			ActivityBuilder builder = new ActivityBuilder(mainActivityDocument,
					null, baseUrl);
			page.setBuilder(builder);

			ArrayList<Object> resultBundle = builder.preload(context, bundle);
			page.setAssets(resultBundle);

		} else if (pageUrl.endsWith(".rb")) {
			controllerIdentifier = pageUrl;
			ActivityBuilder builder = new ActivityBuilder(null,
					null, baseUrl);
			page.setBuilder(builder);

		}

		if (controllerIdentifier != null) {
			String csplit[] = org.apache.commons.lang3.StringUtils.split(
					controllerIdentifier, "#");
			if (csplit.length == 2) {
				if (!csplit[1].trim().equals("")) {
					controllerClass = csplit[1];
				}

				if (!csplit[0].trim().equals("")) {
					Log.d("Activity loader", "loading controller file "
							+ baseUrl + csplit[0]);
					downloadScript(context, bundle, page, app,
							scriptingContainer, csplit[0]);
				}
			} else {
				downloadScript(context, bundle, page, app, scriptingContainer,
						controllerIdentifier);
				String pathComponents[] = StringUtils.split(
						controllerIdentifier, "/");
				controllerClass = StringUtils.replace(
						pathComponents[pathComponents.length - 1], ".rb", "");
			}

		}

		page.setControllerClass(controllerClass);

		scriptingContainer.put("$container_payload", bundle.getPayload());
		try {
			scriptingContainer.runScriptlet("$framework.before_activity_setup");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return page;
	}

	private static void downloadScript(Context context, ExecutionBundle bundle,
			PageAsset page, DroiubyApp app,
			ScriptingContainer scriptingContainer, String scriptUrl) {
		String controller_content = null;
		try {
			controller_content = (String) Utils.loadAppAsset(app, context,
					scriptUrl, Utils.ASSET_TYPE_TEXT, Utils.HTTP_GET);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		long start = System.currentTimeMillis();
		try {
			page.setPreParsedScript(Utils.preParseRuby(scriptingContainer,
					controller_content));
		} catch (ParseFailedException e) {
			e.printStackTrace();
			bundle.addError(e.getMessage());
		}
		long elapsed = System.currentTimeMillis() - start;
		Log.d(DroiubyLauncher.class.toString(),
				"controller preparse: elapsed time = " + elapsed + "ms");
	}

	public static Boolean downloadAssets(Context context, DroiubyApp app,
			ExecutionBundle executionBundle) {

		ScriptingContainer scriptingContainer = executionBundle.getPayload()
				.getContainer();
		if (!executionBundle.isLibraryInitialized()) {
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
											Log.d(DroiubyLauncher.class
													.toString(),
													"Adding vendor path "
															+ vendorPath);
											loadPaths.add(vendorPath);
										}
									} catch (IOException e) {
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
						Log.d(DroiubyLauncher.class.toString(),
								"examine lib at " + path);
						File fpath = new File(path);
						if (fpath.isDirectory()) {
							Log.d(DroiubyLauncher.class.toString(), "Adding "
									+ path + " to load paths.");
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
						Log.d(DroiubyLauncher.class.toString(),
								"executing asset");
						if (elem instanceof EmbedEvalUnit) {
							((EmbedEvalUnit) elem).run();
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			Log.d(DroiubyLauncher.class.toString(), "initializing framework");
			try {
				scriptingContainer.runScriptlet("require '"
						+ app.getFramework() + "/" + app.getFramework() + "'");
				executionBundle.setLibraryInitialized(true);
			} catch (Exception e) {
				e.printStackTrace();
				executionBundle.addError(e.getMessage());
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

	public static IRubyObject runController(Activity activity,
			ExecutionBundle bundle, PageAsset page, boolean refresh) {
		bundle.getPayload().setCurrentActivity(activity);
		bundle.getPayload().setCurrentPage(page);

		EmbedEvalUnit preParsedScript = page.getPreParsedScript();
		ScriptingContainer scriptingContainer = bundle.getContainer();
		long start = System.currentTimeMillis();
		IRubyObject instance = null;
		try {
			if (preParsedScript != null) {

				preParsedScript.run();
				long elapsed = System.currentTimeMillis() - start;
				Log.d(DroiubyLauncher.class.toString(),
						"Preparse started. elapsed " + elapsed);
				start = System.currentTimeMillis();
			}

			scriptingContainer.runScriptlet("$framework.preload");
			if (preParsedScript != null) {
				Log.d(DroiubyLauncher.class.toString(),
						"class = " + page.getControllerClass());

				instance = (IRubyObject) scriptingContainer
						.runScriptlet("$framework.script('"
								+ page.getControllerClass() + "',"
								+ (refresh ? "true" : "false") + ")");
				bundle.setCurrentController(instance);
			}

			return instance;
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
		} finally {
			long elapsed = System.currentTimeMillis() - start;
			Log.d(DroiubyLauncher.class.toString(),
					"run Controller: elapsed time = " + elapsed + "ms");
		}
		return null;
	}

	public static void refresh(Activity currentActivity,
			ExecutionBundle bundle, OnPageRefreshListener listener) {
		PageRefreshTask refreshTask = new PageRefreshTask(currentActivity,
				bundle, listener, PageRefreshTask.QUICK_ACTIVITY_REFRESH);
		refreshTask.execute();
	}

	public static IRubyObject runController(Activity activity,
			String bundleName, String pageUrl, boolean refresh) {
		ExecutionBundle bundle = ExecutionBundleFactory.getBundle(bundleName);
		PageAsset page = bundle.getPage(pageUrl);
		return runController(activity, bundle, page, refresh);
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

		ActivityBuilder builder = page.getBuilder();
		if (builder != null) {
			Log.d(DroiubyLauncher.class.toString(),
					"parsing and preparing views....");
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
			Log.d(DroiubyLauncher.class.toString(),
					"Setting current bundle ...");
			console.setBundle(executionBundle);
			console.setActivity(executionBundle.getPayload()
					.getCurrentActivity());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
