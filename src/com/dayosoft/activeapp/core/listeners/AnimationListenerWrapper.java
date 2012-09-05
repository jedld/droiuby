package com.dayosoft.activeapp.core.listeners;

import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;

public class AnimationListenerWrapper extends ListenerWrapper implements AnimatorListener {

	public AnimationListenerWrapper(ScriptingContainer container,
			IRubyObject block) {
		super(container, block);
		// TODO Auto-generated constructor stub
	}

	public void onAnimationCancel(Animator animation) {
		// TODO Auto-generated method stub

	}

	public void onAnimationEnd(Animator animation) {
		// TODO Auto-generated method stub

	}

	public void onAnimationRepeat(Animator animation) {
		// TODO Auto-generated method stub

	}

	public void onAnimationStart(Animator animation) {
		// TODO Auto-generated method stub

	}

}
