package com.droiuby.client.core;

import java.io.IOException;
import java.io.StringReader;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jruby.embed.ParseFailedException;

import com.droiuby.application.ActiveApp;
import com.droiuby.client.core.utils.ActiveAppDownloader;
import com.droiuby.client.core.utils.Utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class DroiubyRunner extends AsyncTask<Void, Void, ExecutionBundle> {

	OnLoadComplete listener;
	Context context;
	String url;

	protected DroiubyRunner(Context context, String url, OnLoadComplete listener) {
		this.listener = listener;
		this.context = context;
		this.url = url;
	}

	public static void run(Context context, String url, OnLoadComplete listener) {
		DroiubyRunner runner = new DroiubyRunner(context, url, listener);
		runner.execute();
	}

	@Override
	protected ExecutionBundle doInBackground(Void... params) {
		ActiveApp app = ActiveAppDownloader.loadApp(context, url);
		String responseBody = (String) Utils.loadAppAsset(app, context, url,
				Utils.ASSET_TYPE_TEXT, Utils.HTTP_GET);

		if (responseBody == null) {
			responseBody = "<activity><t>Problem loading url " + url
					+ "</t></activity>";
		}

		ExecutionBundle bundle = ExecutionBundleFactory
				.getBundle(app.getName());
		SAXBuilder sax = new SAXBuilder();
		try {
			Document mainActivityDocument = sax.build(new StringReader(
					responseBody));
			String controller = mainActivityDocument.getRootElement()
					.getAttributeValue("controller");
			String controllerClass;
			String baseUrl = app.getBaseUrl();
			if (controller != null) {
				String csplit[] = org.apache.commons.lang3.StringUtils.split(
						controller, "#");
				if (csplit.length == 2) {

					if (!csplit[1].trim().equals("")) {
						controller = csplit[1];
					}

					if (!csplit[0].trim().equals("")) {
						Log.d("Activity loader", "loading controller file "
								+ baseUrl + csplit[0]);
						String controller_content = (String) Utils
								.loadAppAsset(app, context, csplit[0],
										Utils.ASSET_TYPE_TEXT, Utils.HTTP_GET);

						long start = System.currentTimeMillis();
						try {
							Object preParsedScript = Utils.preParseRuby(
									bundle.getContainer(), controller_content);
						} catch (ParseFailedException e) {
							e.printStackTrace();
							bundle.addError(e.getMessage());
						}
						long elapsed = System.currentTimeMillis() - start;
						Log.d(this.getClass().toString(),
								"controller preparse: elapsed time = "
										+ elapsed + "ms");
					}
				} else {
					controllerClass = csplit[0];
				}
			}

		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
