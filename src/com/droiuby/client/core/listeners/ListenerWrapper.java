package com.droiuby.client.core.listeners;

import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;

import android.util.Log;
import android.view.View;

public abstract class ListenerWrapper {

	IRubyObject block;
	ScriptingContainer container;

	public ListenerWrapper(ScriptingContainer container, IRubyObject block) {
		this.block = block;
		this.container = container;
	}

	protected boolean execute(Object view) {
		try {
			container.put("_receiver", block);
			container.put("_view", view);
			container.runScriptlet("_receiver.call(_view)");
			return true;
		} catch (org.jruby.embed.EvalFailedException e) {
			Log.d(this.getClass().toString(), "eval failed: " + e.getMessage());
			e.printStackTrace();
		} catch (org.jruby.embed.ParseFailedException e) {
			e.printStackTrace();
		}
		return false;
	}
}
