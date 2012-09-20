package com.dayosoft.activeapp;

import java.net.MalformedURLException;
import java.net.URL;

import com.dayosoft.activeapp.core.ActiveApp;
import com.dayosoft.activeapp.core.DroiubyActivity;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;

public class CanvasActivity extends DroiubyActivity {

	ViewGroup target;
	ActiveApp application;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.canvas);
		Bundle params = this.getIntent().getExtras();
		application = (ActiveApp) params.getSerializable("application");
		target = (ViewGroup) this.findViewById(R.id.mainLayout);

		setupApplication(application, target);
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
				editor.putString(url.getProtocol() + "_" + url.getHost() + "_" + application.getName(), "");
				editor.commit();
				getExecutionBundle().setCurrentUrl(null);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return false;
	}

}
