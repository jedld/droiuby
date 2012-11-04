package com.droiuby.client.core.listeners;

import org.jruby.RubyNil;
import org.jruby.RubyProc;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.droiuby.client.core.ExecutionBundle;

public class OnTouchListenerWrapper extends ListenerWrapper implements OnTouchListener{

	public OnTouchListenerWrapper(ExecutionBundle bundle, RubyProc block) {
		super(bundle, block);
	}

	public boolean onTouch(View view, MotionEvent motionEvent) {
		return execute(view, motionEvent);
	}
	
	protected boolean execute(Object view, MotionEvent motionEvent) {
		try {
			IRubyObject wrapped_view = JavaUtil.convertJavaToRuby(container.getProvider().getRuntime(), view);
			IRubyObject wrapped_motion_event  = JavaUtil.convertJavaToRuby(container.getProvider().getRuntime(), motionEvent);
			IRubyObject args[] =new IRubyObject[] { wrapped_view, wrapped_motion_event };
			IRubyObject return_value = block.call19(container.getProvider().getRuntime().getCurrentContext(), args , null);
			return this.toBoolean(return_value);
		} catch (org.jruby.exceptions.RaiseException e) {
			Log.d(this.getClass().toString(), "eval failed: " + e.getMessage());
			e.printStackTrace();
			bundle.addError(e.getMessage());
		}
		return false;
	}
}
