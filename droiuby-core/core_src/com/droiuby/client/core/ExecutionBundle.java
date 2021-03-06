package com.droiuby.client.core;

import java.util.ArrayList;
import java.util.HashMap;

import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;

import com.droiuby.client.core.interfaces.OnUrlChangedListener;
import com.droiuby.launcher.ExecutionBundleInterface;

public class ExecutionBundle implements ExecutionBundleInterface {

	ScriptingContainer container;
	OnUrlChangedListener urlChangedListener;
	String currentUrl;
	IRubyObject currentController;
	String name;
	boolean isRootBUndle;
	
	public boolean isRootBundle() {
		return isRootBUndle;
	}

	public void setRootBundle(boolean launcherInstance) {
		this.isRootBUndle = launcherInstance;
	}

	HashMap <String,PageAsset> pageAssets = new HashMap<String, PageAsset>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public IRubyObject getCurrentController() {
		return currentController;
	}

	public void setCurrentController(IRubyObject currentController) {
		this.currentController = currentController;
	}

	boolean libraryInitialized = false;

	public Activity getCurrentActivity() {
		return this.getPayload().getCurrentActivity();
	}

	public void setCurrentActivity(Activity currentActivity) {
		this.getPayload().setCurrentActivity(currentActivity);
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

	public SensorManager getSensor(int type) {
		SensorManager mSensorManager = (SensorManager) this.getCurrentActivity()
				.getSystemService(Context.SENSOR_SERVICE);
		return mSensorManager;
	}

	RubyContainerPayload payload;
	
	public void addPageAsset(String name, PageAsset pageAsset) {
		this.pageAssets.put(name, pageAsset);
	}
	
	public PageAsset getPage(String name) {
		return this.pageAssets.get(name);
	}

	public void terminate() {
		container.terminate();
		ExecutionBundleFactory factory = ExecutionBundleFactory.getInstance(Thread.currentThread().getContextClassLoader());
		factory.removeBundle(getName());
	}
}
