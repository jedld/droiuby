package com.droiuby.client;

import java.net.MalformedURLException;
import java.net.URL;

import org.jdom2.Document;

import com.droiuby.client.R;
import com.droiuby.client.core.ActiveApp;
import com.droiuby.client.core.ActivityBuilder;
import com.droiuby.client.core.AppDownloader;
import com.droiuby.client.core.DroiubyHelper;
import com.droiuby.client.core.ExecutionBundle;
import com.droiuby.client.core.ExecutionBundleFactory;
import com.droiuby.client.core.callbacks.OnAppDownloadComplete;
import com.droiuby.client.core.listeners.DocumentReadyListener;
import com.droiuby.client.utils.Utils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class CanvasActivity extends Activity implements DocumentReadyListener,
		OnAppDownloadComplete {

	ViewGroup target;
	ActiveApp application;
	RelativeLayout topview;
	DroiubyHelper droiuby;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.canvas);
		droiuby = new DroiubyHelper(this);
		int default_orientation = getResources().getConfiguration().orientation;
		if (default_orientation == Configuration.ORIENTATION_LANDSCAPE) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		Bundle params = this.getIntent().getExtras();
		if (params != null) {
			application = (ActiveApp) params.getSerializable("application");
			if (application != null) {
				target = (ViewGroup) this.findViewById(R.id.mainLayout);
				topview = (RelativeLayout) target;

				String pageUrl = (String) params.getString("startUrl");
				if (application != null && pageUrl != null) {
					ExecutionBundleFactory factory = ExecutionBundleFactory
							.getInstance();
					if (factory.bundleAvailableFor(application.getBaseUrl())) {
						ExecutionBundle bundle = factory.getNewScriptingContainer(
								this, application.getBaseUrl());
						droiuby.setExecutionBundle(bundle);
						ActivityBuilder.loadLayout(bundle,
								application, pageUrl, false, Utils.HTTP_GET,
								this, null, this, R.id.mainLayout);
					} else {
						droiuby.setupApplication(application, target, R.id.mainLayout);
					}
				} else {
					droiuby.setupApplication(application, target, R.id.mainLayout);
				}
			}
		} else {
			AppDownloader downloader = new AppDownloader(this,
					"asset:launcher/config.xml", this.getClass(), this);
			downloader.execute();
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		droiuby.onStart();
	}

	public void refreshCurrentApplication() {
		ViewGroup view = (ViewGroup) findViewById(R.id.mainLayout);
		view.removeAllViews();
		droiuby.reloadApplication(application, target, R.id.mainLayout);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.itemRefresh) {
			refreshCurrentApplication();
		} else if (itemId == R.id.itemConsole) {
			droiuby.showConsoleInfo();
		} else if (itemId == R.id.itemLog) {
			if (findViewById(R.id.loglayout) == null) {
				View logview = getLayoutInflater().inflate(R.layout.log, null);
				RelativeLayout.LayoutParams logPos = new RelativeLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, 200);
				logPos.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
						R.id.mainLayout);
				topview.addView(logview, logPos);
			}
			LinearLayout errorListLayout = (LinearLayout) findViewById(R.id.errorLogGroup);
			ScrollView scroll = (ScrollView) findViewById(R.id.scrollViewLog);
			errorListLayout.removeAllViews();
			for (String error : droiuby.getExecutionBundle().getScriptErrors()) {
				TextView errorText = new TextView(this);
				errorText.setText(error);
				errorListLayout.addView(errorText, LayoutParams.MATCH_PARENT,
						LayoutParams.WRAP_CONTENT);
			}
		} else if (itemId == R.id.itemClearCache) {
			SharedPreferences prefs = getSharedPreferences("cookies",
					MODE_PRIVATE);
			try {
				Editor editor = prefs.edit();
				URL url;
				url = new URL(application.getBaseUrl());
				editor.putString(url.getProtocol() + "_" + url.getHost() + "_"
						+ application.getName(), "");
				editor.commit();
				droiuby.getExecutionBundle().setCurrentUrl(null);
				droiuby.getExecutionBundle().setLibraryInitialized(false);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	public void onDocumentReady(Document mainActivity) {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.parseroptions, menu);
		return true;
	}

	public void onDownloadComplete(ActiveApp app) {
		this.application = app;
		droiuby.setupApplication(app, (ViewGroup) this.findViewById(R.id.mainLayout),
				R.id.mainLayout);
		onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		droiuby.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		droiuby.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onResume() {
		super.onResume();
		droiuby.onResume();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onRetainNonConfigurationInstance()
	 */
	@Override
	@Deprecated
	public Object onRetainNonConfigurationInstance() {
		return droiuby.onRetainNonConfigurationInstance();
	}
	
	
}
