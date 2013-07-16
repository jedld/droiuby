
package com.droiuby.client.core.wrappers;

import android.view.MotionEvent;
import android.view.View;
import com.droiuby.client.core.ExecutionBundle;
import com.droiuby.client.core.utils.Utils;
import org.jruby.Ruby;
import org.jruby.RubyObject;
import org.jruby.embed.ScriptingContainer;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;

public class OnTouchListenerRubyWrapper
    implements View.OnTouchListener
{

    protected RubyObject backingObject;
    protected ExecutionBundle executionBundle;
    protected ScriptingContainer container;

    public OnTouchListenerRubyWrapper(ExecutionBundle bundle, RubyObject rubyObject) {
        backingObject = rubyObject;
        executionBundle = bundle;
        container = bundle.getContainer();
    }

    public boolean onTouch(View param1, MotionEvent param2) {
        try {
            Ruby runtime = container.getProvider().getRuntime();
            IRubyObject wrapped_param1 = JavaUtil.convertJavaToRuby(runtime, param1);
            IRubyObject wrapped_param2 = JavaUtil.convertJavaToRuby(runtime, param2);
            IRubyObject[] args = new IRubyObject[] {wrapped_param1, wrapped_param2 };
            boolean result = Utils.toBoolean(backingObject.callMethod(runtime.getCurrentContext(), "onTouch", args));
            return result;
        } catch (RaiseException e) {
            e.printStackTrace();
            executionBundle.addError(e.getMessage());
        }
        return false;
    }

}
