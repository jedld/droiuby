package com.droiuby.client.core;

import java.util.ArrayList;

import org.jruby.embed.EmbedEvalUnit;

import com.droiuby.client.core.builder.ActivityBuilder;

public class PageAsset {

	ArrayList<Object> assets;
	ActivityBuilder builder;
	EmbedEvalUnit preParsedScript;
	String controllerClass;
	String url;
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	ExecutionBundle bundle;
	
	public ExecutionBundle getBundle() {
		return bundle;
	}

	public void setBundle(ExecutionBundle bundle) {
		this.bundle = bundle;
	}

	public EmbedEvalUnit getPreParsedScript() {
		return preParsedScript;
	}

	public void setPreParsedScript(EmbedEvalUnit preParsedScript) {
		this.preParsedScript = preParsedScript;
	}
	
	public String getControllerClass() {
		return controllerClass;
	}

	public void setControllerClass(String controllerClass) {
		this.controllerClass = controllerClass;
	}

	public ArrayList<Object> getAssets() {
		return assets;
	}
	
	public void setAssets(ArrayList<Object> assets) {
		this.assets = assets;
	}
	
	public ActivityBuilder getBuilder() {
		return builder;
	}
	
	public void setBuilder(ActivityBuilder builder) {
		this.builder = builder;
	}
}
