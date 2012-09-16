package com.dayosoft.activeapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dayosoft.activeapp.core.ActiveApp;
import com.dayosoft.activeapp.core.DroiubyActivity;
import com.dayosoft.activeapp.utils.ActiveAppDownloader;

public class AppDownloader extends AsyncTask<Void, Void, ActiveApp> {
	String url;
	Context c;
	private ProgressDialog progress_dialog;
	Class activityClass;

	public AppDownloader(Context c, String url, Class activityClass) {
		this.c = c;
		this.url = url;
		this.activityClass = activityClass;
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		progress_dialog = ProgressDialog.show(c, "",
				"Querying application configuration...", true);
	}

	@Override
	protected ActiveApp doInBackground(Void... params) {
		try {
		return ActiveAppDownloader.loadApp(c, url);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	protected void onPostExecute(ActiveApp result) {
		super.onPostExecute(result);
		
		if (progress_dialog != null) {
			progress_dialog.dismiss();
		}
		if (result != null) {
			Intent intent = new Intent(c, activityClass);
			intent.putExtra("application", result);
			c.startActivity(intent);
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(c);
			builder.setMessage("Unable to download access app at " + url)
					.setCancelable(true).create();
		}
		
	}

}