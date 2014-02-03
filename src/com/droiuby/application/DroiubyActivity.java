package com.droiuby.application;

import com.droiuby.client.core.ExecutionBundle;
import com.droiuby.client.core.ExecutionBundleFactory;

import android.app.Activity;
import android.os.Bundle;

public class DroiubyActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle params = this.getIntent().getExtras();
		String bundleName = params.getString("executionBundle");
		ExecutionBundle bundle = ExecutionBundleFactory.getBundle(bundleName);
	}
}
