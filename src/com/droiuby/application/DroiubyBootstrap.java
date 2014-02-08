package com.droiuby.application;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.droiuby.interfaces.DroiubyHelperInterface;

import dalvik.system.DexClassLoader;

class LibraryBootstrapTask extends AsyncTask<Void, String, ClassLoader> {

	Activity context;
	String libraries[];
	private static ClassLoader envelopedLoader;
	OnEnvironmentReady listener;
	public LibraryBootstrapTask(Activity context, String libraries[],
			OnEnvironmentReady listener) {
		this.context = context;
		this.libraries = libraries;
		this.listener = listener;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		int default_orientation = context.getResources().getConfiguration().orientation;
		if (default_orientation == Configuration.ORIENTATION_LANDSCAPE) {
			context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}

	@Override
	protected void onProgressUpdate(String... values) {
		super.onProgressUpdate(values);
		TextView view = (TextView)context.findViewById(R.id.loadingStatusText);
		if (view!=null) {
			view.setText(values[0]);
		}
	}

	@Override
	protected ClassLoader doInBackground(Void... arg0) {
		if (DroiubyBootstrap.requireUpdate(context)) {
			Log.d(this.getClass().toString(),"new version. updating...");
		}
		
		try {
			File stdlib = new File(DroiubyBootstrap.getVendorPath(context), "stdlib");
			if (stdlib.mkdirs() || DroiubyBootstrap.requireUpdate(context)) {
				Log.d(this.getClass().toString(), "unpacking vendor libraries");
				publishProgress("unpacking vendor libraries");
				Log.d(this.getClass().toString(), "unpacking to vendor");
				BufferedInputStream bis = new BufferedInputStream(context
						.getAssets().open("ruby_stdlib.jar"));
				unpackZip(bis, stdlib.getCanonicalPath());
			}
			
			File frameworkDir = new File(DroiubyBootstrap.getVendorPath(context), "framework");
			if (!frameworkDir.exists() || DroiubyBootstrap.requireUpdate(context)) {
				frameworkDir.mkdir();
				publishProgress("unpacking framework");
				BufferedInputStream bis = new BufferedInputStream(context
						.getAssets().open("framework.jar"));
				unpackZip(bis, frameworkDir.getCanonicalPath());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		if (envelopedLoader == null) {
			// Internal storage where the DexClassLoader writes the optimized dex
			// file to
			final File optimizedDexOutputPath = context.getDir("outdex",
					Context.MODE_PRIVATE);
			
			envelopedLoader = context.getClassLoader();
			for (String name : libraries) {
				Log.d(this.getClass().toString(), "loading = " + name);
				publishProgress("loading " + name);
				File storagePath = DroiubyBootstrap.loadSecondaryDex(context,
						name);
				envelopedLoader = new DexClassLoader(
						storagePath.getAbsolutePath(),
						optimizedDexOutputPath.getAbsolutePath(), null,
						envelopedLoader);
				Log.d(this.getClass().toString(), "done.");
			}
		}
		
		DroiubyBootstrap.markAsUpdated(context);
		
		return envelopedLoader;
	}

	public static boolean unpackZip(InputStream is, String outputdir) {
		ZipInputStream zis;
		try {
			String filename;

			zis = new ZipInputStream(new BufferedInputStream(is));
			ZipEntry ze;
			byte[] buffer = new byte[1024];
			int count;

			if (!outputdir.endsWith(File.separator)) {
				outputdir = outputdir + File.separator;
			}

			while ((ze = zis.getNextEntry()) != null) {
				filename = ze.getName();
				Log.d(DroiubyBootstrap.class.toString(), "processing "
						+ filename);
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
	
	@Override
	protected void onPostExecute(ClassLoader cl) {
		Class<?> libProviderClazz = null;
		// Load the library.
		try {
			Log.d(this.getClass().toString(), "loading helper");
			libProviderClazz = cl
					.loadClass("com.droiuby.client.core.DroiubyHelper");
			
			DroiubyBootstrap.classLoader = cl;
			DroiubyBootstrap.libProviderClazz = libProviderClazz;
			
			DroiubyHelperInterface helper = (DroiubyHelperInterface) libProviderClazz
					.newInstance();
			Log.d(this.getClass().toString(), "new instance loaded");
			helper.setActivity(context);
			helper.setLoader(cl);
			Log.d(this.getClass().toString(), "done.");
			listener.onReady(helper);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		// Cast the return object to the library interface so that the
		// caller can directly invoke methods in the interface.
		// Alternatively, the caller can invoke methods through reflection,
		// which is more verbose.

	}

}

public class DroiubyBootstrap {

	public static final String SECONDARY_DEX_NAME = "secondary_dex.jar";
	public static final String JRUBY_DEX_NAME = "large_dex.jar";
	public static final int BUF_SIZE = 8 * 1024;
	public static Class<?> libProviderClazz;
	public static ClassLoader classLoader;
	
	public static DroiubyHelperInterface getHelperInstance() {
		try {
			return (DroiubyHelperInterface) libProviderClazz
					.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static boolean requireUpdate(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("version",
				Context.MODE_PRIVATE);
		String currentVersion = prefs.getString("version", "");
		PackageInfo pInfo;
		try {
			pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			String newVersion = pInfo.versionName;
			return !currentVersion.equals(newVersion);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void markAsUpdated(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("version",
				Context.MODE_PRIVATE);
		
		PackageInfo pInfo;
		try {
			pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			String newVersion = pInfo.versionName;
			prefs.edit().putString("version", newVersion).commit();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

	}
	
	public static File getVendorPath(Context context) {
		return context.getDir("vendor",
					Context.MODE_PRIVATE);
	}
	
	public static File getDexPath(Context context) {
			return context.getDir("dex",
					Context.MODE_PRIVATE);
	}
	
	
	public static LibraryBootstrapTask bootstrapEnvironment(Activity context,
			OnEnvironmentReady listener) {
		String dexnames[] = { JRUBY_DEX_NAME, SECONDARY_DEX_NAME };
		LibraryBootstrapTask library = new LibraryBootstrapTask(context,
				dexnames, listener);
		return library;
	}

	public static File loadSecondaryDex(Context context, String name) {
		// Before the secondary dex file can be processed by the DexClassLoader,
		// it has to be first copied from asset resource to a storage location.
		File dexInternalStoragePath = new File(DroiubyBootstrap.getDexPath(context), name);
		BufferedInputStream bis = null;
		OutputStream dexWriter = null;
		if (!dexInternalStoragePath.exists() || requireUpdate(context)) {
			try {
				bis = new BufferedInputStream(context.getAssets().open(name));
				dexWriter = new BufferedOutputStream(new FileOutputStream(
						dexInternalStoragePath));
				byte[] buf = new byte[BUF_SIZE];
				int len;
				while ((len = bis.read(buf, 0, BUF_SIZE)) > 0) {
					dexWriter.write(buf, 0, len);
				}
				dexWriter.close();
				bis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return dexInternalStoragePath;
	}

}
