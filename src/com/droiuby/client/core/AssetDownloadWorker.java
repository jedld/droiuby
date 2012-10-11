package com.droiuby.client.core;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import android.app.Activity;
import android.os.Environment;

import com.droiuby.client.utils.Utils;

public class AssetDownloadWorker implements Runnable {
	
	Activity targetActivity;
	ActiveApp activeApp;
	ExecutionBundle bundle;
	String assetName;
	int method;
	Vector <Object>resultBundle;
	AssetDownloadCompleteListener onCompleteListener;
	
	public AssetDownloadWorker(Activity targetActivity, ActiveApp activeApp, ExecutionBundle bundle, 
			String assetName, Vector <Object>resultBundle, AssetDownloadCompleteListener onCompleteListener, int method) {
		this.targetActivity = targetActivity;
		this.activeApp = activeApp;
		this.bundle = bundle;
		this.assetName = assetName;
		this.method = method;
		this.resultBundle = resultBundle;
		this.onCompleteListener = onCompleteListener;
	}
	
	public String loadAsset() {
		if (assetName != null) {
			if (assetName.startsWith("asset:")) {
				return Utils.loadAsset(targetActivity, assetName);
			} else {
				if (activeApp.getBaseUrl().indexOf("asset:") != -1) {
					return Utils
							.loadAsset(targetActivity, activeApp.getBaseUrl() + assetName);
				} else if (activeApp.getBaseUrl().indexOf("file:") != -1) {
					return Utils.loadFile(assetName);
				} else if (activeApp.getBaseUrl().indexOf("sdcard:") != -1) {
					File directory = Environment.getExternalStorageDirectory();
					try {
						String asset_path = directory.getCanonicalPath()
								+ assetName;
						return Utils.loadFile(asset_path);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return null;
				} else {
					String baseUrl = activeApp.getBaseUrl();
					
					if (assetName.startsWith("/")) {
						assetName = assetName.substring(1);
					}

					if (activeApp.getBaseUrl().endsWith("/")) {
						baseUrl = activeApp.getBaseUrl().substring(0, activeApp.getBaseUrl().length() - 1);
					}

					return Utils.query(baseUrl + "/" + assetName,
							targetActivity, activeApp.getName(), method);
				}
			}
		} else {
			return null;
		}
	}
	
	
	public void run() {
		onCompleteListener.onComplete(bundle, assetName, resultBundle.add(loadAsset()));
		this.notifyAll();
	}

}
