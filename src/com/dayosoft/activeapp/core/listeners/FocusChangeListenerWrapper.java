package com.dayosoft.activeapp.core.listeners;

import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;

import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;

public class FocusChangeListenerWrapper extends ListenerWrapper implements OnFocusChangeListener {

	public FocusChangeListenerWrapper(ScriptingContainer container,
			IRubyObject block) {
		super(container, block);
	}

	public void onFocusChange(View view, boolean hasFocus) {
			try {
				container.put("_receiver", block);
				container.put("_view", view);
				container.put("_has_focus", hasFocus);
				container.runScriptlet("_receiver.call(_view, _has_focus)");
			} catch (org.jruby.embed.EvalFailedException e) {
				Log.d(this.getClass().toString(), "eval failed: " + e.getMessage());
				e.printStackTrace();
			} catch (org.jruby.embed.ParseFailedException e) {
				e.printStackTrace();
			}
		}

}