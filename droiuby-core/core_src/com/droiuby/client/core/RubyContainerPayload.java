package com.droiuby.client.core;

import org.jruby.embed.ScriptingContainer;

import com.droiuby.application.bootstrap.DroiubyApp;
import com.droiuby.client.core.builder.ActivityBuilder;

import android.app.Activity;

public class RubyContainerPayload {
	
	Activity currentActivity;
	ActivityBuilder activityBuilder;
	DroiubyApp activeApp;
	PageAsset currentPage;
	
	static RubyContainerPayload instance;

	public PageAsset getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(PageAsset currentPage) {
		this.currentPage = currentPage;
	}

	public DroiubyApp getActiveApp() {
		return activeApp;
	}

	public void setDroiubyApp(DroiubyApp activeApp) {
		this.activeApp = activeApp;
	}

	ExecutionBundle executionBundle;

	public ExecutionBundle getExecutionBundle() {
		return executionBundle;
	}

	public void setExecutionBundle(ExecutionBundle executionBundle) {
		this.executionBundle = executionBundle;
	}



	public RubyContainerPayload() {

	}

	public Activity getCurrentActivity() {
		return currentActivity;
	}

	public void setCurrentActivity(Activity currentActivity) {
		this.currentActivity = currentActivity;
	}

	public ActivityBuilder getActivityBuilder() {
		return activityBuilder;
	}

	public void setActivityBuilder(ActivityBuilder activityBuilder) {
		this.activityBuilder = activityBuilder;
	}

	public ScriptingContainer getContainer() {
		return container;
	}

	public void setContainer(ScriptingContainer container) {
		this.container = container;
	}

	ScriptingContainer container;
}
