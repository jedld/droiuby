package com.droiuby.client.core.console;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.ScriptingContainer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.Toast;

import com.droiuby.application.bootstrap.DroiubyApp;
import com.droiuby.client.core.DroiubyLauncher;
import com.droiuby.client.core.ExecutionBundle;
import com.droiuby.client.core.ExecutionBundleFactory;
import com.droiuby.client.core.utils.NanoHTTPD;
import com.droiuby.client.core.utils.OnWebConsoleReady;
import com.droiuby.client.core.utils.Utils;
import com.droiuby.launcher.ExecutionBundleInterface;
import com.droiuby.launcher.Options;
import com.droiuby.launcher.WebConsoleInterface;

public class WebConsole extends NanoHTTPD implements WebConsoleInterface  {

	private static final String TAG = WebConsole.class.getName();
	WeakReference<ExecutionBundleInterface> bundleRef;
	public static WebConsole instance;
	WeakReference<Activity> activity;
	AssetManager manager;
	int referenceCount = 0;

	public ExecutionBundleInterface getBundle() {
		return bundleRef.get();
	}

	public void setBundle(ExecutionBundleInterface bundle) {
		this.bundleRef = new WeakReference<ExecutionBundleInterface>(bundle);
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

	protected WebConsole(int port, File wwwroot, OnWebConsoleReady listener)
			throws IOException {
		super(port, wwwroot, listener);
		Log.d(this.getClass().toString(), "Starting HTTPD server on port "
				+ port);
	}

	public static WebConsole getInstance(int port, File wwwroot,
			OnWebConsoleReady listener) throws IOException {
		if (instance == null) {
			instance = new WebConsole(port, wwwroot, listener);
		} else {
			listener.onReady(instance);
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
		Log.d(this.getClass().toString(), "HTTP request received. uri  = " + uri);

		Response response;
		if (method.equalsIgnoreCase("POST") && uri.startsWith("/upload_file")) {
			String content = params.getProperty("content");
			ExecutionBundle bundle = (ExecutionBundle) getBundle();
			if (bundle!=null) {
				ScriptingContainer container = bundle.getContainer();
				container.put("$file_contents", content);
				Log.d(TAG, "file content " + content + "set");
				return new Response(NanoHTTPD.HTTP_OK, "application/json",
					"{\"status\": \"OK\"}");
			} else {
				return new Response(NanoHTTPD.HTTP_INTERNALERROR, "text/html",
						"no Activity attached.");
			}
		} else
		if (method.equalsIgnoreCase("POST") && uri.startsWith("/upload")) {
			String name = params.getProperty("name", null);

			Log.d(this.getClass().toString(), "Receiving file upload " + name);
			if (activity.get() != null) {
				String filename = files.getProperty("file");
				boolean launch = params.getProperty("run", "false").equals(
						"true") ? true : false;

				try {

					String extraction_target = Utils.processArchive(
							activity.get(), name, filename,
							true);

					if (launch) {
						Log.d(this.getClass().toString(),
								"running application...");
						final Map<String, String> resultMap = new HashMap<String, String>();
						launchAppFromUrl(resultMap, "file://"
								+ extraction_target + File.separator
								+ "config.droiuby");
					}

					return new Response(NanoHTTPD.HTTP_OK, "application/json",
							"{\"status\": \"OK\"}");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
					return new Response(NanoHTTPD.HTTP_INTERNALERROR,
							"text/html", e.getMessage());
				}

				if (name != null) {
					return new Response(NanoHTTPD.HTTP_OK, "application/json",
							"{\"status\": \"OK\"}");
				} else {
					return new Response(NanoHTTPD.HTTP_BADREQUEST, "text/html",
							"Name is required.");
				}
			} else {
				return new Response(NanoHTTPD.HTTP_INTERNALERROR, "text/html",
						"no Activity attached.");
			}
		} else {
			if (uri.startsWith("/control")) {
				final Map<String, String> resultMap = new HashMap<String, String>();
				String cmd = params.getProperty("cmd", "");
				if (cmd.equals("launch")) {
					final String url = params.getProperty("url", "");
					launchAppFromUrl(resultMap, url);
				} else if (cmd.equals("list")) {
					String[] bundles = ExecutionBundleFactory
							.listActiveBundles();
					resultMap.put("result", "success");
					resultMap.put("list", StringUtils.join(bundles, ','));
				} else if (cmd.equals("autostart")) {
					ExecutionBundle bundle = (ExecutionBundle) getBundle();

					String name = params.getProperty("name", null);
					if (name != null) {
						bundle = ExecutionBundleFactory.getBundle(name);
					}

					if (bundle != null) {
						SharedPreferences prefs = bundle.getCurrentActivity()
								.getSharedPreferences("bootstrap",
										Context.MODE_PRIVATE);

						String url = bundle.getPayload().getActiveApp()
								.getLaunchUrl();
						prefs.edit().putString("autostart", url).commit();
						resultMap.put("result", "success");
						resultMap.put("autostart", url);
					} else {
						resultMap.put("err", "true");
						resultMap.put("result", "no activity attached");
					}
				} else if (cmd.equals("clearautostart")) {
					ExecutionBundle bundle = (ExecutionBundle) getBundle();
					if (bundle != null) {
						SharedPreferences prefs = bundle.getCurrentActivity()
								.getSharedPreferences("boostrap",
										Context.MODE_PRIVATE);
						prefs.edit().remove("autostart").commit();
					} else {
						resultMap.put("err", "true");
						resultMap.put("result", "no activity attached");
					}
				} else if (cmd.equals("switch")) {
					String namespace = params.getProperty("name");
					ExecutionBundle bundle = ExecutionBundleFactory
							.getBundle(namespace);
					if (bundle != null) {
						this.setBundle(bundle);
						resultMap.put("result", "success");
					} else {
						resultMap.put("err", "true");
						resultMap.put("result", "unknown namespace");
					}
				} else if (cmd.equals("proximity")) {
					String flag = params.getProperty("switch");

					ExecutionBundle bundle = (ExecutionBundle) getBundle();
					if (bundle != null) {
						SharedPreferences prefs = bundle.getCurrentActivity()
								.getSharedPreferences("bootstrap",
										Context.MODE_PRIVATE);
						if (flag.equals("true")) {
							prefs.edit().putBoolean("proximity_refresh", true)
									.commit();
						} else {
							prefs.edit().putBoolean("proximity_refresh", false)
									.commit();
						}
						resultMap.put("result", "success");
					} else {
						resultMap.put("err", "true");
						resultMap.put("result", "no app loaded.");
					}

				} else if (cmd.equals("reload")) {
					final Activity currentActivity = activity.get();
					if (currentActivity != null) {
						DroiubyLauncher.refresh(currentActivity, (ExecutionBundle) getBundle(),
								null);
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
				final ExecutionBundle bundle = (ExecutionBundle) bundleRef.get();
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
						final EmbedEvalUnit evalUnit = scripting_container
								.parse(statement, 0);
						Activity currentActivity = activity.get();
						WebConsole.uiPosted = false;
						if (currentActivity != null) {
							Log.d(this.getClass().toString(),
									"Current Activity "
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
							resultMap.put("result", writer.getBuffer()
									.toString());
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
					response = new Response(NanoHTTPD.HTTP_OK, content_type,
							stream);
				} catch (IOException e) {
					response = new Response(NanoHTTPD.HTTP_NOTFOUND,
							"text/html", "Cannot find " + uri);
				}
			}

		}
		return response;
	}

	private void launchAppFromUrl(final Map<String, String> resultMap,
			final String url) {
		final Activity currentActivity = activity.get();
		if (currentActivity != null) {
			currentActivity.runOnUiThread(new Runnable() {
				public void run() {
					
					if (!getBundle().isRootBundle()) {
						Log.d(this.getClass().toString(),"Closing current bundle " + getBundle().getName());
						currentActivity.finish();
						getBundle().terminate();
					}
					
					Options options = new Options();
					options.setOverwrite(true);
					options.setNewActivity(true);
					options.setCloseParentActivity(false);
					options.setNewRuntime(true);
					options.setConsole(WebConsole.this);
					options.setRootBundle(false);
					
					Toast.makeText(currentActivity, "launching " + url, Toast.LENGTH_SHORT).show();
					
					DroiubyLauncher.launch(currentActivity, url, options);
				}
			});

			resultMap.put("result", "success");
		} else {
			resultMap.put("err", "true");
			resultMap
					.put("result",
							"No JRuby instance attached. Make sure an activity is visible before issuing console commands");
		}
	}

}