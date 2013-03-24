package com.droiuby.interfaces;

import org.jdom2.Document;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ViewGroup;

import com.droiuby.application.ActiveApp;
import com.droiuby.client.core.ExecutionBundle;

public interface DroiubyHelperInterface {

	public void setActivity(Activity activity);
	
	public abstract ExecutionBundle getExecutionBundle();

	public abstract void setExecutionBundle(ExecutionBundle executionBundle);

	public abstract void onIntent(Bundle params);

	public abstract void reloadApplication(ActiveApp application, int mainlayout);

	public abstract SharedPreferences getCurrentPreferences();

	public abstract String getIpAddr();

	public abstract void showConsoleInfo();

	public abstract void setupApplication(ActiveApp application,
			ViewGroup target, int resId);

	public abstract void onStart();

	public abstract void onDestroy();

	public abstract void onResume();

	public abstract Object onRetainNonConfigurationInstance();

	public abstract void setActiveApp(ActiveApp application);

	public abstract void onActivityResult(int requestCode, int resultCode,
			Intent intent);

	public abstract void start(String url);

	public abstract void onDownloadComplete(ActiveApp app);

	public abstract void onDocumentReady(Document mainActivity);

}