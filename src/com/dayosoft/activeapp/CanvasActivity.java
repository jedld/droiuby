package com.dayosoft.activeapp;

import com.dayosoft.activeapp.core.ActiveApp;
import com.dayosoft.activeapp.core.DroiubyActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;

public class CanvasActivity extends DroiubyActivity {
	
	ViewGroup target;
	ActiveApp application;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.canvas);
		Bundle params = this.getIntent().getExtras();
		application = (ActiveApp) params.getSerializable("application");
		target = (ViewGroup) this.findViewById(R.id.mainLayout);
		
		setupApplication(application, target);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.itemRefresh:
			setupApplication(application, target);
			break;
		case R.id.itemConsole:

		}
		return false;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemRefresh:
			setupApplication(application, target);
			break;
		}
		return false;
	}

}
