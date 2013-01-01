package com.droiuby.client.core.console;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.ViewGroup;

import com.droiuby.client.CanvasActivity;
import com.droiuby.client.R;
import com.droiuby.client.core.ActiveApp;
import com.droiuby.client.core.ActivityBuilder;
import com.droiuby.client.core.AppDownloader;
import com.droiuby.client.core.ExecutionBundle;
import com.droiuby.client.core.OnDownloadCompleteListener;
import com.droiuby.client.core.callbacks.OnAppDownloadComplete;
import com.droiuby.client.utils.ActiveAppDownloader;
import com.droiuby.client.utils.NanoHTTPD;
import com.droiuby.client.utils.NanoHTTPD.Response;
import com.fasterxml.jackson.core.JsonGenerationException;

public class WebConsole extends NanoHTTPD {

	WeakReference<ExecutionBundle> bundleRef;
	public static WebConsole instance;
	WeakReference<Activity> activity;
	AssetManager manager;
	int referenceCount = 0;

	public ExecutionBundle getBundle() {
		return bundleRef.get();
	}

	public void setBundle(ExecutionBundle bundle) {
		this.bundleRef = new WeakReference<ExecutionBundle>(bundle);
	}

	public static WebConsole getInstance() {
		return instance;
	}

	public static void setInstance(WebConsole instance) {
		WebConsole.instance = instance;
	}

	public Activity getActivity() {
		return activity.get();
	}

	public void setActivity(Activity activity) {
		this.activity = new WeakReference<Activity>(activity);
		this.manager = activity.getAssets();
	}

	public static boolean uiPosted = false;

	protected WebConsole(int port, File wwwroot) throws IOException {
		super(port, wwwroot);
		Log.d(this.getClass().toString(), "Starting HTTPD server on port "
				+ port);
	}

	public static WebConsole getInstance(int port, File wwwroot)
			throws IOException {
		if (instance == null) {
			instance = new WebConsole(port, wwwroot);
		}
		instance.incrementReference();
		return instance;
	}

	public void incrementReference() {
		referenceCount++;
	}

	public void shutdownConsole() {
		referenceCount--;
		if (referenceCount <= 0) {
			instance = null;
			this.stop();
		}
	}

	public static void execute(ScriptingContainer container,
			EmbedEvalUnit evalUnit, Map<String, String> resultMap) {
		try {

			container.put("inspect_target", evalUnit.run());
			container.runScriptlet("puts \"=> #{inspect_target.inspect}\"");
		} catch (org.jruby.embed.EvalFailedException e) {
			Log.d(WebConsole.class.toString(), "eval failed: " + e.getMessage());
			e.printStackTrace();
			resultMap.put("err", "true");
			resultMap.put("result", e.getMessage());
		} catch (org.jruby.embed.ParseFailedException e) {
			e.printStackTrace();
			resultMap.put("err", "true");
			resultMap.put("result", e.getMessage());
		} catch (org.jruby.exceptions.RaiseException e) {
			e.printStackTrace();
			resultMap.put("err", "true");
			resultMap.put("result", e.getException().toString());
		} catch (Exception e) {
			resultMap.put("err", "true");
			resultMap.put("result", e.getMessage());
		}
	}

	String escapeJSON(String str) {
		return "\""
				+ str.replace("\\", "\\\\").replace("\"", "\\\"")
						.replace("\n", "\\n").replace("\r", "\\r")
						.replace("\t", "\\t") + "\"";
	}

	String mapToJSON(Map<String, String> resultMap) {
		StringBuilder jsonString = new StringBuilder();
		jsonString.append("{");
		boolean first = true;
		for (String key : resultMap.keySet()) {
			if (!first) {
				jsonString.append(",");
			} else {
				first = false;
			}
			String value = resultMap.get(key);
			if (value == null) {
				jsonString.append("null");
			} else {
				jsonString.append(escapeJSON(key) + ":" + escapeJSON(value));
			}
		}
		jsonString.append("}");
		return jsonString.toString();
	}

	@Override
	public Response serve(String uri, String method, Properties header,
			Properties params, Properties files) {
		Log.d(this.getClass().toString(), "HTTP request received. uri = " + uri);

		Response response;
		if (uri.startsWith("/control")) {
			final Map<String, String> resultMap = new HashMap<String, String>();
			String cmd = params.getProperty("cmd", "");
			if (cmd.equals("launch")) {
				final String url = params.getProperty("url", "");
				final Activity currentActivity = activity.get();
				if (currentActivity != null) {
					currentActivity.runOnUiThread(new Runnable() {
						public void run() {
							ActivityBuilder.loadApp(currentActivity, url);
						}
					});

					resultMap.put("result", "success");
				} else {
					resultMap.put("err", "true");
					resultMap
							.put("result",
									"No JRuby instance attached. Make sure an activity is visible before issuing console commands");
				}
			} else
			if (cmd.equals("reload")) {
				final Activity currentActivity = activity.get();
				if (currentActivity instanceof CanvasActivity) {
					currentActivity.runOnUiThread(new Runnable() {
						public void run() {
							((CanvasActivity) currentActivity)
							.refreshCurrentApplication();
						}
					});
					resultMap.put("result", "success");
				} else {
					resultMap.put("err", "true");
					resultMap
							.put("result",
									"Activity must be an instance of CanvasActivity to support this operation.");
				}
			} else {
				resultMap.put("err", "true");
				resultMap.put("result", "unknown command");
			}
			response = new Response(NanoHTTPD.HTTP_OK, "application/json",
					mapToJSON(resultMap));

		} else if (uri.startsWith("/console")) {
			final String statement = params.getProperty("cmd", "");
			final Map<String, String> resultMap = new HashMap<String, String>();
			StringBuilder resultStr = new StringBuilder();
			final ExecutionBundle bundle = bundleRef.get();
			if (bundle == null) {
				resultMap.put("err", "true");
				resultMap
						.put("result",
								"No JRuby instance attached. Make sure an activity is visible before issuing console commands");
			} else {

				StringWriter writer = new StringWriter();
				final ScriptingContainer scripting_container = bundle
						.getContainer();
				scripting_container.setWriter(writer);

				resultMap.put("cmd", statement);
				try {
					final EmbedEvalUnit evalUnit = scripting_container.parse(
							statement, 0);
					Activity currentActivity = activity.get();
					WebConsole.uiPosted = false;
					if (currentActivity != null) {
						Log.d(this.getClass().toString(), "Current Activity "
								+ currentActivity.toString());
						currentActivity.runOnUiThread(new Runnable() {
							public void run() {
								Log.d(this.getClass().toString(),
										"Running command on "
												+ bundle.toString());
								execute(scripting_container, evalUnit,
										resultMap);
								WebConsole.uiPosted = true;
							}
						});
						while (!uiPosted) {
							Thread.sleep(100);
						}
					} else {
						scripting_container.put("inspect_target",
								evalUnit.run());
						scripting_container
								.runScriptlet("puts \"=> #{inspect_target.inspect}\"");
					}

					writer.flush();
					if (!resultMap.containsKey("err")) {
						resultMap.put("result", writer.getBuffer().toString());
					}
				} catch (org.jruby.embed.ParseFailedException e) {
					resultMap.put("parse_failed", "true");
					resultMap.put("err", "true");
					resultMap.put("result", e.getMessage());
				} catch (org.jruby.embed.InvokeFailedException e) {
					resultMap.put("parse_failed", "true");
					resultMap.put("err", "true");
					resultMap.put("result", e.getMessage());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				resultStr.append(mapToJSON(resultMap));
			}

			response = new Response(NanoHTTPD.HTTP_OK, "application/json",
					resultStr.toString());
		} else {
			try {
				if (uri.equalsIgnoreCase("/")) {
					uri = "/index.html";
				}
				InputStream stream = manager.open("lib/console" + uri);
				String content_type = "text/html";
				if (uri.endsWith(".js")) {
					content_type = "application/javascript";
				} else if (uri.endsWith(".css")) {
					content_type = "text/css";
				}
				response = new Response(NanoHTTPD.HTTP_OK, content_type, stream);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				response = new Response(NanoHTTPD.HTTP_NOTFOUND, "text/html",
						"Cannot find " + uri);
			}
		}
		return response;
	}

	public void setActiveApp(ActiveApp application) {
		// TODO Auto-generated method stub

	}

}