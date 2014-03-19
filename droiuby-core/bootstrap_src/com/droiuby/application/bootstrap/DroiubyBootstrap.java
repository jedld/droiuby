package com.droiuby.application.bootstrap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import com.droiuby.interfaces.DroiubyHelperInterface;


public class DroiubyBootstrap {

	public static final String SECONDARY_DEX_NAME = "droiuby-core.jar";
	public static final String JRUBY_DEX_NAME = "jruby.jar";
	public static final String JRUBY_DEPS = "jruby-dependencies.jar";
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
	
	
	public static LibraryBootstrapTask bootstrapEnvironment(Activity context, int progressUpdateViewId,
			OnEnvironmentReady listener) {
		String dexnames[] = { JRUBY_DEPS, JRUBY_DEX_NAME, SECONDARY_DEX_NAME };
		LibraryBootstrapTask library = new LibraryBootstrapTask(context,
				dexnames, progressUpdateViewId, listener);
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
