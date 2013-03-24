package com.droiuby.client.core;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jruby.CompatVersion;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;

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

	private ExecutionBundle newBundle(Activity context, String namespace) {
		ExecutionBundle bundle = new ExecutionBundle();
		ScriptingContainer container = new ScriptingContainer(
				LocalContextScope.SINGLETHREAD,
				LocalVariableBehavior.PERSISTENT);
		RubyContainerPayload payload = new RubyContainerPayload();
		payload.setCurrentActivity(context);
		payload.setContainer(container);
		container.setObjectSpaceEnabled(false);
		container.setClassLoader(loader);
		container.setCompatVersion(CompatVersion.RUBY1_9);
		String data_dir = context.getApplicationInfo().dataDir;
		Log.d(this.getClass().toString(), "data directory in " + data_dir);
		container.setHomeDirectory(data_dir + "/jruby/home");
		List<String> loadPaths = new ArrayList<String>();
		loadPaths.add(data_dir + "/jruby/vendor");
		loadPaths.add(data_dir + "/jruby/vendor/lib");
		container.setLoadPaths(loadPaths);
		bundle.setLibraryInitialized(false);
		bundle.setContainer(container);
		bundle.setPayload(payload);
		return bundle;

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

	public ExecutionBundle getNewScriptingContainer(Activity context,
			String namespace) {
		if (bundles.containsKey(namespace)) {

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

}
