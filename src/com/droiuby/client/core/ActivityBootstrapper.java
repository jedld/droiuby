package com.droiuby.client.core;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.JDOMParseException;
import org.jdom2.input.SAXBuilder;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.EvalFailedException;
import org.jruby.embed.ParseFailedException;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.droiuby.application.ActiveApp;
import com.droiuby.callbacks.DocumentReadyListener;
import com.droiuby.client.core.utils.Utils;

class ActivityBootstrapper extends AsyncTask<Void, Void, ActivityBuilder> {

	ActiveApp app;
	Activity targetActivity;
	Document mainActivityDocument;
	ScriptingContainer scriptingContainer;
	String baseUrl;
	String controllerClass;
	String controller_attribute;
	String pageUrl;
	String errorMsg;
	View preparedViews;
	int resId;

	ExecutionBundle executionBundle;

	DocumentReadyListener onReadyListener;
	private EmbedEvalUnit preParsedScript;
	ArrayList<Object> resultBundle;
	SAXBuilder sax = new SAXBuilder();
	int method;

	public ActivityBootstrapper(ExecutionBundle executionBundle, ActiveApp app,
			String pageUrl, int method, int resId, Activity targetActivity,
			Document cachedActivityDocument,
			DocumentReadyListener onReadyListener) {
		this.app = app;
		this.pageUrl = pageUrl;
		this.executionBundle = executionBundle;
		this.targetActivity = targetActivity;
		this.baseUrl = app.getBaseUrl();
		this.scriptingContainer = executionBundle.getContainer();
		this.mainActivityDocument = cachedActivityDocument;
		this.onReadyListener = onReadyListener;
		this.method = method;
		this.resId = resId;
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		targetActivity.setRequestedOrientation(app.getInitiallOrientation());
	}

	@Override
	protected ActivityBuilder doInBackground(Void... params) {
		String responseBody = (String) Utils.loadAppAsset(app, targetActivity,
				pageUrl, Utils.ASSET_TYPE_TEXT, method);
		if (responseBody != null) {
			// Log.d(this.getClass().toString(), responseBody);
		} else {
			Log.d(this.getClass().toString(), "response empty.");
			return null;
		}
		try {
			if (mainActivityDocument == null) {
				mainActivityDocument = sax
						.build(new StringReader(responseBody));
			}
		} catch (JDOMParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			responseBody = "<activity><t>" + e.getMessage() + "</t></activity>";
			try {
				mainActivityDocument = sax
						.build(new StringReader(responseBody));
			} catch (JDOMException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		controller_attribute = mainActivityDocument.getRootElement()
				.getAttributeValue("controller");
		String baseUrl = app.getBaseUrl();

		if (controller_attribute != null) {
			String csplit[] = org.apache.commons.lang3.StringUtils.split(
					controller_attribute, "#");
			if (csplit.length == 2) {

				if (!csplit[1].trim().equals("")) {
					controllerClass = csplit[1];
				}

				if (!csplit[0].trim().equals("")) {
					Log.d("Activity loader", "loading controller file "
							+ baseUrl + csplit[0]);
					String controller_content = (String) Utils.loadAppAsset(
							app, targetActivity, csplit[0],
							Utils.ASSET_TYPE_TEXT, Utils.HTTP_GET);

					long start = System.currentTimeMillis();
					try {
						preParsedScript = Utils.preParseRuby(
								scriptingContainer, controller_content,
								targetActivity);
					} catch (ParseFailedException e) {
						e.printStackTrace();
						executionBundle.addError(e.getMessage());
					}
					long elapsed = System.currentTimeMillis() - start;
					Log.d(this.getClass().toString(),
							"controller preparse: elapsed time = " + elapsed
									+ "ms");
				}
			} else {
				controllerClass = csplit[0];
			}
		}

		ActivityBuilder builder = new ActivityBuilder(mainActivityDocument,
				targetActivity, baseUrl, resId);
		executionBundle.getPayload().setActivityBuilder(builder);
		executionBundle.getPayload().setExecutionBundle(executionBundle);
		executionBundle.getPayload().setActiveApp(app);
		executionBundle.setCurrentUrl(pageUrl);

		scriptingContainer.put("$container_payload",
				executionBundle.getPayload());
		try {
			scriptingContainer.runScriptlet("$framework.before_activity_setup");
		} catch (Exception e) {
			e.printStackTrace();
		}

		resultBundle = builder.preload(executionBundle);


		return builder;
	}

	@Override
	protected void onPostExecute(ActivityBuilder result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		if (result != null) {
			buildView(result);
			try {
				if (preParsedScript != null) {
					preParsedScript.run();
				}
				scriptingContainer.runScriptlet("$framework.preload");
				if (preParsedScript != null) {
					Log.d(this.getClass().toString(), "class = "
							+ controllerClass);
					IRubyObject instance;
					instance = (IRubyObject) scriptingContainer
							.runScriptlet("$framework.script('"
									+ controllerClass + "')");
					executionBundle.setCurrentController(instance);
				}
			} catch (EvalFailedException e) {
				e.printStackTrace();
				executionBundle.addError(e.getMessage());
				Log.e(this.getClass().toString(), e.getMessage());
			} catch (RuntimeException e) {
				e.printStackTrace();
				executionBundle.addError(e.getMessage());
				Log.e(this.getClass().toString(), e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				executionBundle.addError(e.getMessage());
				Log.e(this.getClass().toString(), e.getMessage());
			}

		}
	}

	private long buildView(ActivityBuilder result) {
		long start = System.currentTimeMillis();
		
		Log.d(this.getClass().toString(), "parsing and preparing views....");

		preparedViews = result.prepare();

		long elapsed = System.currentTimeMillis() - start;
		Log.d(this.getClass().toString(), "prepare activity: elapsed time = "
				+ elapsed + "ms");
		View view = result.setPreparedView(preparedViews);
		// apply CSS
		result.applyStyle(view, resultBundle);
		Log.d(this.getClass().toString(), "build activity: elapsed time = "
				+ elapsed + "ms");
		
		if (onReadyListener != null) {
			Log.d(this.getClass().toString(), "invoking onDocumentReady.");
			onReadyListener.onDocumentReady(mainActivityDocument);
		} else {
			Log.d(this.getClass().toString(), "no OnDocumentReady passed.");
		}

		return start;
	}
}