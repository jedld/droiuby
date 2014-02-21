package com.droiuby.application.activities;

import android.app.Activity;
import android.os.Bundle;

import com.droiuby.application.DroiubyBootstrap;
import com.droiuby.application.LibraryBootstrapTask;
import com.droiuby.application.OnEnvironmentReady;
import com.droiuby.application.R;
import com.droiuby.interfaces.DroiubyHelperInterface;
import com.droiuby.launcher.Options;

public class StartupActivity extends Activity implements OnEnvironmentReady {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LibraryBootstrapTask library = DroiubyBootstrap.bootstrapEnvironment(
				this, this);
		setContentView(R.layout.canvas);
		library.execute();
	}

	public void onReady(DroiubyHelperInterface result) {
		
		Options options = new Options();
		options.setOverwrite(false);
		options.setNewActivity(true);
		options.setCloseParentActivity(true);
		
		result.launch(this, "asset:launcher.zip", DroiubyActivity.class, options);
	}
	
}
