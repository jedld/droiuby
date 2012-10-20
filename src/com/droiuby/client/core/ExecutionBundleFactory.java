package com.droiuby.client.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jruby.CompatVersion;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;

import android.content.Context;
import android.util.Log;

public class ExecutionBundleFactory {

	static ExecutionBundleFactory instance;

	HashMap<String, ExecutionBundle> bundles = new HashMap<String, ExecutionBundle>();

	protected ExecutionBundleFactory() {
	}

	public static ExecutionBundleFactory getInstance() {
		if (instance == null) {
			instance = new ExecutionBundleFactory();
		}
		return instance;
	}

	public ExecutionBundle getNewScriptingContainer(Context context,
			String namespace) {

		if (bundles.containsKey(namespace)) {
			Log.d(this.getClass().toString(),"Reuse bundle for " + namespace);
			return bundles.get(namespace);
		} else {
			ExecutionBundle bundle = new ExecutionBundle();
			bundles.put(namespace, bundle);
			ScriptingContainer container = new ScriptingContainer(
					LocalContextScope.SINGLETHREAD,
					LocalVariableBehavior.PERSISTENT);
			RubyContainerPayload payload = new RubyContainerPayload();
			payload.setContainer(container);
			bundle.setContainer(container);
			bundle.setPayload(payload);
			return bundle;
		}
	}

}
