package com.droiuby.client.core;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.droiuby.application.DroiubyApp;
import com.droiuby.application.R;
import com.droiuby.callbacks.OnAppDownloadComplete;
import com.droiuby.client.core.utils.ActiveAppDownloader;

public class AppDownloader extends AsyncTask<Void, String, DroiubyApp> {
	String url;
	Activity c;
	Class activityClass;
	OnAppDownloadComplete onDownloadComplete;

	public AppDownloader(Activity c, String url, Class activityClass,
			OnAppDownloadComplete listener) {
		this.c = c;
		this.url = url;
		this.activityClass = activityClass;
		this.onDownloadComplete = listener;
		Log.d(this.getClass().toString(), "app downloader with listener");
	}

	public AppDownloader(Activity c, String url, Class activityClass) {
		this.c = c;
		this.url = url;
		this.activityClass = activityClass;
	}

	
	protected void onProgressUpdate(String... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
		TextView view = (TextView)c.findViewById(R.id.loadingStatusText);
		if (view!=null) {
			view.setText(values[0]);
		}
	}
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
	}

	@Override
	protected DroiubyApp doInBackground(Void... params) {
		Log.d(this.getClass().toString(),"Loading app descriptor ...");
		publishProgress("loading app");
		try {
			return ActiveAppDownloader.loadApp(c, url);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	protected void onPostExecute(DroiubyApp result) {
		super.onPostExecute(result);
		if (this.onDownloadComplete != null) {
			Log.d(this.getClass().toString(),"Invoking onDownloadComplete");
			onDownloadComplete.onDownloadComplete(result);
		} else {
			if (result != null) {
				Log.d(this.getClass().toString(), "starting a new activity ....");
				Intent intent = new Intent(c, activityClass);
				intent.putExtra("application", result);
				intent.putExtra("fullscreen", result.isFullScreen());
				c.startActivity(intent);
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(c);
				builder.setMessage("Unable to download access app at " + url)
						.setCancelable(true).create();
			}
		}
	}

}