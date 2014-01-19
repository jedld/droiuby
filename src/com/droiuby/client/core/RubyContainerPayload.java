package com.droiuby.client.core;

import org.jruby.embed.ScriptingContainer;

import com.droiuby.application.DroiubyApp;
import com.droiuby.client.core.builder.ActivityBuilder;

import android.app.Activity;

public class RubyContainerPayload {
	Activity currentActivity;
	ActivityBuilder activityBuilder;
	DroiubyApp activeApp;
	
	public DroiubyApp getActiveApp() {
		return activeApp;
	}

	public void setActiveApp(DroiubyApp activeApp) {
		this.activeApp = activeApp;
	}

	ExecutionBundle executionBundle;

	public ExecutionBundle getExecutionBundle() {
		return executionBundle;
	}

	public void setExecutionBundle(ExecutionBundle executionBundle) {
		this.executionBundle = executionBundle;
	}

	static RubyContainerPayload instance;

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
