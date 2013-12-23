package com.droiuby.application;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.util.Log;
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

import com.droiuby.callbacks.OnRefreshRequested;
import com.droiuby.interfaces.DroiubyHelperInterface;

public class CanvasActivity extends Activity implements OnEnvironmentReady, OnRefreshRequested,
		SensorEventListener {

	RelativeLayout topview;
	DroiubyHelperInterface droiuby;
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private SharedPreferences prefs;
//	private IntentFilter[] intentFiltersArray;
//	private String[][] techListsArray;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LibraryBootstrapTask library = DroiubyBootstrap.bootstrapEnvironment(
				this, this);
		setContentView(R.layout.canvas);
		library.execute();

		
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		
		prefs = getSharedPreferences("bootstrap",
				Context.MODE_PRIVATE);
		
//		PendingIntent pendingIntent = PendingIntent.getActivity(
//			    this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
//		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
//	    try {
//	        ndef.addDataType("*/*");    /* Handles all MIME based dispatches.
//	                                       You should specify only the ones that you need. */
//	    }
//	    catch (MalformedMimeTypeException e) {
//	        throw new RuntimeException("fail", e);
//	    }
//	   intentFiltersArray = new IntentFilter[] {ndef, };
//	   techListsArray = new String[][] { new String[] { NfcF.class.getName(), NfcA.class.getName(), NfcB.class.getName() } };
	   
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if (droiuby != null) {
			droiuby.onStart();
		}
	}

	public void refreshCurrentApplication() {
		if (droiuby != null) {
			ViewGroup view = (ViewGroup) findViewById(R.id.mainLayout);
			view.removeAllViews();
			droiuby.reloadApplication(R.id.mainLayout);
		}
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
			for (String error : droiuby.getScriptErrors()) {
				TextView errorText = new TextView(this);
				errorText.setText(error);
				errorListLayout.addView(errorText, LayoutParams.MATCH_PARENT,
						LayoutParams.WRAP_CONTENT);
			}
		} else if (itemId == R.id.itemClearCache) {
			droiuby.clearCache();
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.parseroptions, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (droiuby != null) {
			droiuby.onDestroy();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (droiuby != null) {
			droiuby.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		mSensorManager.registerListener(this, mSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
		if (droiuby != null) {
			droiuby.onResume();
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mSensorManager.unregisterListener(this);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	}
	
	public void onDownloadComplete(ActiveApp app) {
		Log.d(this.getClass().toString(), "app " + app.getName()
				+ " download complete");
	}

	public void onReady(DroiubyHelperInterface result) {
		Bundle params = this.getIntent().getExtras();
		droiuby = result;
		if (params != null) {
			droiuby.onIntent(params);
		} else {
			String autostart = prefs.getString("autostart", null);
			if (autostart == null) {
			   droiuby.startDefault();
			} else {
			   droiuby.start(autostart);
			}
		}
	}

	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		if (droiuby != null) {
			droiuby.onSensorChanged(event);
		}
	}

}
