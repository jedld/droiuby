package com.droiuby.client.core.async;

import org.jruby.RubyProc;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;

import android.os.AsyncTask;
import android.util.Log;

import com.droiuby.client.core.ExecutionBundle;

public class AsyncWrapper extends AsyncTask<Object, Object, IRubyObject> {

	RubyProc background_task, post_execute, pre_execute;

	public Object getBackground_task() {
		return background_task;
	}

	public void setBackground_task(RubyProc background_task) {
		this.background_task = background_task;
	}

	public Object getPost_execute() {
		return post_execute;
	}

	public void setPost_execute(RubyProc post_execute) {
		this.post_execute = post_execute;
	}

	public RubyProc getPre_execute() {
		return pre_execute;
	}

	public void setPre_execute(RubyProc pre_execute) {
		this.pre_execute = pre_execute;
	}

	public ScriptingContainer getContainer() {
		return container;
	}

	public void setContainer(ScriptingContainer container) {
		this.container = container;
	}

	ScriptingContainer container;
	private ExecutionBundle bundle;

	public AsyncWrapper(ExecutionBundle bundle) {
		this.bundle = bundle;
		this.container = bundle.getContainer();
	}

	@Override
	protected void onPostExecute(IRubyObject result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		if (post_execute != null) {
			long start = System.currentTimeMillis();
			Log.d(this.getClass().toString(), "Executing Post");
			try {
				IRubyObject args[] = new IRubyObject[] { result };
				post_execute.call19(container.getProvider().getRuntime()
						.getCurrentContext(), args, null);
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
				IRubyObject args[] = new IRubyObject[] {};
				pre_execute.call19(container.getProvider().getRuntime()
						.getCurrentContext(), args, null);
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
	protected IRubyObject doInBackground(Object... params) {
		// TODO Auto-generated method stub
		if (background_task != null) {
			long start = System.currentTimeMillis();
			Log.d(this.getClass().toString(), "Executing background");
			try {
				IRubyObject args[] = new IRubyObject[] {};
				return background_task.call19(container.getProvider()
						.getRuntime().getCurrentContext(), args, null);
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
