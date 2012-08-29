package com.dayosoft.activeapp.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jruby.CompatVersion;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

public class Utils {

	public static ScriptingContainer evalRuby(String statement,
			Activity activity) {
		ScriptingContainer container = new ScriptingContainer(
				LocalContextScope.CONCURRENT, LocalVariableBehavior.TRANSIENT);
		container.setCompatVersion(CompatVersion.RUBY1_9);
		return evalRuby(container, statement, activity);
	}

	public static ScriptingContainer evalRuby(ScriptingContainer container,
			String statement, Activity activity) {
		container.runScriptlet(statement);
		return container;
	}

	public static EmbedEvalUnit preParseRuby(ScriptingContainer container,
			String statement, Activity activity) {
		EmbedEvalUnit parsed = container.parse(statement, 0);
		return parsed;
	}

	public static String loadFile(String asset_path) {
		File asset = new File(asset_path);
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(asset));
			StringBuffer contents = new StringBuffer();
			while (reader.ready()) {
				contents.append(reader.readLine());
			}
			return contents.toString();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static String loadAsset(Context c, String url) {
		String asset_path = url.substring(6);
		Log.d(Utils.class.toString(), "Loading from asset " + asset_path);
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(c.getAssets()
					.open(asset_path)));

			StringBuffer content = new StringBuffer();

			while (reader.ready()) {
				content.append(reader.readLine() + "\n");
			}
			return content.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static String query(String url) {
		Log.d(Utils.class.toString(),"query url = " + url);
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		request.setHeader("User-Agent", "Droiuby/1.0 (Android)");
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		try {
			return httpclient.execute(request, responseHandler);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public static boolean unpackZip(InputStream is, String outputdir) {
		ZipInputStream zis;
		try {
			String filename;

			zis = new ZipInputStream(new BufferedInputStream(is));
			ZipEntry ze;
			byte[] buffer = new byte[1024];
			int count;

			while ((ze = zis.getNextEntry()) != null) {
				filename = ze.getName();
				Log.d(Utils.class.toString(), "processing " + filename);
				if (ze.isDirectory()) {
					File dir = new File(outputdir + filename);
					
					dir.mkdirs();
				} else {
					FileOutputStream fout = new FileOutputStream(outputdir
							+ filename);

					while ((count = zis.read(buffer)) != -1) {
						fout.write(buffer, 0, count);
					}
					fout.close();
				}


				zis.closeEntry();
			}

			zis.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
}
