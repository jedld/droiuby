package com.droiuby.client.core.listeners;

import org.jruby.Ruby;
import org.jruby.RubyBoolean;
import org.jruby.RubyNil;
import org.jruby.RubyProc;
import org.jruby.embed.ScriptingContainer;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;

import com.droiuby.client.core.ExecutionBundle;

import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;

public class FocusChangeListenerWrapper extends ListenerWrapper implements
		OnFocusChangeListener {

	public FocusChangeListenerWrapper(ExecutionBundle bundle, RubyProc block) {
		super(bundle, block);
	}

	public void onFocusChange(View view, boolean hasFocus) {
		try {
			Ruby runtime = container.getProvider().getRuntime();
			IRubyObject wrapped_view = JavaUtil
					.convertJavaToRuby(runtime, view);
			IRubyObject wrapped_has_focus = RubyBoolean.newBoolean(runtime,
					hasFocus);
			IRubyObject args[] = new IRubyObject[] { wrapped_view,
					wrapped_has_focus };
			block.call19(runtime.getCurrentContext(), args, null);
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