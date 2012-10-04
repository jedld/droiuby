package com.droiuby.client.core;

import java.io.Serializable;

public class ActiveApp implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4120098422645102827L;

	String name, description, baseUrl, mainUrl;
	
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
}
