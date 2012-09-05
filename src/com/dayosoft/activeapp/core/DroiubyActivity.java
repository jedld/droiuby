package com.dayosoft.activeapp.core;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jruby.CompatVersion;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;

import com.dayosoft.activeapp.R;
import com.dayosoft.activeapp.WebConsole;
import com.dayosoft.activeapp.R.id;
import com.dayosoft.activeapp.R.layout;
import com.dayosoft.activeapp.R.menu;
import com.dayosoft.activeapp.utils.ActiveAppDownloader;

import android.app.Activity;
import android.content.SharedPreferences;
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
	ExecutionBundle executionBundle;
	ActiveAppDownloader downloader;
	private WebConsole console;

	private ExecutionBundle getNewScriptingContainer() {
		ExecutionBundle bundle = new ExecutionBundle();
		ScriptingContainer container = new ScriptingContainer(
				LocalVariableBehavior.PERSISTENT);
		RubyContainerPayload payload = new RubyContainerPayload();

		payload.setCurrentActivity(this);
		payload.setContainer(container);
		container.setObjectSpaceEnabled(false);
		container.setCompatVersion(CompatVersion.RUBY1_9);
		try {
			container.setHomeDirectory(this.getCacheDir().getCanonicalPath()
					+ "/jruby/home");
			List<String> loadPaths = new ArrayList();
			loadPaths.add(this.getCacheDir().getCanonicalPath()
					+ "/jruby/vendor");
			loadPaths.add(this.getCacheDir().getCanonicalPath()
					+ "/jruby/vendor/lib");
			container.setLoadPaths(loadPaths);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		container.put("container_payload", payload);
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
				prefs = getSharedPreferences(asset_name
						, MODE_PRIVATE);
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

	protected void setupApplication(ActiveApp application, ViewGroup target) {
		Log.d(this.getClass().toString(), "Loading application at "
				+ application.getName());
		final AppCache cache = (AppCache) getLastNonConfigurationInstance();
		this.application = application;
		executionBundle = getNewScriptingContainer();

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
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		console.stop();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
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
			console = WebConsole.getInstance(4000, webroot, this,
					executionBundle.getContainer());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}