package com.dayosoft.activeapp.core.listeners;

import org.jruby.RubyBoolean;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;

import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;

public class ViewOnLongClickListener extends ListenerWrapper implements
		OnLongClickListener {

	public ViewOnLongClickListener(ScriptingContainer container,
			IRubyObject block) {
		super(container, block);
	}

	public boolean onLongClick(View view) {
		try {
			container.put("_receiver", block);
			container.put("_view", view);

			return (Boolean) container.runScriptlet("!!_receiver.call(_view)");
		} catch (org.jruby.embed.EvalFailedException e) {
			Log.d(this.getClass().toString(), "eval failed: " + e.getMessage());
			e.printStackTrace();
		} catch (org.jruby.embed.ParseFailedException e) {
			e.printStackTrace();
		}
		return false;
	}

}
