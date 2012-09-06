package com.dayosoft.activeapp.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.EvalFailedException;
import org.jruby.embed.ScriptingContainer;
import org.jruby.javasupport.JavaObject;
import org.jruby.runtime.builtin.IRubyObject;

import com.dayosoft.activeapp.R;
import com.dayosoft.activeapp.core.ActiveApp;
import com.dayosoft.activeapp.core.ActivityBuilder;
import com.dayosoft.activeapp.core.AppCache;
import com.dayosoft.activeapp.core.ExecutionBundle;
import com.dayosoft.activeapp.core.OnDownloadCompleteListener;
import com.dayosoft.activeapp.core.RubyContainerPayload;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ActiveAppDownloader extends AsyncTask<Void, Void, Boolean> {

	String baseUrl;
	Activity targetActivity;
	ActiveApp app;
	Element rootElem;
	Document mainActivityDocument;
	String controller;
	ActivityBuilder builder;
	ScriptingContainer scriptingContainer;
	RubyContainerPayload payload;
	ArrayList<EmbedEvalUnit> evalUnits = new ArrayList<EmbedEvalUnit>();
	OnDownloadCompleteListener listener;

	public AppCache getCache() {
		AppCache cache = new AppCache();
		cache.setMainActivityDocument(mainActivityDocument);
		cache.setEvalUnits(evalUnits);
		return cache;
	}

	SAXBuilder sax = new SAXBuilder();

	public ActiveAppDownloader(ActiveApp app, Activity targetActivity,
			ViewGroup target, AppCache cache, ExecutionBundle executionBundle, OnDownloadCompleteListener listener) {
		this.targetActivity = targetActivity;
		this.app = app;
		this.baseUrl = app.getBaseUrl();
		this.scriptingContainer = executionBundle.getContainer();
		this.payload = executionBundle.getPayload();
		this.listener = listener;
		if (cache != null) {
			this.mainActivityDocument = cache.getMainActivityDocument();
			this.evalUnits = cache.getEvalUnits();
		}
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		Toast.makeText(targetActivity, "Loading app", Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		return super.equals(o);
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}

	public String loadAsset(String asset_name) {
		if (baseUrl.indexOf("asset:") != -1) {
			return Utils.loadAsset(targetActivity, baseUrl + asset_name);
		} else if (baseUrl.indexOf("file:") != -1) {
			return Utils.loadFile(asset_name);
		} else if (baseUrl.indexOf("sdcard:") != -1) {
			File directory = Environment.getExternalStorageDirectory();
			try {
				String asset_path = directory.getCanonicalPath() + asset_name;
				return Utils.loadFile(asset_path);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		} else {
			if (asset_name.startsWith("/")) {
				asset_name = asset_name.substring(1);
			}
			return Utils.query(baseUrl + "/" +asset_name, targetActivity);
		}
	}

	public static ActiveApp loadApp(Context c, String url) {
		String responseBody = null;
		Log.d(ActiveAppDownloader.class.toString(), "loading " + url);
		if (url.indexOf("asset:") != -1) {
			responseBody = Utils.loadAsset(c, url);
		} else {
			responseBody = Utils.query(url, c);
		}
		if (responseBody != null) {
			Log.d(ActiveAppDownloader.class.toString(), responseBody);
			SAXBuilder sax = new SAXBuilder();
			Document doc;
			try {
				doc = sax.build(new StringReader(responseBody));
				Element rootElem = doc.getRootElement();
				String appName = rootElem.getChild("name").getText();
				String appDescription = rootElem.getChild("description")
						.getText();
				String baseUrl = rootElem.getChildText("base_url");
				String mainActivity = rootElem.getChildText("main");
				ActiveApp app = new ActiveApp();
				app.setDescription(appDescription);
				app.setName(appName);
				app.setBaseUrl(baseUrl);
				app.setMainUrl(mainActivity);
				return app;
			} catch (JDOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public Boolean download() {

		try {
			if (this.mainActivityDocument == null) {
				String responseBody = loadAsset(app.getMainUrl());
				Log.d(this.getClass().toString(), responseBody);
				mainActivityDocument = sax
						.build(new StringReader(responseBody));
			}
			builder = new ActivityBuilder(mainActivityDocument, targetActivity);
			Log.d(this.getClass().toString(), "setting activity "
					+ targetActivity);

			payload.setActivityBuilder(builder);

			try {
				AssetManager manager = targetActivity.getAssets();
				scriptingContainer.parse(manager.open("lib/loader.rb"),
						"lib/loader.rb").run();
				scriptingContainer.parse(manager.open("lib/bootstrap.rb"),
						"lib/bootstrap.rb").run();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			controller = mainActivityDocument.getRootElement()
					.getAttributeValue("controller");
			if (controller != null) {
				Log.d(this.getClass().toString(), "loading controller file "
						+ baseUrl + controller);
				String controller_content = "class MainActivity < ActivityWrapper\n"
						+ loadAsset(controller) + "\n end\n";

				evalUnits.add(Utils.preParseRuby(scriptingContainer,
						controller_content, targetActivity));
			}
			return true;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	protected Boolean doInBackground(Void... arg0) {
		// TODO Auto-generated method stub
		return download();
	}

	@Override
	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);

		builder.build();

		try {
			for (EmbedEvalUnit evalUnit : evalUnits) {
				long start = System.currentTimeMillis();
				evalUnit.run();
				long elapsed = System.currentTimeMillis() - start;
				Log.d(this.getClass().toString(),
						"ruby segment: elapsed time = " + elapsed + "ms");
			}
			if (controller != null) {
				long start = System.currentTimeMillis();
				scriptingContainer
						.runScriptlet("$main_activty = MainActivity.new; $main_activty.on_create");
				long elapsed = System.currentTimeMillis() - start;
				Log.d(this.getClass().toString(),
						"ruby segment: on_create() elapsed time = " + elapsed + "ms");
			}
		} catch (EvalFailedException e) {
			Log.e(this.getClass().toString(), e.getMessage());
		}

	}

}
