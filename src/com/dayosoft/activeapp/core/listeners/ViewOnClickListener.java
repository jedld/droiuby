package com.dayosoft.activeapp.core.listeners;

import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;

import android.view.View;
import android.view.View.OnClickListener;

public class ViewOnClickListener extends ListenerWrapper implements OnClickListener {

	public ViewOnClickListener(ScriptingContainer container ,IRubyObject block) {
		super(container,block);
	}
	
	public void onClick(View view) {
		execute(view);
	}

}
