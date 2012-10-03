package com.droiuby.client;

import java.net.MalformedURLException;
import java.net.URL;

import com.droiuby.client.R;
import com.droiuby.client.core.ActiveApp;
import com.droiuby.client.core.DroiubyActivity;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class CanvasActivity extends DroiubyActivity {

	ViewGroup target;
	ActiveApp application;
	RelativeLayout topview;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.canvas);
		Bundle params = this.getIntent().getExtras();
		application = (ActiveApp) params.getSerializable("application");
		target = (ViewGroup) this.findViewById(R.id.mainLayout);
		topview = (RelativeLayout) target;
		setupApplication(application, target);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if (console!=null) {
			console.setContainer(executionBundle.getContainer());
			console.setActivity(this);
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
		case R.id.itemLog:
			
			if (findViewById(R.id.loglayout)==null) {
				View logview = getLayoutInflater().inflate(R.layout.log, null);
				RelativeLayout.LayoutParams logPos = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 200);
				logPos.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, R.id.mainLayout);
				topview.addView(logview, logPos);
			}
			
			LinearLayout errorListLayout = (LinearLayout)findViewById(R.id.errorLogGroup);
			ScrollView scroll = (ScrollView)findViewById(R.id.scrollViewLog);
			errorListLayout.removeAllViews();
			for(String error : executionBundle.getScriptErrors()) {
				TextView errorText = new TextView(this);
				errorText.setText(error);
				errorListLayout.addView(errorText, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			}
		
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
