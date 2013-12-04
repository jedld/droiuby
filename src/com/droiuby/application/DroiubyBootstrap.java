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

import com.droiuby.interfaces.DroiubyHelperInterface;

import dalvik.system.DexClassLoader;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

class LibraryBootstrapTask extends AsyncTask<Void, Void, ClassLoader> {

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
		// TODO Auto-generated method stub
		super.onPreExecute();
		int default_orientation = context.getResources().getConfiguration().orientation;
		if (default_orientation == Configuration.ORIENTATION_LANDSCAPE) {
			context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}

	@Override
	protected ClassLoader doInBackground(Void... arg0) {
		try {
			Log.d(this.getClass().toString(), "unpacking vendor libraries");

			File stdlib = new File(context.getDir("vendor",
					Context.MODE_PRIVATE), "stdlib");
			if (stdlib.mkdirs()) {
				Log.d(this.getClass().toString(), "unpacking to vendor");
				BufferedInputStream bis = new BufferedInputStream(context
						.getAssets().open("ruby_stdlib.jar"));
				unpackZip(bis, stdlib.getCanonicalPath());
			}
			
			File frameworkDir = new File(context.getDir("vendor",
					Context.MODE_PRIVATE), "framework");
			if (!frameworkDir.exists()) {
				frameworkDir.mkdir();
				BufferedInputStream bis = new BufferedInputStream(context
						.getAssets().open("framework.jar"));
				unpackZip(bis, frameworkDir.getCanonicalPath());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Internal storage where the DexClassLoader writes the optimized dex
		// file to
		final File optimizedDexOutputPath = context.getDir("outdex",
				Context.MODE_PRIVATE);
		if (envelopedLoader == null) {
			envelopedLoader = context.getClassLoader();
			for (String name : libraries) {
				Log.d(this.getClass().toString(), "loading = " + name);
				File storagePath = DroiubyBootstrap.loadSecondaryDex(context,
						name);
				envelopedLoader = new DexClassLoader(
						storagePath.getAbsolutePath(),
						optimizedDexOutputPath.getAbsolutePath(), null,
						envelopedLoader);
				Log.d(this.getClass().toString(), "done.");
			}
		}
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
		Class libProviderClazz = null;
		// Load the library.
		try {
			Log.d(this.getClass().toString(), "loading helper");
			libProviderClazz = cl
					.loadClass("com.droiuby.client.core.DroiubyHelper");
			DroiubyHelperInterface helper = (DroiubyHelperInterface) libProviderClazz
					.newInstance();
			Log.d(this.getClass().toString(), "new instance loaded");
			helper.setActivity(context);
			helper.setLoader(cl);
			Log.d(this.getClass().toString(), "done.");
			listener.onReady(helper);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
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
		File dexInternalStoragePath = new File(context.getDir("dex",
				Context.MODE_PRIVATE), name);
		BufferedInputStream bis = null;
		OutputStream dexWriter = null;
		if (!dexInternalStoragePath.exists()) {
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return dexInternalStoragePath;
	}

}
