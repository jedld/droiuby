package com.droiuby.client.core.wrappers;

import org.jruby.Ruby;
import org.jruby.RubyProc;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;

import com.droiuby.client.core.ExecutionBundle;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SurfaceViewWrapper extends SurfaceView implements
		SurfaceHolder.Callback {

	RubyProc surfaceCreatedBlock, surfaceChangedBlock, surfaceDestroyedBlock;
	ExecutionBundle bundle;

	public RubyProc getSurfaceDestroyedBlock() {
		return surfaceDestroyedBlock;
	}

	public void setSurfaceDestroyedBlock(RubyProc surfaceDestroyedBlock) {
		this.surfaceDestroyedBlock = surfaceDestroyedBlock;
	}

	Ruby rubyRuntime;

	public RubyProc getSurfaceCreatedBlock() {
		return surfaceCreatedBlock;
	}

	public void setSurfaceCreatedBlock(RubyProc surfaceCreatedBlock) {
		this.surfaceCreatedBlock = surfaceCreatedBlock;
	}

	public RubyProc getSurfaceChangedBlock() {
		return surfaceChangedBlock;
	}

	public void setSurfaceChangedBlock(RubyProc surfaceChangedBlock) {
		this.surfaceChangedBlock = surfaceChangedBlock;
	}

	public SurfaceViewWrapper(Context context, ExecutionBundle bundle) {
		super(context);
		getHolder().addCallback(this);
		this.bundle = bundle;
		this.rubyRuntime = bundle.getContainer().getProvider().getRuntime();
	}

	public void surfaceChanged(SurfaceHolder surface, int format, int width,
			int height) {
		if (surfaceCreatedBlock != null) {
			try {
				IRubyObject wrapped_canvas = JavaUtil.convertJavaToRuby(
						rubyRuntime, new SurfaceViewHolderWrapper(surface));
				IRubyObject wrapped_format = JavaUtil.convertJavaToRuby(
						rubyRuntime, format);
				IRubyObject wrapped_width = JavaUtil.convertJavaToRuby(
						rubyRuntime, width);
				IRubyObject wrapped_height = JavaUtil.convertJavaToRuby(
						rubyRuntime, height);
				IRubyObject args[] = new IRubyObject[] { wrapped_canvas,
						wrapped_format, wrapped_width, wrapped_height };
				surfaceCreatedBlock.call19(rubyRuntime.getCurrentContext(),
						args, null);
			} catch (org.jruby.exceptions.RaiseException e) {
				bundle.addError(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public void surfaceCreated(SurfaceHolder surface) {
		if (surfaceCreatedBlock != null) {
			try {
				IRubyObject wrapped_canvas = JavaUtil.convertJavaToRuby(
						rubyRuntime,new SurfaceViewHolderWrapper(surface));
				IRubyObject args[] = new IRubyObject[] { wrapped_canvas };
				surfaceCreatedBlock.call19(rubyRuntime.getCurrentContext(),
						args, null);
			} catch (org.jruby.exceptions.RaiseException e) {
				bundle.addError(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public void surfaceDestroyed(SurfaceHolder surface) {
		if (surfaceDestroyedBlock != null) {
			try {
				IRubyObject wrapped_canvas = JavaUtil.convertJavaToRuby(
						rubyRuntime, new SurfaceViewHolderWrapper(surface));
				IRubyObject args[] = new IRubyObject[] { wrapped_canvas };
				surfaceDestroyedBlock.call19(rubyRuntime.getCurrentContext(), args,
						null);
			} catch (org.jruby.exceptions.RaiseException e) {
				bundle.addError(e.getMessage());
				e.printStackTrace();
			}
		}
	}

}
