package com.dayosoft.activeapp;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jruby.CompatVersion;
import org.jruby.RubyInstanceConfig.CompileMode;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;

import com.dayosoft.activeapp.core.AppCache;
import com.dayosoft.activeapp.core.ExecutionBundle;
import com.dayosoft.activeapp.core.RubyContainerPayload;
import com.dayosoft.activeapp.utils.ActiveAppDownloader;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class CanvasActivity extends Activity {
	/** Called when the activity is first created. */
	String applicationUrl;
	AppCache cache;
	ExecutionBundle executionBundle;
	ActiveAppDownloader downloader;
	private WebConsole console;

	private ExecutionBundle getNewScriptingContainer() {
		ExecutionBundle bundle = new ExecutionBundle();
		ScriptingContainer container = new ScriptingContainer(
				LocalVariableBehavior.PERSISTENT);
		RubyContainerPayload payload = new RubyContainerPayload();

		payload.setCurrentActivity(this);
		payload.setContainer(container);

		container.setCompatVersion(CompatVersion.RUBY1_9);
		container.setCompileMode(CompileMode.OFF);
		try {
			container.setHomeDirectory(this.getCacheDir().getCanonicalPath()
					+ "/jruby/home");
			List<String> loadPaths = new ArrayList();
			loadPaths.add(this.getCacheDir().getCanonicalPath()
					+ "/jruby/vendor");
			loadPaths.add(this.getCacheDir().getCanonicalPath()
					+ "/jruby/vendor/lib");
			container.setLoadPaths(loadPaths);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		container.put("container_payload", payload);
		bundle.setContainer(container);
		bundle.setPayload(payload);
		return bundle;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.canvas);
		Bundle params = this.getIntent().getExtras();
		applicationUrl = params.getString("url");
		Log.d(this.getClass().toString(), "Loading application at "
				+ applicationUrl);
		final AppCache cache = (AppCache) getLastNonConfigurationInstance();

		executionBundle = getNewScriptingContainer();

		downloader = new ActiveAppDownloader(applicationUrl, this, cache,
				executionBundle);

		downloader.execute();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.parseroptions, menu);
		return true;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		console.stop();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		setupConsole();
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.itemRefresh:

			executionBundle = getNewScriptingContainer();

			downloader = new ActiveAppDownloader(applicationUrl, this, null,
					executionBundle);
			downloader.execute();
			break;
		case R.id.itemConsole:

		}
		return false;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemRefresh:
			executionBundle = getNewScriptingContainer();
			downloader = new ActiveAppDownloader(applicationUrl, this, null,
					executionBundle);
			downloader.execute();
			break;
		}
		return false;
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return downloader.getCache();
	}

	private void setupConsole() {
		String web_public_loc;
		try {
			web_public_loc = this.getCacheDir().getCanonicalPath() + "/www";
			File webroot = new File(web_public_loc);
			webroot.mkdirs();
			console = new WebConsole(4000, webroot, this,
					executionBundle.getContainer());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}