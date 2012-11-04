package com.droiuby.client.core.listeners;

import org.jruby.RubyProc;

import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;

import com.droiuby.client.core.ExecutionBundle;

public class ViewOnLongClickListener extends ListenerWrapper implements
		OnLongClickListener {

	public ViewOnLongClickListener(ExecutionBundle bundle,
			RubyProc block) {
		super(bundle, block);
	}

	public boolean onLongClick(View view) {
		try {
			return (Boolean) this.execute(view);
		} catch (org.jruby.exceptions.RaiseException e) {
			Log.d(this.getClass().toString(), "eval failed: " + e.getMessage());
			e.printStackTrace();
			bundle.addError(e.getMessage());
		}
		return false;
	}

}
