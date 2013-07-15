
package com.droiuby.wrappers;

import android.animation.Animator;
import com.droiuby.client.core.ExecutionBundle;
import org.jruby.Ruby;
import org.jruby.RubyObject;
import org.jruby.embed.ScriptingContainer;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;

public class AnimatorListenerRubyWrapper
    implements Animator.AnimatorListener
{

    protected RubyObject backingObject;
    protected ExecutionBundle executionBundle;
    protected ScriptingContainer container;

    public AnimatorListenerRubyWrapper(ExecutionBundle bundle, RubyObject rubyObject) {
        backingObject = rubyObject;
        executionBundle = bundle;
        container = bundle.getContainer();
    }

    public void onAnimationStart(Animator param1) {
        try {
            Ruby runtime = container.getProvider().getRuntime();
            IRubyObject wrapped_param1 = JavaUtil.convertJavaToRuby(runtime, param1);
            IRubyObject[] args = new IRubyObject[] {wrapped_param1 };
            backingObject.callMethod(runtime.getCurrentContext(), "onAnimationStart", args);
        } catch (RaiseException e) {
            e.printStackTrace();
            executionBundle.addError(e.getMessage());
        }
    }

    public void onAnimationEnd(Animator param1) {
        try {
            Ruby runtime = container.getProvider().getRuntime();
            IRubyObject wrapped_param1 = JavaUtil.convertJavaToRuby(runtime, param1);
            IRubyObject[] args = new IRubyObject[] {wrapped_param1 };
            backingObject.callMethod(runtime.getCurrentContext(), "onAnimationEnd", args);
        } catch (RaiseException e) {
            e.printStackTrace();
            executionBundle.addError(e.getMessage());
        }
    }

    public void onAnimationCancel(Animator param1) {
        try {
            Ruby runtime = container.getProvider().getRuntime();
            IRubyObject wrapped_param1 = JavaUtil.convertJavaToRuby(runtime, param1);
            IRubyObject[] args = new IRubyObject[] {wrapped_param1 };
            backingObject.callMethod(runtime.getCurrentContext(), "onAnimationCancel", args);
        } catch (RaiseException e) {
            e.printStackTrace();
            executionBundle.addError(e.getMessage());
        }
    }

    public void onAnimationRepeat(Animator param1) {
        try {
            Ruby runtime = container.getProvider().getRuntime();
            IRubyObject wrapped_param1 = JavaUtil.convertJavaToRuby(runtime, param1);
            IRubyObject[] args = new IRubyObject[] {wrapped_param1 };
            backingObject.callMethod(runtime.getCurrentContext(), "onAnimationRepeat", args);
        } catch (RaiseException e) {
            e.printStackTrace();
            executionBundle.addError(e.getMessage());
        }
    }

}
