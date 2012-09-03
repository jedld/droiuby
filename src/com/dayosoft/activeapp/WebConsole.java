package com.dayosoft.activeapp;

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
import android.content.res.AssetManager;
import android.util.Log;

import com.dayosoft.activeapp.utils.NanoHTTPD;
import com.dayosoft.activeapp.utils.NanoHTTPD.Response;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WebConsole extends NanoHTTPD {

	ScriptingContainer container;
	public static WebConsole instance;
	WeakReference<Activity> activity;
	AssetManager manager;
	
	public ScriptingContainer getContainer() {
		return container;
	}

	public void setContainer(ScriptingContainer container) {
		this.container = container;
	}

	public static WebConsole getInstance() {
		return instance;
	}

	public static void setInstance(WebConsole instance) {
		WebConsole.instance = instance;
	}

	public WeakReference<Activity> getActivity() {
		return activity;
	}

	public void setActivity(WeakReference<Activity> activity) {
		this.activity = activity;
	}

	public static boolean uiPosted = false;
	

	protected WebConsole(int port, File wwwroot, Activity activity,
			ScriptingContainer container) throws IOException{
		super(port, wwwroot);
		this.activity = new WeakReference<Activity>(activity);
		this.container = container;
		this.manager = activity.getAssets();
		Log.d(this.getClass().toString(), "Starting HTTPD server on port "
				+ port);
	}
	
	public static WebConsole getInstance(int port, File wwwroot, Activity activity,
			ScriptingContainer container) throws IOException {
		if (instance == null) {
			instance = new WebConsole(port, wwwroot, activity, container);
		};
		return instance;
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

	@Override
	public Response serve(String uri, String method, Properties header,
			Properties params, Properties files) {
		Log.d(this.getClass().toString(), "HTTP request received. uri = " + uri);
		Response response;
		if (uri.startsWith("/console")) {
			final String statement = params.getProperty("cmd", "");

			OutputStream output = new OutputStream() {
				private StringBuilder string = new StringBuilder();

				@Override
				public void write(int b) throws IOException {
					this.string.append((char) b);
				}

				public String toString() {
					return this.string.toString();
				}
			};

			StringWriter writer = new StringWriter();
			container.setWriter(writer);
			StringBuffer resultStr = new StringBuffer();
			ObjectMapper mapper = new ObjectMapper();

			final Map<String, String> resultMap = new HashMap<String, String>();
			resultMap.put("cmd", statement);
			try {
				final EmbedEvalUnit evalUnit = container.parse(statement, 0);

				WebConsole.uiPosted = false;
				if (activity.get() != null) {
					activity.get().runOnUiThread(new Runnable() {
						public void run() {
							execute(container, evalUnit, resultMap);
							WebConsole.uiPosted = true;
						}
					});
					while (!uiPosted) {
						Thread.sleep(100);
					}
				} else {
					container.put("inspect_target", evalUnit.run());
					container
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

			try {
				resultStr.append(mapper.writeValueAsString(resultMap));
			} catch (JsonGenerationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

}