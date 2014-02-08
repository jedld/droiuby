package com.droiuby.application;

import android.app.Activity;
import android.os.Bundle;

import com.droiuby.interfaces.DroiubyHelperInterface;

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
