package com.droiuby.client.core.listeners;

import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;

import com.droiuby.client.core.ExecutionBundle;

import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;

public class FocusChangeListenerWrapper extends ListenerWrapper implements OnFocusChangeListener {

	public FocusChangeListenerWrapper(ExecutionBundle bundle,
			IRubyObject block) {
		super(bundle, block);
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
				bundle.addError(e.getMessage());
			} catch (org.jruby.embed.ParseFailedException e) {
				e.printStackTrace();
				bundle.addError(e.getMessage());
			}
		}

}