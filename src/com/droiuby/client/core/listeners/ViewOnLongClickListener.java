package com.droiuby.client.core.listeners;

import org.jruby.RubyBoolean;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;

import com.droiuby.client.core.ExecutionBundle;

import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;

public class ViewOnLongClickListener extends ListenerWrapper implements
		OnLongClickListener {

	public ViewOnLongClickListener(ExecutionBundle bundle,
			IRubyObject block) {
		super(bundle, block);
	}

	public boolean onLongClick(View view) {
		try {
			container.put("_receiver", block);
			container.put("_view", view);
			return (Boolean) container.runScriptlet("!!_receiver.call(_view)");
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
