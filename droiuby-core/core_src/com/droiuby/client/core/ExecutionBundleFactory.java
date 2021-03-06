package com.droiuby.client.core;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jruby.CompatVersion;
import org.jruby.RubyInstanceConfig.CompileMode;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;

import com.droiuby.application.bootstrap.DroiubyBootstrap;
import com.droiuby.client.core.utils.Utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

public class ExecutionBundleFactory {

	static ExecutionBundleFactory instance;

	HashMap<String, WeakReference<ExecutionBundle>> bundles = new HashMap<String, WeakReference<ExecutionBundle>>();

	private ClassLoader loader;

	protected ExecutionBundleFactory(ClassLoader loader) {
		this.loader = loader;
	}

	public static ExecutionBundleFactory getInstance(ClassLoader loader) {
		if (instance == null) {
			instance = new ExecutionBundleFactory(loader);
		}
		return instance;
	}

	private ExecutionBundle newBundle(Context context, String namespace) {
		ExecutionBundle bundle = new ExecutionBundle();

        System.setProperty("jruby.backtrace.style", "normal"); // normal raw full mri
        System.setProperty("jruby.bytecode.version", "1.6");
        System.setProperty("jruby.compile.mode", "OFF"); // OFF OFFIR JITIR? FORCE FORCEIR
        System.setProperty("jruby.interfaces.useProxy", "true");
        System.setProperty("jruby.ir.passes", "LocalOptimizationPass,DeadCodeElimination");
        System.setProperty("jruby.management.enabled", "false");
        System.setProperty("jruby.native.enabled", "false");
        System.setProperty("jruby.objectspace.enabled", "false");
        System.setProperty("jruby.rewrite.java.trace", "true");
        System.setProperty("jruby.thread.pooling", "true");
		
        // Used to enable JRuby to generate proxy classes
        System.setProperty("jruby.ji.proxyClassFactory", "org.ruboto.DalvikProxyClassFactory");
        System.setProperty("jruby.ji.upper.case.package.name.allowed", "true");
        System.setProperty("jruby.class.cache.path", context.getDir("dex", 0).getAbsolutePath());
  
		ScriptingContainer container = new ScriptingContainer(
				LocalContextScope.SINGLETHREAD,
				LocalVariableBehavior.PERSISTENT);
		
		RubyContainerPayload payload = new RubyContainerPayload();
		payload.setContainer(container);
		payload.setExecutionBundle(bundle);
		
		Map <String,String> environment = new HashMap<String,String>();
      
		container.setClassLoader(loader);
		String data_dir = context.getApplicationInfo().dataDir;
		Log.d(this.getClass().toString(), "data directory in " + data_dir);
		container.setHomeDirectory(data_dir + "/jruby/home");
		List<String> loadPaths = new ArrayList<String>();
		loadPaths.add(data_dir + "/jruby/vendor");
		loadPaths.add(data_dir + "/jruby/vendor/lib");
		File stdlib = new File(DroiubyBootstrap.getVendorPath(context),
				"stdlib");
		File frameworkDir = new File(DroiubyBootstrap.getVendorPath(context), "framework");
		try {
			loadPaths.add(frameworkDir.getCanonicalPath());
			loadPaths.add(stdlib.getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		container.setLoadPaths(loadPaths);
		bundle.setName(namespace);
		bundle.setLibraryInitialized(false);
		bundle.setContainer(container);
		bundle.setPayload(payload);
		return bundle;

	}

	public static String[] listActiveBundles() {
		ArrayList<String> result = new ArrayList<String>();
		if (instance != null) {
			HashMap<String, WeakReference<ExecutionBundle>> bundleList = instance.bundles;
			for (String name : bundleList.keySet()) {
				if (bundleList.get(name).get() != null) {
					result.add(name);
				}
			}
		}
		String[] arr = new String[result.size()];
		return result.toArray(arr);
	}

	public static ExecutionBundle getBundle(String namespace) {
		if (instance != null) {
			HashMap<String, WeakReference<ExecutionBundle>> bundleList = instance.bundles;
			if (bundleList.containsKey(namespace)) {
				ExecutionBundle bundle = bundleList.get(namespace).get();
				if (bundle != null) {
					return bundle;
				}
			}
		}
		Log.e(ExecutionBundleFactory.class.toString(),"No execution bundle instance!");
		return null;
	}

	public boolean bundleAvailableFor(String namespace) {
		if (bundles.containsKey(namespace)) {
			ExecutionBundle bundle = bundles.get(namespace).get();
			if (bundle != null) {
				return true;
			}
		}
		return false;
	}

	public ExecutionBundle getNewScriptingContainer(Context context,
			String namespace, boolean forceNew) {
		if (bundles.containsKey(namespace) && !forceNew) {
			ExecutionBundle bundle = bundles.get(namespace).get();
			if (bundle == null) {
				Log.d(this.getClass().toString(), "Bundle for " + namespace
						+ " cleared. creating new instance");
				bundle = newBundle(context, namespace);
				bundles.put(namespace, new WeakReference<ExecutionBundle>(
						bundle));
				return bundle;
			} else {
				Log.d(this.getClass().toString(), "Reuse bundle for "
						+ namespace);
				return bundle;
			}
		} else {
			Log.d(this.getClass().toString(), "Creating new bundle for "
					+ namespace);
			ExecutionBundle bundle = newBundle(context, namespace);
			bundles.put(namespace, new WeakReference<ExecutionBundle>(bundle));
			return bundle;
		}
	}

	public void removeBundle(String namespace) {
		if (bundles.containsKey(namespace)) {
			bundles.remove(namespace);
		}
	}

}
