package com.droiuby.client.core.listeners;

import org.jruby.runtime.builtin.IRubyObject;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.droiuby.client.core.ExecutionBundle;

public class OnTouchListenerWrapper extends ListenerWrapper implements OnTouchListener{

	public OnTouchListenerWrapper(ExecutionBundle bundle, IRubyObject block) {
		super(bundle, block);
	}

	public boolean onTouch(View view, MotionEvent motionEvent) {
		return execute(view, motionEvent);
	}
	
	protected boolean execute(Object view, MotionEvent motionEvent) {
		try {
			container.put("_receiver", block);
			container.put("_view", view);
			container.put("_motion_event", motionEvent);
			return (Boolean)container.runScriptlet("!!_receiver.call(wrap_native_view(_view), wrap_motion_event(_motion_event))");
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
