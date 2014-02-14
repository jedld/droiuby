package com.droiuby.application;

import android.app.Activity;
import android.os.Bundle;

import com.droiuby.interfaces.DroiubyHelperInterface;

public class StartupActivity extends Activity implements OnEnvironmentReady {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LibraryBootstrapTask library = DroiubyBootstrap.bootstrapEnvironment(
				this, this);
		setContentView(R.layout.canvas);
		library.execute();
	}

	public void onReady(DroiubyHelperInterface result) {
		result.launch(this, "asset:launcher.zip", DroiubyActivity.class, false);
	}
	
}
