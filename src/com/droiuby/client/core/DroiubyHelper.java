package com.droiuby.client.core;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.jruby.embed.ScriptingContainer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.ViewGroup;

import com.droiuby.client.R;
import com.droiuby.client.core.callbacks.OnAppDownloadComplete;
import com.droiuby.client.core.console.WebConsole;
import com.droiuby.client.utils.ActiveAppDownloader;

public class DroiubyHelper implements OnAppDownloadComplete,
		OnDownloadCompleteListener {
	/** Called when the activity is first created. */
	ActiveApp application;
	Activity activity;
	AppCache cache;

	public DroiubyHelper(Activity activity) {
		this.activity = activity;
	}

	protected ExecutionBundle executionBundle;

	public ExecutionBundle getExecutionBundle() {
		return executionBundle;
	}

	public void setExecutionBundle(ExecutionBundle executionBundle) {
		this.executionBundle = executionBundle;
	}

	ActiveAppDownloader downloader;
	String currentUrl;
	protected WebConsole console;

	public void reloadApplication(ActiveApp application, ViewGroup target,
			int mainlayout) {
		ScriptingContainer container = executionBundle.getContainer();

		container.put("_controller", executionBundle.getCurrentController());
		executionBundle.getContainer().runScriptlet(
				"_controller.on_activity_reload");
		this.setupApplication(application, target, mainlayout);
	}

	public SharedPreferences getCurrentPreferences() {
		try {
			SharedPreferences prefs = null;
			if (application.getBaseUrl().startsWith("asset:")) {
				String asset_name = "data_" + application.getBaseUrl();
				asset_name = asset_name.replace('/', '_').replace('\\', '_');
				prefs = activity.getSharedPreferences(asset_name,
						Context.MODE_PRIVATE);
			} else {
				URL parsedURL = new URL(application.getBaseUrl());
				prefs = activity.getSharedPreferences(
						"data_" + parsedURL.getProtocol() + "_"
								+ parsedURL.getHost(), Context.MODE_PRIVATE);
			}
			return prefs;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String getIpAddr() {
		WifiManager wifiManager = (WifiManager) activity
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ip = wifiInfo.getIpAddress();

		String ipString = String.format("%d.%d.%d.%d", (ip & 0xff),
				(ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));

		return ipString.toString();
	}

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

	public void setupApplication(ActiveApp application, ViewGroup target,
			int resId) {
		Log.d(this.getClass().toString(), "Loading application at "
				+ application.getName());
		final AppCache cache = (AppCache) activity
				.getLastNonConfigurationInstance();
		this.application = application;

		if (cache != null) {
			executionBundle = cache.getExecutionBundle();
		} else {
			ExecutionBundleFactory factory = ExecutionBundleFactory
					.getInstance();
			executionBundle = factory.getNewScriptingContainer(activity,
					application.getBaseUrl());
			executionBundle.setCurrentActivity(activity);
		}

		downloader = new ActiveAppDownloader(application, activity, target,
				cache, executionBundle, this, resId);

		downloader.execute();
	}

	public void onStart() {
		// TODO Auto-generated method stub
		if (console != null) {
			console.setBundle(executionBundle);
			console.setActivity(activity);
		}
	}

	public void onDestroy() {
		console.shutdownConsole();
	}

	public void onResume() {
		setupConsole();
		if (executionBundle != null) {
			executionBundle.setCurrentActivity(activity);
		}
	}

	public Object onRetainNonConfigurationInstance() {
		return downloader.getCache();
	}

	public void setActiveApp(ActiveApp application) {
		this.application = application;
	}

	private void setupConsole() {
		String web_public_loc;
		try {
			web_public_loc = activity.getCacheDir().getCanonicalPath() + "/www";
			File webroot = new File(web_public_loc);
			webroot.mkdirs();
			console = WebConsole.getInstance(4000, webroot);
			console.setBundle(executionBundle);
			console.setActivity(activity);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

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

	public void start(String url) {
		AppDownloader downloader = new AppDownloader(activity,
				url, activity.getClass(), this);
		downloader.execute();
	}
	
	public void onDownloadComplete(ActiveApp app) {
		setupApplication(app,
				(ViewGroup) activity.findViewById(R.id.mainLayout),
				R.id.mainLayout);
		if (activity instanceof OnAppDownloadComplete) {
			((OnAppDownloadComplete) activity).onDownloadComplete(app);
		}
		onResume();
	}
}