package com.dayosoft.activeapp.core;

import org.jruby.embed.ScriptingContainer;

import android.app.Activity;

public class RubyContainerPayload {
	Activity currentActivity;
	ActivityBuilder activityBuilder;

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
