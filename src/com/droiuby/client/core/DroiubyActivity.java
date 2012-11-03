package com.droiuby.client.core;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.jruby.embed.ScriptingContainer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.ViewGroup;

import com.droiuby.client.WebConsole;
import com.droiuby.client.utils.ActiveAppDownloader;

public abstract class DroiubyActivity extends Activity implements
		OnDownloadCompleteListener {
	/** Called when the activity is first created. */
	ActiveApp application;
	AppCache cache;
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

	public SharedPreferences getCurrentPreferences() {
		try {
			SharedPreferences prefs = null;
			if (application.getBaseUrl().startsWith("asset:")) {
				String asset_name = "data_" + application.getBaseUrl();
				asset_name = asset_name.replace('/', '_').replace('\\', '_');
				prefs = getSharedPreferences(asset_name, MODE_PRIVATE);
			} else {
				URL parsedURL = new URL(application.getBaseUrl());
				prefs = getSharedPreferences("data_" + parsedURL.getProtocol()
						+ "_" + parsedURL.getHost(), MODE_PRIVATE);
			}
			return prefs;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String getIpAddr() {
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ip = wifiInfo.getIpAddress();

		String ipString = String.format("%d.%d.%d.%d", (ip & 0xff),
				(ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));

		return ipString.toString();
	}

	protected void showConsoleInfo() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

	protected void setupApplication(ActiveApp application, ViewGroup target, int resId) {
		Log.d(this.getClass().toString(), "Loading application at "
				+ application.getName());
		final AppCache cache = (AppCache) getLastNonConfigurationInstance();
		this.application = application;

		if (cache != null) {
			executionBundle = cache.getExecutionBundle();
		} else {
			ExecutionBundleFactory factory = ExecutionBundleFactory
					.getInstance();
			executionBundle = factory.getNewScriptingContainer(this,
					application.getBaseUrl());
			executionBundle.setCurrentActivity(this);
		}

		downloader = new ActiveAppDownloader(application, this, target, cache,
				executionBundle, this, resId);

		downloader.execute();
	}


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		console.shutdownConsole();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(this.getClass().toString(), "onResume() called");
		setupConsole();
		if (executionBundle != null) {
			executionBundle.setCurrentActivity(this);
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return downloader.getCache();
	}

	public void setActiveApp(ActiveApp application) {
		this.application = application;
	}

	private void setupConsole() {
		String web_public_loc;
		try {
			web_public_loc = this.getCacheDir().getCanonicalPath() + "/www";
			File webroot = new File(web_public_loc);
			webroot.mkdirs();
			console = WebConsole.getInstance(4000, webroot);
			ScriptingContainer container = null;
			if (executionBundle != null) {
				container = executionBundle.getContainer();
			}
			console.setContainer(container);
			console.setActivity(this);
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
}