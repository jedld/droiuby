package com.droiuby.client.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.os.Environment;

import com.droiuby.application.DroiubyApp;
import com.droiuby.client.core.utils.Utils;

public class AssetDownloadWorker implements Runnable {

	Context context;
	DroiubyApp activeApp;
	ExecutionBundle bundle;
	String assetName;
	int method, assetType;
	ArrayList<Object> resultBundle;
	AssetDownloadCompleteListener onCompleteListener;

	public AssetDownloadWorker(Context context, DroiubyApp activeApp,
			ExecutionBundle bundle, String assetName, int assetType,
			ArrayList<Object> resultBundle,
			AssetDownloadCompleteListener onCompleteListener, int method) {
		this.context = context;
		this.activeApp = activeApp;
		this.bundle = bundle;
		this.assetName = assetName;
		this.method = method;
		this.resultBundle = resultBundle;
		this.onCompleteListener = onCompleteListener;
		this.assetType = assetType;
	}

	public String loadAsset() {
		if (assetName != null) {
			if (assetName.startsWith("asset:")) {
				try {
					return Utils.loadAsset(context, assetName);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				if (activeApp.getBaseUrl().indexOf("asset:") != -1) {
					try {
						return Utils.loadAsset(context, activeApp.getBaseUrl()
								+ assetName);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (activeApp.getBaseUrl().indexOf("file:") != -1) {
					return Utils.loadFile(assetName);
				} else if (activeApp.getBaseUrl().indexOf("sdcard:") != -1) {
					File directory = Environment.getExternalStorageDirectory();
					try {
						String asset_path = directory.getCanonicalPath()
								+ assetName;
						return Utils.loadFile(asset_path);
					} catch (IOException e) {
						e.printStackTrace();
					}
					return null;
				} else {
					String baseUrl = activeApp.getBaseUrl();

					if (assetName.startsWith("/")) {
						assetName = assetName.substring(1);
					}

					if (activeApp.getBaseUrl().endsWith("/")) {
						baseUrl = activeApp.getBaseUrl().substring(0,
								activeApp.getBaseUrl().length() - 1);
					}

					return Utils.query(baseUrl + "/" + assetName, context,
							activeApp.getName(), method);
				}
			}
		} else {
			return null;
		}
		return null;
	}

	public void run() {
		if (onCompleteListener != null) {
			Object result = null;
			try {
				result = onCompleteListener.onComplete(bundle, assetName,
						Utils.loadAppAsset(activeApp, context, assetName,
								assetType, method));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (result != null) {
				resultBundle.add(result);
			}
		}
	}

}
