package com.dayosoft.activeapp.core.async;

import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;

import android.os.AsyncTask;
import android.util.Log;

public class AsyncWrapper extends AsyncTask<Object, Object, Object>{

	Object background_task, post_execute, pre_execute;
	

	public Object getBackground_task() {
		return background_task;
	}

	public void setBackground_task(Object background_task) {
		this.background_task = background_task;
	}

	public Object getPost_execute() {
		return post_execute;
	}

	public void setPost_execute(Object post_execute) {
		this.post_execute = post_execute;
	}

	public Object getPre_execute() {
		return pre_execute;
	}

	public void Object(IRubyObject pre_execute) {
		this.pre_execute = pre_execute;
	}

	public ScriptingContainer getContainer() {
		return container;
	}

	public void setContainer(ScriptingContainer container) {
		this.container = container;
	}


	ScriptingContainer container;
	
	public AsyncWrapper(ScriptingContainer container) {
		this.container = container;
	}
		
	@Override
	protected void onPostExecute(Object result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		if (post_execute != null) {
			String targetName = getTargetName();
			Log.d(this.getClass().toString(), "post executing ..." + targetName);
			container.put(targetName, post_execute);
			container.put(targetName+"_result", result);
			container.runScriptlet(targetName + ".call("+targetName+"_result)");
		}
	}

	private String getTargetName() {
		return "_target_" + Thread.currentThread().getId();
	}
	
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		if (pre_execute != null) {
			String targetName = getTargetName();
			container.put(targetName, pre_execute);
			container.runScriptlet(targetName + ".call");
		}
	}


	@Override
	protected Object doInBackground(Object... params) {
		// TODO Auto-generated method stub
		if (background_task != null) {
			String targetName = getTargetName();
			Log.d(this.getClass().toString(), "background executing ..." + targetName);
			container.put(targetName, background_task);
			return container.runScriptlet(targetName + ".call");
		}
		return null;
	}

}
