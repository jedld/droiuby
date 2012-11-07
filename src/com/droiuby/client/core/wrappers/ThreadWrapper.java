package com.droiuby.client.core.wrappers;

import org.jruby.RubyProc;
import org.jruby.runtime.builtin.IRubyObject;

import android.util.Log;

import com.droiuby.client.core.ExecutionBundle;

public class ThreadWrapper extends Thread {
	@Override
	public void run() {
		
		super.run();
			long start = System.currentTimeMillis();
			Log.d(this.getClass().toString(), "Executing background");
			try {
				IRubyObject args[] = new IRubyObject[] {};
				block.call19(bundle.getContainer().getProvider()
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

	RubyProc block;
	ExecutionBundle bundle;
	
	public ThreadWrapper(RubyProc block, ExecutionBundle bundle) {
		this.block = block;
		this.bundle = bundle;
	}
	
	

}
