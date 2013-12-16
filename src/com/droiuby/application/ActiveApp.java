package com.droiuby.application;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;

public class ActiveApp implements Serializable {

	public static final int ASSET_TYPE_SCRIPT = 0;
	public static final int ASSET_TYPE_LIB = 4;
	public static final int ASSET_TYPE_CSS = 3;
	public static final int ASSET_TYPE_BINARY = 5;
	public static final int ASSET_TYPE_VENDOR = 6;
	
	private static final long serialVersionUID = 4120098422645102827L;
	
	String name, description, baseUrl, mainUrl, launchUrl, framework;
	boolean isFullScreen;

	public String getLaunchUrl() {
		return launchUrl;
	}

	public void setLaunchUrl(String launchUrl) {
		this.launchUrl = launchUrl;
	}

	public boolean isFullScreen() {
		return isFullScreen;
	}

	public void setFullScreen(boolean isFullScreen) {
		this.isFullScreen = isFullScreen;
	}

	HashMap <String, Integer> assets = new HashMap <String, Integer>();
	
	public int getInitiallOrientation() {
		return initiallOrientation;
	}

	public void setInitiallOrientation(int initiallOrientation) {
		this.initiallOrientation = initiallOrientation;
	}

	int initiallOrientation;

	public String getMainUrl() {
		return mainUrl;
	}

	public void setMainUrl(String mainUrl) {
		this.mainUrl = mainUrl;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void addAsset(String name, int type) {
		this.assets.put(name, type);
	}

	public HashMap<String, Integer> getAssets() {
		return assets;
	}

	public void setAssets(HashMap<String, Integer> assets) {
		this.assets = assets;
	}

	public String getFramework() {
		return framework;
	}

	public void setFramework(String framework) {
		this.framework = framework;
	}
	
	public SharedPreferences getCurrentPreferences(Context c) {
		try {
			SharedPreferences prefs = null;
			if (getBaseUrl().startsWith("asset:")) {
				String asset_name = "data_" + getBaseUrl();
				asset_name = asset_name.replace('/', '_').replace('\\', '_');
				prefs = c.getSharedPreferences(asset_name,
						Context.MODE_PRIVATE);
			} else {
				URL parsedURL = new URL(getBaseUrl());
				prefs = c.getSharedPreferences(
						"data_" + parsedURL.getProtocol() + "_"
								+ parsedURL.getHost(), Context.MODE_PRIVATE);
			}
			return prefs;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
