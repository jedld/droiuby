package com.droiuby.client.core;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.jdom2.Document;
import org.jruby.embed.ScriptingContainer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.SensorEvent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import com.droiuby.application.ActiveApp;
import com.droiuby.callbacks.DocumentReadyListener;
import com.droiuby.callbacks.OnAppDownloadComplete;
import com.droiuby.callbacks.OnRefreshRequested;
import com.droiuby.client.core.console.WebConsole;
import com.droiuby.client.core.utils.ActiveAppDownloader;
import com.droiuby.client.core.utils.Utils;
import com.droiuby.interfaces.DroiubyHelperInterface;

public class DroiubyHelper implements OnAppDownloadComplete,
		OnDownloadCompleteListener, DocumentReadyListener,
		DroiubyHelperInterface {
	/** Called when the activity is first created. */
	ActiveApp application;
	Activity activity;

	public DroiubyHelper() {
		Log.d(this.getClass().toString(), "new instance...");
	}

	/**
	 * @return the activity
	 */
	public Activity getActivity() {
		return activity;
	}

	/**
	 * @param activity
	 *            the activity to set
	 */
	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	AppCache cache;

	protected ExecutionBundle executionBundle;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.droiuby.client.core.DroiubyHelperInterface#getExecutionBundle()
	 */
	public ExecutionBundle getExecutionBundle() {
		return executionBundle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.droiuby.client.core.DroiubyHelperInterface#setExecutionBundle(com
	 * .droiuby.client.core.ExecutionBundle)
	 */
	public void setExecutionBundle(ExecutionBundle executionBundle) {
		this.executionBundle = executionBundle;
	}

	ActiveAppDownloader downloader;
	String currentUrl;
	private ClassLoader loader;

	protected int getMainLayoutId() {
		return ActivityBuilder.getViewById(activity, "mainLayout");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.droiuby.client.core.DroiubyHelperInterface#onIntent(android.os.Bundle
	 * )
	 */
	public void onIntent(Bundle params) {
		Log.d(this.getClass().toString(), "onIntent");
		application = (ActiveApp) params.getSerializable("application");
		if (application != null) {
			ViewGroup target = (ViewGroup) activity
					.findViewById(getMainLayoutId());

			String pageUrl = (String) params.getString("startUrl");
			if (application != null && pageUrl != null) {
				ExecutionBundleFactory factory = ExecutionBundleFactory
						.getInstance(loader);
				if (factory.bundleAvailableFor(application.getBaseUrl())) {
					Log.d(this.getClass().toString(), "new Bundle");
					ExecutionBundle bundle = factory.getNewScriptingContainer(
							activity, application.getBaseUrl());
					setExecutionBundle(bundle);
					ActivityBuilder.loadLayout(bundle, application, pageUrl,
							false, Utils.HTTP_GET, activity, null, this,
							getMainLayoutId());
				} else {
					setupApplication(application, target, getMainLayoutId());
				}
			} else {
				setupApplication(application, target, getMainLayoutId());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.droiuby.client.core.DroiubyHelperInterface#reloadApplication(com.
	 * droiuby.application.ActiveApp, int)
	 */
	public void reloadApplication(ActiveApp application, int mainlayout) {
		ScriptingContainer container = executionBundle.getContainer();
		ViewGroup target = (ViewGroup) activity.findViewById(mainlayout);
		if (executionBundle.getCurrentController() != null) {
			container
					.put("_controller", executionBundle.getCurrentController());

			executionBundle.getContainer().runScriptlet(
					"_controller.on_activity_reload");
		}
		this.setupApplication(application, target, mainlayout);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.droiuby.client.core.DroiubyHelperInterface#getIpAddr()
	 */
	public String getIpAddr() {
		WifiManager wifiManager = (WifiManager) activity
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ip = wifiInfo.getIpAddress();

		String ipString = String.format("%d.%d.%d.%d", (ip & 0xff),
				(ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));

		return ipString.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.droiuby.client.core.DroiubyHelperInterface#showConsoleInfo()
	 */
	public void showConsoleInfo() {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage("Console running at " + getIpAddr() + ":4000")
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.droiuby.client.core.DroiubyHelperInterface#setupApplication(com.droiuby
	 * .application.ActiveApp, android.view.ViewGroup, int)
	 */
	public void setupApplication(ActiveApp application, ViewGroup target,
			int resId) {
		Log.d(this.getClass().toString(),
				"setupApplication -> Loading application at "
						+ application.getName());
		final AppCache cache = (AppCache) activity
				.getLastNonConfigurationInstance();
		this.application = application;

		if (cache != null) {
			executionBundle = cache.getExecutionBundle();
		} else {
			ExecutionBundleFactory factory = ExecutionBundleFactory
					.getInstance(loader);
			executionBundle = factory.getNewScriptingContainer(activity,
					application.getBaseUrl());
			executionBundle.setCurrentActivity(activity);
		}

		setCurrentWebConsoleBundle(executionBundle, activity);
		
		downloader = new ActiveAppDownloader(application, activity, target,
				cache, executionBundle, this, resId);

		downloader.execute();
	}

	public void setCurrentWebConsoleBundle(ExecutionBundle bundle,
			Activity activity) {
		WebConsole console = WebConsole.getInstance();
		if (console != null) {
			console.setBundle(bundle);
			console.setActivity(activity);
		}
	}

	/**
	 * @return the loader
	 */
	public ClassLoader getLoader() {
		return loader;
	}

	/**
	 * @param loader
	 *            the loader to set
	 */
	public void setLoader(ClassLoader loader) {
		this.loader = loader;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.droiuby.client.core.DroiubyHelperInterface#onStart()
	 */
	public void onStart() {
		setCurrentWebConsoleBundle(executionBundle, activity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.droiuby.client.core.DroiubyHelperInterface#onDestroy()
	 */
	public void onDestroy() {
		WebConsole console = WebConsole.getInstance();
		if (console != null) {
			console.shutdownConsole();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.droiuby.client.core.DroiubyHelperInterface#onResume()
	 */
	public void onResume() {
		setupConsole();
		if (executionBundle != null) {
			executionBundle.setCurrentActivity(activity);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.droiuby.client.core.DroiubyHelperInterface#
	 * onRetainNonConfigurationInstance()
	 */
	public Object onRetainNonConfigurationInstance() {
		return downloader.getCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.droiuby.client.core.DroiubyHelperInterface#setActiveApp(com.droiuby
	 * .application.ActiveApp)
	 */
	public void setActiveApp(ActiveApp application) {
		this.application = application;
	}

	private void setupConsole() {
		String web_public_loc;
		Log.d(this.getClass().toString(), "Loading WebConsole...");
		try {
			web_public_loc = activity.getCacheDir().getCanonicalPath() + "/www";
			File webroot = new File(web_public_loc);
			webroot.mkdirs();
			WebConsole console = WebConsole.getInstance(4000, webroot);
			console.setBundle(executionBundle);
			console.setActivity(activity);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.droiuby.client.core.DroiubyHelperInterface#onActivityResult(int,
	 * int, android.content.Intent)
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (executionBundle != null) {
			if (executionBundle.getCurrentController() != null) {
				try {
					ScriptingContainer container = executionBundle
							.getContainer();
					container.put("_requestCode", requestCode);
					container.put("_resultCode", resultCode);
					container.put("_intent", intent);
					container.put("_controller",
							executionBundle.getCurrentController());
					container
							.runScriptlet("_controller.on_activity_result(_requestCode, _resultCode, wrap_native(_intent))");
				} catch (org.jruby.embed.EvalFailedException e) {
					Log.d(this.getClass().toString(),
							"eval failed: " + e.getMessage());
					e.printStackTrace();
					executionBundle.addError(e.getMessage());
				} catch (org.jruby.embed.ParseFailedException e) {
					e.printStackTrace();
					executionBundle.addError(e.getMessage());
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.droiuby.client.core.DroiubyHelperInterface#start(java.lang.String)
	 */
	public void start(String url) {
		AppDownloader downloader = new AppDownloader(activity, url,
				activity.getClass(), this);
		downloader.execute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.droiuby.client.core.DroiubyHelperInterface#onDownloadComplete(com
	 * .droiuby.application.ActiveApp)
	 */
	public void onDownloadComplete(ActiveApp app) {
		Log.d(this.getClass().toString(), "onDownloadComplete()");
		setupApplication(app,
				(ViewGroup) activity.findViewById(getMainLayoutId()),
				getMainLayoutId());
		application = app;
		if (activity instanceof OnAppDownloadComplete) {
			((OnAppDownloadComplete) activity).onDownloadComplete(app);
		}
		onResume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.droiuby.client.core.DroiubyHelperInterface#onDocumentReady(org.jdom2
	 * .Document)
	 */
	public void onDocumentReady(Document mainActivity) {
		Log.d(this.getClass().toString(), "On document ready");
	}

	public ArrayList<String> getScriptErrors() {
		return getExecutionBundle().getScriptErrors();
	}

	public void setCurrentUrl(String currentUrl) {
		getExecutionBundle().setCurrentUrl(currentUrl);

	}

	public void setLibraryInitialized(boolean b) {
		getExecutionBundle().setLibraryInitialized(b);
	}

	public void reloadApplication(int mainlayout) {
		reloadApplication(application, mainlayout);
	}

	public void clearCache() {
		SharedPreferences prefs = activity.getSharedPreferences("cookies",
				Context.MODE_PRIVATE);
		try {
			Editor editor = prefs.edit();
			URL url;
			url = new URL(application.getBaseUrl());
			editor.putString(url.getProtocol() + "_" + url.getHost() + "_"
					+ application.getName(), "");
			editor.commit();
			setCurrentUrl(null);
			setLibraryInitialized(false);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onSensorChanged(SensorEvent event) {

		SharedPreferences prefs = activity.getSharedPreferences("bootstrap",
				Context.MODE_PRIVATE);
		if (prefs != null && prefs.getBoolean("proximity_refresh", false)) {
			if (event.values[0] == 0) {
				if (activity instanceof OnRefreshRequested) {
					((OnRefreshRequested) activity).refreshCurrentApplication();
				}
			}
		}

	}

	public SharedPreferences getCurrentPreferences() {
		if (application != null) {
			return application.getCurrentPreferences(activity);
		}
		return null;
	}

	public void startDefault() {
		start("asset:launcher/config.droiuby");
	}
}