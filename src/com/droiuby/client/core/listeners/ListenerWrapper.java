package com.droiuby.client.core.listeners;

import org.jruby.RubyBoolean;
import org.jruby.RubyNil;
import org.jruby.RubyProc;
import org.jruby.embed.ScriptingContainer;
import org.jruby.javasupport.JavaObject;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;

import com.droiuby.client.core.ExecutionBundle;
import com.droiuby.client.core.scripting.ScriptingEngine;

import android.util.Log;
import android.view.View;

public abstract class ListenerWrapper {

	RubyProc block;
	ExecutionBundle bundle;
	ScriptingEngine container;

	public ListenerWrapper(ExecutionBundle bundle, RubyProc block) {
		this.block = block;
		this.bundle = bundle;
		this.container = bundle.getContainer();
	}

	protected boolean toBoolean(IRubyObject object) {
		if (object.isNil())
			return false;
		if (object.isTrue())
			return true;
		if (object instanceof RubyBoolean) {
			if (((RubyBoolean) object).isFalse())
				return false;
		}
		return true;
	}
	
	protected boolean execute(Object view) {
		try {
			IRubyObject wrapped_view = JavaUtil.convertJavaToRuby(container
					.getProvider().getRuntime(), view);
			IRubyObject args[] = new IRubyObject[] { wrapped_view };
			IRubyObject return_value = block.call19(container.getProvider()
					.getRuntime().getCurrentContext(), args, null);
			return toBoolean(return_value);
		} catch (org.jruby.exceptions.RaiseException e) {
			Log.d(this.getClass().toString(), "eval failed: " + e.getMessage());
			e.printStackTrace();
			bundle.addError(e.getMessage());
		}
		return false;
	}
}
