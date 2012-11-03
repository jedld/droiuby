package com.droiuby.client.core.wrappers;

import org.jruby.RubyProc;
import org.jruby.embed.ScriptingContainer;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;

import com.droiuby.client.core.ExecutionBundle;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

public class ViewWrapper extends View {

	RubyProc block;
	ExecutionBundle bundle;
	ScriptingContainer container;
	
	public ViewWrapper(RubyProc block, ExecutionBundle bundle) {
		super(bundle.getCurrentActivity());
		// TODO Auto-generated constructor stub
		this.block = block;
		this.bundle = bundle;
		this.container= bundle.getContainer();
	}

	/* (non-Javadoc)
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		IRubyObject wrapped_canvas = JavaUtil.convertJavaToRuby(container
				.getProvider().getRuntime(), canvas);
		IRubyObject args[] = new IRubyObject[] { wrapped_canvas };
		IRubyObject return_value = block.call19(container.getProvider()
				.getRuntime().getCurrentContext(), args, null);
	}
	
	

}
