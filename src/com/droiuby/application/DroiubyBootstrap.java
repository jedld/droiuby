package com.droiuby.application;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.droiuby.interfaces.DroiubyHelperInterface;

import dalvik.system.DexClassLoader;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

class LibraryBootstrapTask extends
		AsyncTask<Void, Void, DroiubyHelperInterface> {

	Context context;
	String libraries[];
	OnEnvironmentReady listener;

	public LibraryBootstrapTask(Context context, String libraries[],
			OnEnvironmentReady listener) {
		this.context = context;
		this.libraries = libraries;
		this.listener = listener;
	}

	@Override
	protected DroiubyHelperInterface doInBackground(Void... arg0) {
		// Internal storage where the DexClassLoader writes the optimized dex
		// file to
		final File optimizedDexOutputPath = context.getDir("outdex",
				Context.MODE_PRIVATE);
		ClassLoader cl = context.getClassLoader();
		for (String name : libraries) {
			Log.d(this.getClass().toString(), "loading = " + name);
			File storagePath = DroiubyBootstrap.loadSecondaryDex(context, name);
			cl = new DexClassLoader(storagePath.getAbsolutePath(),
					optimizedDexOutputPath.getAbsolutePath(), null, cl);
		}

		Class libProviderClazz = null;
		// Load the library.
		try {
			libProviderClazz = cl
					.loadClass("com.droiuby.client.core.DroiubyHelper");
			return (DroiubyHelperInterface) libProviderClazz.newInstance();
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
		return null;
	}

	@Override
	protected void onPostExecute(DroiubyHelperInterface result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		listener.onReady(result);
	}

}

public class DroiubyBootstrap {

	public static final String SECONDARY_DEX_NAME = "secondary_dex.jar";
	public static final String JRUBY_DEX_NAME = "large_dex.jar";
	public static final int BUF_SIZE = 8 * 1024;

	public static void bootstrapEnvironment(Context context,
			OnEnvironmentReady listener) {
		String dexnames[] = { SECONDARY_DEX_NAME, JRUBY_DEX_NAME };
		LibraryBootstrapTask library = new LibraryBootstrapTask(context,
				dexnames, listener);
		library.execute();
	}

	public static File loadSecondaryDex(Context context, String name) {
		// Before the secondary dex file can be processed by the DexClassLoader,
		// it has to be first copied from asset resource to a storage location.
		File dexInternalStoragePath = new File(context.getDir("dex",
				Context.MODE_PRIVATE), name);
		BufferedInputStream bis = null;
		OutputStream dexWriter = null;

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
		return dexInternalStoragePath;
	}

}
