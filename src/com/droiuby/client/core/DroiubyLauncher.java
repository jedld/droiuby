package com.droiuby.client.core;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.droiuby.application.DroiubyApp;
import com.droiuby.client.core.utils.ActiveAppDownloader;
import com.droiuby.client.core.utils.Utils;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.util.Log;

public class DroiubyLauncher extends AsyncTask<Void,Void,Void>{

	Context context;
	String url;
	
	protected DroiubyLauncher(Context context, String url) {
		this.context = context;
		this.url = url;
	}
	
	public static void launch(Context context, String url) {
		DroiubyLauncher launcher = new DroiubyLauncher(context, url);
		launcher.execute();
	}
	
	private DroiubyApp download(String url) {
		String responseBody = null;
		Log.d(ActiveAppDownloader.class.toString(), "loading " + url);
		if (url.startsWith("file://") || url.indexOf("asset:") != -1) {
			responseBody = Utils.loadAsset(context, url);
		} else {
			responseBody = Utils.query(url, context, null);
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
				String framework = rootElem.getChildText("framework");
				if (baseUrl == null || baseUrl.equals("")) {
					if (url.startsWith("file://")) {
						baseUrl = extractBasePath(url);
					} else {
						URL aURL = new URL(url);
						String adjusted_path = aURL.getPath();
						adjusted_path = extractBasePath(adjusted_path);

						baseUrl = aURL.getProtocol() + "://" + aURL.getHost()
								+ ":" + aURL.getPort() + adjusted_path;
					}
				}

				DroiubyApp app = new DroiubyApp();
				app.setDescription(appDescription);
				app.setName(appName);
				app.setBaseUrl(baseUrl);
				app.setMainUrl(mainActivity);
				app.setFramework(framework);
				app.setLaunchUrl(url);
				app.setInitiallOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

				String orientation = rootElem.getChildText("orientation");
				if (orientation != null) {
					if (orientation.equals("landscape")) {
						app.setInitiallOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					} else if (orientation.equals("portrait")
							|| orientation.equals("vertical")) {
						app.setInitiallOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					} else if (orientation.equals("sensor_landscape")) {
						app.setInitiallOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
					} else if (orientation.equals("sensor_portrait")) {
						app.setInitiallOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
					} else if (orientation.equals("auto")) {
						app.setInitiallOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
					} else {
						app.setInitiallOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
					}
				}
				if (rootElem.getChild("assets") != null) {
					List<Element> assets = rootElem.getChild("assets")
							.getChildren("resource");
					for (Element asset : assets) {
						String asset_name = asset.getAttributeValue("name");
						String asset_type = asset.getAttributeValue("type");
						Log.d("APP DOWNLOADER", "loading asset " + asset_name
								+ ".");
						int type_int = DroiubyApp.ASSET_TYPE_SCRIPT;
						if (asset_type.equals("script")) {
							type_int = DroiubyApp.ASSET_TYPE_SCRIPT;
						} else if (asset_type.equals("css")) {
							type_int = DroiubyApp.ASSET_TYPE_CSS;
						} else if (asset_type.equals("lib")) {
							type_int = DroiubyApp.ASSET_TYPE_LIB;
						} else if (asset_type.equals("font") || asset_type.equals("typeface")) {
							type_int = DroiubyApp.ASSET_TYPE_TYPEFACE;
						} else if (asset_type.equals("binary")
								|| asset_type.equals("file")) {
							type_int = DroiubyApp.ASSET_TYPE_BINARY;
						} else if (asset_type.equals("vendor")) {
							type_int = DroiubyApp.ASSET_TYPE_VENDOR;
						}

						app.addAsset(asset_name, type_int);
					}
				}

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
	
	@Override
	protected Void doInBackground(Void... params) {
		DroiubyApp app = download(url);
		return null;
	}

	private static String extractBasePath(String adjusted_path) {
		int pos = 0;

		while ((adjusted_path.indexOf("/", pos)) != -1) {
			pos = adjusted_path.indexOf("/", pos) + 1;
		}
		adjusted_path = adjusted_path.substring(0, pos);
		return adjusted_path;
	}
	
}
