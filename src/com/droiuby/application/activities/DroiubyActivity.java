package com.droiuby.application.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.droiuby.application.DroiubyBootstrap;
import com.droiuby.application.R;
import com.droiuby.interfaces.DroiubyHelperInterface;

public class DroiubyActivity extends Activity {
	
	DroiubyHelperInterface helper;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle params = this.getIntent().getExtras();
		String bundleName = params.getString("bundle");
		String pageUrl = params.getString("pageUrl");
		
		helper = DroiubyBootstrap.getHelperInstance();
		helper.runController(this, bundleName, pageUrl);
		helper.setExecutionBundle(this, bundleName);
	}

	@Override
	protected void onResume() {
		super.onResume();
		helper.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		helper.onActivityResult(requestCode, resultCode, data);
	}
	
}
