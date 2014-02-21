package com.droiuby.client.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

import org.jdom2.Document;
import org.jruby.Ruby;
import org.jruby.RubyInteger;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;

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

import com.droiuby.application.DroiubyApp;
import com.droiuby.callbacks.DocumentReadyListener;
import com.droiuby.callbacks.OnRefreshRequested;
import com.droiuby.client.core.builder.ActivityBuilder;
import com.droiuby.client.core.console.WebConsole;
import com.droiuby.client.core.utils.Utils;
import com.droiuby.interfaces.DroiubyHelperInterface;
import com.droiuby.launcher.Options;

public class DroiubyHelper implements OnDownloadCompleteListener,
		DocumentReadyListener, DroiubyHelperInterface {

	DroiubyApp application;
	Activity activity;
	IRubyObject backingObject;
	Ruby runtime;

	protected HashSet<String> methodCache;

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

	public DroiubyHelperInterface setExecutionBundle(Activity activity,
			String bundleName) {
		ExecutionBundle bundle = ExecutionBundleFactory.getBundle(bundleName);
		setExecutionBundle(bundle);
		this.activity = activity;
		return this;
	}

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

		String ipString = String.format(Locale.ENGLISH, "%d.%d.%d.%d",
				(ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff),
				(ip >> 24 & 0xff));

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
		try {
			if (methodCache.contains("onResume")) {
				IRubyObject[] args = new IRubyObject[] {};
				backingObject.callMethod(runtime.getCurrentContext(),
						"onResume", args);
			}
		} catch (RaiseException e) {
			e.printStackTrace();
			executionBundle.addError(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.droiuby.client.core.DroiubyHelperInterface#setActiveApp(com.droiuby
	 * .application.ActiveApp)
	 */
	public void setActiveApp(DroiubyApp application) {
		this.application = application;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.droiuby.client.core.DroiubyHelperInterface#onActivityResult(int,
	 * int, android.content.Intent)
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		try {
			if (methodCache.contains("on_activity_result")) {
				IRubyObject wrapped_param1 = RubyInteger.int2fix(runtime,
						requestCode);
				IRubyObject wrapped_param2 = RubyInteger.int2fix(runtime,
						resultCode);
				IRubyObject wrapped_param3 = JavaUtil.convertJavaToRuby(
						runtime, intent);
				IRubyObject[] args = new IRubyObject[] { wrapped_param1,
						wrapped_param2, wrapped_param3 };
				backingObject.callMethod(runtime.getCurrentContext(),
						"on_activity_result", args);
			}
		} catch (RaiseException e) {
			e.printStackTrace();
			executionBundle.addError(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.droiuby.client.core.DroiubyHelperInterface#start(java.lang.String)
	 */
	public void start(String url) {
		DroiubyLauncher.launch(activity, url);
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

	public void launch(Context context, String url, Class<?> activityClass,
			Options options) {
		DroiubyLauncher.launch(context, url, activityClass, options);
	}

	public void setPage(Activity activity, String bundleName, String pageUrl) {
		DroiubyLauncher.setPage(activity, bundleName, pageUrl);
	}

	public void runController(Activity activity, String bundleName,
			String pageUrl) {

		this.backingObject = DroiubyLauncher.runController(activity,
				bundleName, pageUrl);
		this.executionBundle = ExecutionBundleFactory.getBundle(bundleName);
		this.runtime = executionBundle.getContainer().getProvider()
				.getRuntime();
		if (backingObject != null) {
			methodCache = Utils.toStringSet(backingObject.callMethod(
					executionBundle.getContainer().getProvider().getRuntime()
							.getCurrentContext(), "methods",
					new IRubyObject[] {}));
		}
	}
}