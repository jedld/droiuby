package com.droiuby.client.core.listeners;

import org.jruby.embed.ScriptingContainer;

import android.view.View;
import android.view.View.OnClickListener;

public class OnClickListenerBridge implements OnClickListener {

	ScriptingContainer scriptingContainer;

	public OnClickListenerBridge(ScriptingContainer scriptingContainer,
			int view_id) {
		this.scriptingContainer = scriptingContainer;
	}

	public void onClick(View v) {
		scriptingContainer.put("native_view", v);
		scriptingContainer.runScriptlet("$framework.on_click"
				+ "(native_view)");
		scriptingContainer.remove("native_view");
	}

}
