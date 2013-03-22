package com.droiuby.client.core.async;

import org.jruby.RubyProc;
import org.jruby.embed.ScriptingContainer;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;

import com.droiuby.client.core.ExecutionBundle;
import com.droiuby.client.core.scripting.ScriptObject;
import com.droiuby.client.core.scripting.ScriptProc;
import com.droiuby.client.core.scripting.ScriptingEngine;

import android.os.AsyncTask;
import android.util.Log;

public class AsyncWrapper extends AsyncTask<Object, Object, ScriptObject> {

	ScriptProc background_task, post_execute;
	ScriptProc pre_execute;

	public Object getBackground_task() {
		return background_task;
	}

	public void setBackground_task(ScriptProc background_task) {
		this.background_task = background_task;
	}

	public Object getPost_execute() {
		return post_execute;
	}

	public void setPost_execute(ScriptProc post_execute) {
		this.post_execute = post_execute;
	}

	public ScriptProc getPre_execute() {
		return pre_execute;
	}

	public void setPre_execute(ScriptProc pre_execute) {
		this.pre_execute = pre_execute;
	}

	public ScriptingEngine getContainer() {
		return container;
	}

	public void setContainer(ScriptingEngine container) {
		this.container = container;
	}

	ScriptingEngine container;
	private ExecutionBundle bundle;

	public AsyncWrapper(ExecutionBundle bundle) {
		this.bundle = bundle;
		this.container = bundle.getContainer();
	}

	@Override
	protected void onPostExecute(ScriptObject result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		if (post_execute != null) {
			long start = System.currentTimeMillis();
			Log.d(this.getClass().toString(), "Executing Post");
			try {
				ScriptObject args[] = new ScriptObject[] { result };
				post_execute.call(container.getCurrentContext(), args, null);
			} catch (org.jruby.exceptions.RaiseException e) {
				e.printStackTrace();
				bundle.addError(e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				bundle.addError(e.getMessage());
			} finally {
				long duration = System.currentTimeMillis() - start;
				Log.d(this.getClass().toString(), "done elapsed = " + duration);
			}
		}
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		if (pre_execute != null) {
			try {
				ScriptObject args[] = new ScriptObject[] {};
				pre_execute.call(container.getCurrentContext(), args, null);
			} catch (org.jruby.exceptions.RaiseException e) {
				e.printStackTrace();
				bundle.addError(e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				bundle.addError(e.getMessage());
			}
		}
	}

	@Override
	protected ScriptObject doInBackground(Object... params) {
		// TODO Auto-generated method stub
		if (background_task != null) {
			long start = System.currentTimeMillis();
			Log.d(this.getClass().toString(), "Executing background");
			try {
				ScriptObject args[] = new ScriptObject[] {};
				return background_task.call(container.getCurrentContext(), args, null);
			} catch (org.jruby.exceptions.RaiseException e) {
				e.printStackTrace();
				bundle.addError(e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				bundle.addError(e.getMessage());
			} finally {
				long duration = System.currentTimeMillis() - start;
				Log.d(this.getClass().toString(), "done elapsed = " + duration);
			}
		}
		return null;
	}

}
