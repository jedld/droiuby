package com.droiuby.client.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jruby.CompatVersion;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

class DroiubyHttpResponseHandler extends BasicResponseHandler {

	Context context;
	URL requestURL;
	String namespace;

	public DroiubyHttpResponseHandler(String url, Context context,
			String namespace) {
		this.namespace = namespace;
		try {
			requestURL = new URL(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.context = context;
	}

	public String handleResponse(HttpResponse response)
			throws ClientProtocolException, IOException {
		Utils.logHeaders(response.getAllHeaders(), this.getClass());
		long content_length = response.getEntity().getContentLength();
		Log.d(this.getClass().toString(), "status = "
				+ response.getStatusLine().getStatusCode());
		Log.d(this.getClass().toString(), "reason = "
				+ response.getStatusLine().getReasonPhrase());
		Log.d(this.getClass().toString(), "content length = " + content_length);
		
		if (response.getStatusLine().getStatusCode() < 300) {
			String responseBody = super.handleResponse(response);
			Log.d(this.getClass().toString(), "response = " + responseBody);
			
			Header headers[] = response.getHeaders("Set-Cookie");
			SharedPreferences prefs = context.getSharedPreferences("cookies",
					context.MODE_PRIVATE);
			Editor edit = prefs.edit();
			for (Header header : headers) {
				String name = requestURL.getProtocol() + "_"
						+ requestURL.getHost() + "_" + namespace;
				String value = header.getValue();
				Log.d(this.getClass().toString(), "Saving coookie " + name
						+ " = " + value);
				edit.putString(name, value);
			}
			edit.apply();
			return responseBody;
		} else {
			return null;
		}
	}

}

public class Utils {

	public static final int HTTP_GET = 1;
	public static final int HTTP_POST = 2;

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

	public static void logHeaders(Header[] headers, Class context) {
		for (Header header : headers) {
			Log.d(context.getName(), "Location Header " + header.getName()
					+ "=" + header.getValue());
		}
	}

	public static String load(Context c, String url, String namespace) {
		Log.d(ActiveAppDownloader.class.toString(), "loading " + url);
		if (url.indexOf("asset:") != -1) {
			return Utils.loadAsset(c, url);
		} else {
			return Utils.query(url, c, namespace);
		}
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

	public static String query(String url, Context c, String namespace) {
		return query(url, c, namespace, Utils.HTTP_GET);
	}

	public static String query(String url, Context c, String namespace,
			int method) {
		Log.d(Utils.class.toString(), "query url = " + url);
		HttpClient httpclient = new DefaultHttpClient();
		HttpUriRequest request = null;
		if (method == Utils.HTTP_GET) {
			request = new HttpGet(url);
		} else {
			request = new HttpPost(url);
		}

		URL parsedURL = null;
		try {
			parsedURL = new URL(url);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		SharedPreferences prefs = c.getSharedPreferences("cookies",
				c.MODE_PRIVATE);
		String cookie = prefs.getString(parsedURL.getProtocol() + "_"
				+ parsedURL.getHost() + "_" + namespace, null);

		if (namespace != null && cookie != null) {
			if (!cookie.toString().trim().equals("")) {
				request.setHeader("Cookie", cookie.toString());
				Log.d(Utils.class.toString(),
						"setting cookie = " + cookie.toString());
			}
		}
		request.setHeader(
				"User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.89 Safari/537.1 Droiuby/1.0 (Android)");
		WindowManager wm = (WindowManager) c
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		request.setHeader("Accept",
				"text/html,application/xhtml+xml,application/xml,application/x-ruby;q=0.9,*/*;q=0.8");
		request.setHeader("Droiuby-Height",
				Integer.toString(metrics.heightPixels));
		request.setHeader("Droiuby-Width",
				Integer.toString(metrics.widthPixels));
		request.setHeader("Droiuby-Density", Float.toString(metrics.density));
		request.setHeader("Droiuby-Dpi", Integer.toString(metrics.densityDpi));
		request.setHeader("Droiuby-OS",
				"android " + Integer.valueOf(android.os.Build.VERSION.SDK_INT));

		ResponseHandler<String> responseHandler = new DroiubyHttpResponseHandler(
				url, c, namespace);
		try {
			Utils.logHeaders(request.getAllHeaders(), Utils.class);
			String responseString = httpclient
					.execute(request, responseHandler);

			return responseString;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
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
					
					File file = new File(outputdir + filename);
					File dir = new File(file.getParent());
					dir.mkdirs();
					
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
