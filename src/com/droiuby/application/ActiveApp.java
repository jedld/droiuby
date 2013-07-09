package com.droiuby.application;

import java.io.Serializable;
import java.util.HashMap;

public class ActiveApp implements Serializable {

	public static final int ASSET_TYPE_SCRIPT = 0;
	public static final int ASSET_TYPE_LIB = 4;
	public static final int ASSET_TYPE_CSS = 3;
	
	private static final long serialVersionUID = 4120098422645102827L;
	
	String name, description, baseUrl, mainUrl, framework;

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
	

}
