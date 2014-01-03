
package com.droiuby.wrappers;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import com.droiuby.client.core.ExecutionBundle;
import org.jruby.Ruby;
import org.jruby.RubyInteger;
import org.jruby.RubyObject;
import org.jruby.embed.ScriptingContainer;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;

public class ViewRubyWrapper
    extends View
{

    protected RubyObject backingObject;
    protected ExecutionBundle executionBundle;
    protected ScriptingContainer container;
    protected Ruby runtime;

    public ViewRubyWrapper(ExecutionBundle bundle, RubyObject rubyObject, Context param1) {
        super(bundle.getCurrentActivity());
        backingObject = rubyObject;
        executionBundle = bundle;
        container = bundle.getContainer();
        runtime = container.getProvider().getRuntime();
    }

    public ViewRubyWrapper(ExecutionBundle bundle, RubyObject rubyObject, Context param1, AttributeSet param2) {
        super(bundle.getCurrentActivity(), param2);
        backingObject = rubyObject;
        executionBundle = bundle;
        container = bundle.getContainer();
        runtime = container.getProvider().getRuntime();
    }

    public ViewRubyWrapper(ExecutionBundle bundle, RubyObject rubyObject, Context param1, AttributeSet param2, int param3) {
        super(bundle.getCurrentActivity(), param2, param3);
        backingObject = rubyObject;
        executionBundle = bundle;
        container = bundle.getContainer();
        runtime = container.getProvider().getRuntime();
    }

    @Override
    protected void onSizeChanged(int param1, int param2, int param3, int param4) {
        try {
            IRubyObject wrapped_param1 = RubyInteger.int2fix(runtime, param1);
            IRubyObject wrapped_param2 = RubyInteger.int2fix(runtime, param2);
            IRubyObject wrapped_param3 = RubyInteger.int2fix(runtime, param3);
            IRubyObject wrapped_param4 = RubyInteger.int2fix(runtime, param4);
            IRubyObject[] args = new IRubyObject[] {wrapped_param1, wrapped_param2, wrapped_param3, wrapped_param4 };
            backingObject.callMethod(runtime.getCurrentContext(), "onSizeChanged", args);
        } catch (RaiseException e) {
            e.printStackTrace();
            executionBundle.addError(e.getMessage());
        }
    }

    @Override
    protected void onDraw(Canvas param1) {
        try {
            IRubyObject wrapped_param1 = JavaUtil.convertJavaToRuby(runtime, param1);
            IRubyObject[] args = new IRubyObject[] {wrapped_param1 };
            backingObject.callMethod(runtime.getCurrentContext(), "onDraw", args);
        } catch (RaiseException e) {
            e.printStackTrace();
            executionBundle.addError(e.getMessage());
        }
    }

}
