package com.droiuby.client;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.droiuby.client.R;
import com.droiuby.client.core.ActivityBuilder;
import com.droiuby.client.core.utils.Utils;
import com.droiuby.client.core.utils.intents.IntentIntegrator;
import com.droiuby.client.core.utils.intents.IntentResult;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

class InitializeLibrary extends AsyncTask<Void, Void, Void> {
	ProgressDialog progress_dialog;
	Context context;

	public InitializeLibrary(Context c) {
		this.context = c;
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		// TODO Auto-generated method stub
		try {
			String extract_path = context.getCacheDir().getCanonicalPath()
					+ "/jruby/vendor";
			File vendor = new File(extract_path);
			vendor.mkdirs();
			AssetManager manager = context.getAssets();
			String[] files = manager.list("lib/vendor");
			for (String file : files) {
				Log.d(this.getClass().toString(), "Unpacking " + file + " to "
						+ vendor.getCanonicalPath() + " .... ");
				Utils.unpackZip(manager.open("lib/vendor/" + file),
						vendor.getCanonicalPath() + "/");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		if (progress_dialog != null) {
			progress_dialog.dismiss();
		}
		SharedPreferences prefs = context.getSharedPreferences("droiuby",
				context.MODE_PRIVATE);
		prefs.edit().putBoolean("ruby.library_initialized", true).commit();
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		progress_dialog = ProgressDialog.show(context, "",
				"Please. Setting up ruby libraries ...", true);
	}

}

public class MainActivity extends Activity implements OnClickListener {
	EditText applicationURL;
	private SharedPreferences settings;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Button runButton = (Button) findViewById(R.id.buttonRun);
		Button qrCodeButton = (Button) findViewById(R.id.buttonQRCode);
		applicationURL = (EditText) findViewById(R.id.editTextApplicationURL);
		applicationURL.setText("http://droiuby.herokuapp.com/droiuby");
		runButton.setOnClickListener(this);
		qrCodeButton.setOnClickListener(this);
		settings = getSharedPreferences("droiuby", MODE_PRIVATE);
		if (!settings.contains("ruby.library_initialized")) {
			new InitializeLibrary(this).execute();
		}
	}

	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.buttonRun) {
			ActivityBuilder.loadApp(this, applicationURL.getText().toString());
		} else if (id == R.id.buttonQRCode) {
			IntentIntegrator integrator = new IntentIntegrator(this);
			integrator.initiateScan();
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		  IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		  if (scanResult != null) {
			  applicationURL.setText(scanResult.getContents());
		  }
		}
}
