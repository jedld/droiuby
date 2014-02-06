package com.droiuby.application;

import com.droiuby.client.core.ExecutionBundle;
import com.droiuby.client.core.ExecutionBundleFactory;
import com.droiuby.client.core.PageAsset;
import com.droiuby.interfaces.DroiubyHelperInterface;

import android.app.Activity;
import android.os.Bundle;

public class DroiubyActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.canvas);
		Bundle params = this.getIntent().getExtras();
		String bundleName = params.getString("bundle");
		String pageUrl = params.getString("pageUrl");
		DroiubyHelperInterface helper = DroiubyBootstrap.getHelperInstance();
		helper.runController(this, bundleName, pageUrl);
	}
}
