package com.droiuby.client.core;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jruby.CompatVersion;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;

import com.droiuby.client.R;
import com.droiuby.client.R.id;
import com.droiuby.client.R.layout;
import com.droiuby.client.R.menu;
import com.droiuby.client.WebConsole;
import com.droiuby.client.core.interfaces.OnUrlChangedListener;
import com.droiuby.client.utils.ActiveAppDownloader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

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

	private ExecutionBundle getNewScriptingContainer() {
		ExecutionBundle bundle = new ExecutionBundle();
		ScriptingContainer container = new ScriptingContainer(LocalContextScope.SINGLETHREAD,
				LocalVariableBehavior.PERSISTENT);
		RubyContainerPayload payload = new RubyContainerPayload();
		payload.setCurrentActivity(this);
		payload.setContainer(container);
		bundle.setContainer(container);
		bundle.setPayload(payload);
		return bundle;
	}

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

		   String ipString = String.format(
		   "%d.%d.%d.%d",
		   (ip & 0xff),
		   (ip >> 8 & 0xff),
		   (ip >> 16 & 0xff),
		   (ip >> 24 & 0xff));

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
		       })
		       ;
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	protected void setupApplication(ActiveApp application, ViewGroup target) {
		Log.d(this.getClass().toString(), "Loading application at "
				+ application.getName());
		final AppCache cache = (AppCache) getLastNonConfigurationInstance();
		this.application = application;
		if (executionBundle == null) {
			executionBundle = getNewScriptingContainer();
		}

		downloader = new ActiveAppDownloader(application, this, target, cache,
				executionBundle, this);

		downloader.execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.parseroptions, menu);
		return true;
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
			console.setContainer(executionBundle.getContainer());
			console.setActivity(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}