package com.droiuby.interfaces;

import java.util.ArrayList;

import org.jdom2.Document;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorEvent;
import android.os.Bundle;

import com.droiuby.application.DroiubyApp;

public interface DroiubyHelperInterface {

	public void setActivity(Activity activity);

	public void onIntent(Bundle params);

	public void clearCache();
	
	public SharedPreferences getCurrentPreferences();

	public String getIpAddr();

	public void showConsoleInfo();

	public void onStart();

	public void onDestroy();

	public void onResume();

	public void setActiveApp(DroiubyApp application);

	public void onActivityResult(int requestCode, int resultCode, Intent intent);

	public void start(String url);
	
	public void startDefault();

	public void onDocumentReady(Document mainActivity);

	public ArrayList<String> getScriptErrors();

	public void setCurrentUrl(String currentUrl);

	public void setLibraryInitialized(boolean b);
	
	public void setLoader(ClassLoader loader);

	public void onSensorChanged(SensorEvent arg0);
	
	public void launch(Context context, String url, Class<?> activityClass);
	
	public void setPage(Activity activity, String bundleName, String pageUrl);
	
	public void runController(Activity activity, String bundleName, String pageUrl);

}