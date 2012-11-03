package com.droiuby.ide;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.jdom2.Document;

import com.droiuby.client.core.ActiveApp;
import com.droiuby.client.core.ActivityBuilder;
import com.droiuby.client.core.DroiubyActivity;
import com.droiuby.client.core.ExecutionBundle;
import com.droiuby.client.core.ExecutionBundleFactory;
import com.droiuby.client.core.callbacks.OnAppDownloadComplete;
import com.droiuby.client.core.interfaces.OnServerReadyListener;
import com.droiuby.client.core.listeners.DocumentReadyListener;
import com.droiuby.client.utils.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class CanvasActivity extends DroiubyActivity implements
		DocumentReadyListener, OnAppDownloadComplete, OnServerReadyListener {

	ViewGroup target;
	ActiveApp application;
	RelativeLayout topview;
	static final String START_URL = "asset:launcher/config.xml";
	WebView webConsole;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.canvas);
		int default_orientation = getResources().getConfiguration().orientation;
		webConsole = (WebView) findViewById(R.id.console);
		target = (ViewGroup) this.findViewById(R.id.mainLayout);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		if (default_orientation == Configuration.ORIENTATION_LANDSCAPE) {
			target.setMinimumHeight(480);
			target.setMinimumWidth(800);
		} else {
			target.setMinimumHeight(800);
			target.setMinimumWidth(480);
		}
		Bundle params = this.getIntent().getExtras();
		if (params != null) {
			application = (ActiveApp) params.getSerializable("application");
			if (application != null) {

				topview = (RelativeLayout) target;

				String pageUrl = (String) params.getString("startUrl");
				if (application != null && pageUrl != null) {
					ExecutionBundleFactory factory = ExecutionBundleFactory
							.getInstance();
					if (factory.bundleAvailableFor(application.getBaseUrl())) {
						executionBundle = factory.getNewScriptingContainer(
								this, application.getBaseUrl());
						ActivityBuilder.loadLayout(executionBundle,
								application, pageUrl, false, Utils.HTTP_GET,
								this, null, this);
					} else {
						setupApplication(application, target);
					}
				} else {
					setupApplication(application, target);
				}
			}
		} else {
			AppDownloader downloader = new AppDownloader(this, START_URL,
					this.getClass(), this);
			downloader.execute();
		}
		Timer timer = new Timer();
		
		UpdateLogTask task = new UpdateLogTask(this, R.id.errorLog);
		timer.schedule(task, 2000);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if (console != null) {
			console.setContainer(executionBundle.getContainer());
			console.setActivity(this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.parseroptions, menu);
		return true;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(this.getClass().toString(), "onResume() called");
		setupConsole(this);
		if (executionBundle != null) {
			executionBundle.setCurrentActivity(this);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.itemRefresh:
			setupApplication(application, target);
			break;
		case R.id.itemConsole:
			this.showConsoleInfo();
			break;
		case R.id.itemClearCache:
			SharedPreferences prefs = getSharedPreferences("cookies",
					MODE_PRIVATE);

			try {
				Editor editor = prefs.edit();
				URL url;
				url = new URL(application.getBaseUrl());
				editor.putString(url.getProtocol() + "_" + url.getHost() + "_"
						+ application.getName(), "");
				editor.commit();
				getExecutionBundle().setCurrentUrl(null);
				getExecutionBundle().setLibraryInitialized(false);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return false;
	}

	public void onDocumentReady(Document mainActivity) {
		// TODO Auto-generated method stub

	}

	public void onDownloadComplete(ActiveApp app) {
		setupApplication(app, (ViewGroup) this.findViewById(R.id.mainLayout));
		application = app;
		onResume();
	}

	public void onServerReady() {
		runOnUiThread(new Runnable() {
			public void run() {
				webConsole.loadUrl("http://127.0.0.1:"
						+ WebConsole.CONSOLE_PORT);
				webConsole.getSettings().setJavaScriptEnabled(true);
				webConsole.setVerticalScrollBarEnabled(true);
				webConsole.setHorizontalScrollBarEnabled(true);

				webConsole.setBackgroundColor(Color.parseColor("#FFFFFF"));
				webConsole.setWebViewClient(new WebViewClient() {
					@Override
					public boolean shouldOverrideUrlLoading(WebView view,
							String url) {
						view.loadUrl(url);
						return false;
					}
				});
			}
		});
	}
}
