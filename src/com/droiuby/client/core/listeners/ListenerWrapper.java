package com.droiuby.client.core.listeners;

import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;

import com.droiuby.client.core.ExecutionBundle;

import android.util.Log;
import android.view.View;

public abstract class ListenerWrapper {

	IRubyObject block;
	ExecutionBundle bundle;
	ScriptingContainer container;

	public ListenerWrapper(ExecutionBundle bundle, IRubyObject block) {
		this.block = block;
		this.bundle = bundle;
		this.container = bundle.getContainer();
	}

	protected boolean execute(Object view) {
		try {
			container.put("_receiver", block);
			container.put("_view", view);
			return (Boolean)container.runScriptlet("!!_receiver.call(wrap_native_view(_view))");
		} catch (org.jruby.embed.EvalFailedException e) {
			Log.d(this.getClass().toString(), "eval failed: " + e.getMessage());
			e.printStackTrace();
			bundle.addError(e.getMessage());
		} catch (org.jruby.embed.ParseFailedException e) {
			e.printStackTrace();
			bundle.addError(e.getMessage());
		}
		return false;
	}
}
