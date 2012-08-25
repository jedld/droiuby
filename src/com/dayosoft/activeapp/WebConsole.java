package com.dayosoft.activeapp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.ScriptingContainer;

import android.app.Activity;
import android.content.res.AssetManager;
import android.util.Log;

import com.dayosoft.activeapp.utils.NanoHTTPD;
import com.dayosoft.activeapp.utils.NanoHTTPD.Response;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WebConsole extends NanoHTTPD {

	Activity activity;
	ScriptingContainer container;
	public static boolean uiPosted = false;

	public WebConsole(int port, File wwwroot, Activity activity,
			ScriptingContainer container) throws IOException {
		super(port, wwwroot);
		this.activity = activity;
		this.container = container;
		Log.d(this.getClass().toString(), "Starting HTTPD server on port "
				+ port);
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

				this.uiPosted = false;
				activity.runOnUiThread(new Runnable() {
					public void run() {
						try {
							evalUnit.run();
						} catch (org.jruby.embed.EvalFailedException e) {
							resultMap.put("err", "true");
							resultMap.put("result", e.getMessage());
						} catch (org.jruby.embed.ParseFailedException e) {
							resultMap.put("err", "true");
							resultMap.put("result", e.getMessage());
						} catch (org.jruby.exceptions.RaiseException e) {
							resultMap.put("err", "true");
							resultMap.put("result", e.getMessage());
						}
						WebConsole.uiPosted = true;
					}
				});
				while (!uiPosted) {
					Thread.sleep(100);
				}
				writer.flush();
				resultMap.put("result", writer.getBuffer().toString());
			} catch (org.jruby.embed.ParseFailedException e) {
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
			AssetManager manager = activity.getAssets();
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