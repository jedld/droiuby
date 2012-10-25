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
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.jruby.CompatVersion;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;

import com.droiuby.client.core.ActiveApp;
import com.droiuby.client.core.ExecutionBundle;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

class DroiubyCookie implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7903895180302847726L;

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public long getExpiration() {
		return expiration;
	}

	public void setExpiration(long expiration) {
		this.expiration = expiration;
	}

	String cookie;
	long expiration;

	public static DroiubyCookie parse(String raw_cookie) {
		DroiubyCookie cookie = new DroiubyCookie();
		String[] rawCookieParams = raw_cookie.split(";");
		cookie.setCookie(rawCookieParams[0]);
		return cookie;
	}

}

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

			Header encheader = response.getEntity().getContentEncoding();
			if (encheader != null) {
				HeaderElement[] codecs = encheader.getElements();
				for (int i = 0; i < codecs.length; i++) {
					if (codecs[i].getName().equalsIgnoreCase("gzip")) {
						response.setEntity(new GzipDecompressingEntity(response
								.getEntity()));
					}
				}
			}

			String responseBody = super.handleResponse(response);
			// Log.d(this.getClass().toString(), "response = " + responseBody);

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
				edit.putString(name, DroiubyCookie.parse(value).cookie);
			}
			edit.commit();
			return responseBody;
		} else {
			return null;
		}
	}

}

public class Utils {

	public static final int HTTP_GET = 1;
	public static final int HTTP_POST = 2;

	public static final int ASSET_TYPE_TEXT = 0;
	public static final int ASSET_TYPE_IMAGE = 1;

	public static ScriptingContainer evalRuby(String statement,
			Activity activity) {
		ScriptingContainer container = new ScriptingContainer(
				LocalContextScope.THREADSAFE, LocalVariableBehavior.TRANSIENT);
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

	public static String load(Context c, String url, ExecutionBundle bundle) {
		Log.d(ActiveAppDownloader.class.toString(), "loading " + url
				+ " under namespace = "
				+ bundle.getPayload().getActiveApp().getBaseUrl());
		if (url.indexOf("asset:") != -1) {
			return Utils.loadAsset(c, url);
		} else {
			return Utils.query(url, c, bundle.getPayload().getActiveApp()
					.getBaseUrl());
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

		String cookie_pref_name = parsedURL.getProtocol() + "_"
				+ parsedURL.getHost() + "_" + namespace;
		Log.d("COOKIE", "Loading cookie from " + cookie_pref_name);
		String cookie = prefs.getString(cookie_pref_name, null);

		if (namespace != null && cookie != null) {
			if (!cookie.toString().trim().equals("")) {
				request.setHeader("Cookie", cookie.toString());
				Log.d(Utils.class.toString(),
						"setting cookie = " + cookie.toString());
			}
		}

		final HttpParams httpParams = new BasicHttpParams();
		HttpClientParams.setRedirecting(httpParams, true);
		request.setParams(httpParams);

		request.setHeader(
				"User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.89 Safari/537.1 Droiuby/1.0 (Android)");
		WindowManager wm = (WindowManager) c
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		request.setHeader(
				"Accept",
				"text/html,application/xhtml+xml,application/xml,application/x-ruby;q=0.9,*/*;q=0.8");
		request.setHeader("Accept-Encoding","gzip, deflate");
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

	public static Object loadAppAsset(ActiveApp app, Context context,
			String asset_name, int asset_type, int method) {
		if (asset_name != null) {
			if (asset_name.startsWith("asset:")) {
				return Utils.loadAsset(context, asset_name);
			} else {
				String baseUrl = app.getBaseUrl();
				Log.d("Utils", "base url = " + baseUrl);
				if (baseUrl.indexOf("asset:") != -1) {
					return Utils.loadAsset(context, baseUrl + asset_name);
				} else if (baseUrl.indexOf("file:") != -1) {
					return Utils.loadFile(asset_name);
				} else if (baseUrl.indexOf("sdcard:") != -1) {
					File directory = Environment.getExternalStorageDirectory();
					try {
						String asset_path = directory.getCanonicalPath()
								+ asset_name;
						return Utils.loadFile(asset_path);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return null;
				} else {
					String query_url = null;

					if (asset_name.startsWith("http:")
							|| asset_name.startsWith("https:")) {
						query_url = asset_name;
					} else {

						if (asset_name.startsWith("/")) {
							asset_name = asset_name.substring(1);
						}

						if (baseUrl.endsWith("/")) {
							baseUrl = baseUrl
									.substring(0, baseUrl.length() - 1);
						}

						query_url = baseUrl + "/" + asset_name;
					}
					if (asset_type == Utils.ASSET_TYPE_TEXT) {
						return Utils.query(query_url, context, app.getName(),
								method);
					} else if (asset_type == Utils.ASSET_TYPE_IMAGE) {
						return UrlImageViewHelper
								.downloadFromUrlAsync(context, query_url,
										UrlImageViewHelper
												.getFilenameForUrl(query_url));
					}
					return null;
				}
			}
		} else {
			return null;
		}
	}
}
