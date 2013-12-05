package com.droiuby.client.core.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.EvalFailedException;
import org.jruby.embed.ScriptingContainer;
import org.jruby.javasupport.JavaObject;
import org.jruby.runtime.builtin.IRubyObject;

import com.droiuby.application.ActiveApp;
import com.droiuby.callbacks.DocumentReadyListener;
import com.droiuby.client.core.ActivityBuilder;
import com.droiuby.client.core.AppCache;
import com.droiuby.client.core.AssetDownloadCompleteListener;
import com.droiuby.client.core.AssetDownloadWorker;
import com.droiuby.client.core.ExecutionBundle;
import com.droiuby.client.core.OnDownloadCompleteListener;
import com.droiuby.client.core.RubyContainerPayload;
import com.droiuby.client.core.interfaces.OnUrlChangedListener;
import com.droiuby.client.core.postprocessor.CssPreloadParser;
import com.droiuby.client.core.postprocessor.GenericPostProcessor;
import com.droiuby.client.core.postprocessor.ScriptPreparser;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ActiveAppDownloader extends AsyncTask<Void, Void, Boolean>
		implements DocumentReadyListener {

	String baseUrl;
	Activity targetActivity;
	ActiveApp app;
	Element rootElem;
	Document mainActivityDocument;
	String controller;
	ActivityBuilder builder;
	ScriptingContainer scriptingContainer;
	RubyContainerPayload payload;
	ArrayList<EmbedEvalUnit> evalUnits = new ArrayList<EmbedEvalUnit>();
	OnDownloadCompleteListener listener;
	OnUrlChangedListener urlChangedListener;
	Vector<Object> resultBundle = new Vector<Object>();
	int resId;

	public OnUrlChangedListener getUrlChangedListener() {
		return urlChangedListener;
	}

	public void setUrlChangedListener(OnUrlChangedListener urlChangedListener) {
		this.urlChangedListener = urlChangedListener;
	}

	ExecutionBundle executionBundle;

	public AppCache getCache() {
		AppCache cache = new AppCache();
		cache.setMainActivityDocument(mainActivityDocument);
		cache.setEvalUnits(evalUnits);
		cache.setExecutionBundle(executionBundle);
		return cache;
	}

	SAXBuilder sax = new SAXBuilder();

	public ActiveAppDownloader(ActiveApp app, Activity targetActivity,
			ViewGroup target, AppCache cache, ExecutionBundle executionBundle,
			OnDownloadCompleteListener listener, int resId) {
		this.targetActivity = targetActivity;
		this.app = app;
		this.baseUrl = app.getBaseUrl();
		this.executionBundle = executionBundle;
		this.scriptingContainer = executionBundle.getContainer();
		this.payload = executionBundle.getPayload();
		this.listener = listener;
		this.resId = resId;
		if (cache != null) {
			this.mainActivityDocument = cache.getMainActivityDocument();
		}
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		Toast.makeText(targetActivity, "Loading app", Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		return super.equals(o);
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}

	public String loadAsset(String asset_name) {
		if (baseUrl.indexOf("asset:") != -1) {
			return Utils.loadAsset(targetActivity, baseUrl + asset_name);
		} else if (baseUrl.indexOf("file:") != -1) {
			return Utils.loadFile(asset_name);
		} else if (baseUrl.indexOf("sdcard:") != -1) {
			File directory = Environment.getExternalStorageDirectory();
			try {
				String asset_path = directory.getCanonicalPath() + asset_name;
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
					app.getName());
		}
	}

	public static ActiveApp loadApp(Context c, String url) {
		String responseBody = null;
		Log.d(ActiveAppDownloader.class.toString(), "loading " + url);
		if (url.startsWith("file://") || url.indexOf("asset:") != -1) {
			responseBody = Utils.loadAsset(c, url);
		} else {
			responseBody = Utils.query(url, c, null);
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

				ActiveApp app = new ActiveApp();
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
					} else if (orientation.equals("portrait") || orientation.equals("vertical")) {
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
						int type_int = ActiveApp.ASSET_TYPE_SCRIPT;
						if (asset_type.equals("script")) {
							type_int = ActiveApp.ASSET_TYPE_SCRIPT;
						} else if (asset_type.equals("css")) {
							type_int = ActiveApp.ASSET_TYPE_CSS;
						} else if (asset_type.equals("lib")) {
							type_int = ActiveApp.ASSET_TYPE_LIB;
						} else if (asset_type.equals("binary") || asset_type.equals("file")) {
							type_int = ActiveApp.ASSET_TYPE_BINARY;
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

	private static String extractBasePath(String adjusted_path) {
		int pos = 0;

		while ((adjusted_path.indexOf("/", pos)) != -1) {
			pos = adjusted_path.indexOf("/", pos) + 1;
		}
		adjusted_path = adjusted_path.substring(0, pos);
		return adjusted_path;
	}

	public Boolean downloadAssets() {
		if (!executionBundle.isLibraryInitialized()) {
			Log.d(this.getClass().toString(), "initializing framework");
			scriptingContainer.runScriptlet("require '"  + app.getFramework() + "/" + app.getFramework() + "'");
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
					if (asset_type == ActiveApp.ASSET_TYPE_SCRIPT) {
						listener = new ScriptPreparser();
					} else if (asset_type == ActiveApp.ASSET_TYPE_CSS) {
						listener = new CssPreloadParser();
					} else if (asset_type == ActiveApp.ASSET_TYPE_LIB) {
						List<String> loadPaths = new ArrayList<String>();
						String path = Utils.stripProtocol(app.getBaseUrl())
								+ asset_name;
						Log.d(this.getClass().toString(), "examine lib at " + path);
						File fpath = new File(path);
						if (fpath.isDirectory()) {
							Log.d(this.getClass().toString(), "Adding " + path
									+ " to load paths.");
							loadPaths.add(path);
							scriptingContainer.getProvider().getRuntime().getLoadService().addPaths(loadPaths);
						}
						continue;
					} else if (asset_type == ActiveApp.ASSET_TYPE_BINARY) {
						download_type = Utils.ASSET_TYPE_BINARY;
					}
					
					Log.d(this.getClass().toString(), "downloading "
							+ asset_name + " ...");
					AssetDownloadWorker worker = new AssetDownloadWorker(
							targetActivity, app, executionBundle, asset_name,
							download_type, resultBundle, listener,
							Utils.HTTP_GET);
					thread_pool.execute(worker);
				}
				thread_pool.shutdown();
				try {
					thread_pool.awaitTermination(240, TimeUnit.SECONDS);

					for (Object elem : resultBundle) {
						Log.d(this.getClass().toString(), "executing asset");
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

	@Override
	protected Boolean doInBackground(Void... arg0) {
		return downloadAssets();
	}

	@Override
	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		Log.d(this.getClass().toString(), "Loading activity builder...");
		String targetUrl = app.getMainUrl();
		if (executionBundle.getCurrentUrl() != null) {
			targetUrl = executionBundle.getCurrentUrl();
		}
		Log.d(this.getClass().toString(), "target url = " + targetUrl);
		ActivityBuilder.loadLayout(executionBundle, app, targetUrl, false,
				Utils.HTTP_GET, targetActivity, this.mainActivityDocument,
				this, resId);
	}

	public void onDocumentReady(Document mainActivity) {
		this.mainActivityDocument = mainActivity;
	}

}
