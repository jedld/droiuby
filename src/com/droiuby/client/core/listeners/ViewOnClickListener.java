package com.droiuby.client.core.listeners;

import org.jruby.RubyProc;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;

import com.droiuby.client.core.ExecutionBundle;

import android.view.View;
import android.view.View.OnClickListener;

public class ViewOnClickListener extends ListenerWrapper implements
		OnClickListener {

	public ViewOnClickListener(ExecutionBundle bundle, RubyProc block) {
		super(bundle, block);
	}

	public void onClick(View view) {
		execute(view);
	}

}
