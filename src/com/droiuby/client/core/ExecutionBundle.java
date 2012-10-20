package com.droiuby.client.core;

import java.util.ArrayList;

import org.jruby.embed.ScriptingContainer;

import android.app.Activity;

import com.droiuby.client.core.interfaces.OnUrlChangedListener;

public class ExecutionBundle {
	
	ScriptingContainer container;
	OnUrlChangedListener urlChangedListener;
	Activity currentActivity;
	String currentUrl;
	boolean libraryInitialized = false;	
	
	public Activity getCurrentActivity() {
		return currentActivity;
	}

	public void setCurrentActivity(Activity currentActivity) {
		this.currentActivity = currentActivity;
	}
	
	public boolean isLibraryInitialized() {
		return libraryInitialized;
	}

	public void setLibraryInitialized(boolean libraryInitialized) {
		this.libraryInitialized = libraryInitialized;
	}

	ArrayList<String> scriptErrors = new ArrayList<String>();

	public ArrayList<String> getScriptErrors() {
		return scriptErrors;
	}
	
	public void clearErrors() {
		scriptErrors.clear();
	}

	public void addError(String errorMessage) {
		scriptErrors.add(errorMessage);
	}
	
	public void setScriptErrors(ArrayList<String> scriptErrors) {
		this.scriptErrors = scriptErrors;
	}

	public String getCurrentUrl() {
		return currentUrl;
	}

	public void setCurrentUrl(String currentUrl) {
		this.currentUrl = currentUrl;
	}

	public OnUrlChangedListener getUrlChangedListener() {
		return urlChangedListener;
	}

	public void setUrlChangedListener(OnUrlChangedListener urlChangedListener) {
		this.urlChangedListener = urlChangedListener;
	}

	public ScriptingContainer getContainer() {
		return container;
	}

	public void setContainer(ScriptingContainer container) {
		this.container = container;
	}

	public RubyContainerPayload getPayload() {
		return payload;
	}

	public void setPayload(RubyContainerPayload payload) {
		this.payload = payload;
	}

	RubyContainerPayload payload;
}
