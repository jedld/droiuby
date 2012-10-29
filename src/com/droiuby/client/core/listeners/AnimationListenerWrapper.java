package com.droiuby.client.core.listeners;

import org.jruby.RubyProc;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;

import com.droiuby.client.core.ExecutionBundle;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;

public class AnimationListenerWrapper extends ListenerWrapper implements
		AnimatorListener {

	String target;

	public AnimationListenerWrapper(ExecutionBundle bundle,
			String target, RubyProc block) {
		super(bundle, block);
		this.target = target;
	}

	public void onAnimationCancel(Animator animation) {
		if (target.equals("cancel")) {
			this.execute(animation);
		}
	}

	public void onAnimationEnd(Animator animation) {
		if (target.equals("end")) {
			this.execute(animation);
		}
	}

	public void onAnimationRepeat(Animator animation) {
		if (target.equals("repeat")) {
			this.execute(animation);
		}
	}

	public void onAnimationStart(Animator animation) {
		if (target.equals("start")) {
			this.execute(animation);
		}
	}

}
