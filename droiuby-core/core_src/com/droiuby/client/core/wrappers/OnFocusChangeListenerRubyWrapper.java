
package com.droiuby.client.core.wrappers;

import android.view.View;
import com.droiuby.client.core.ExecutionBundle;
import org.jruby.Ruby;
import org.jruby.RubyBoolean;
import org.jruby.RubyObject;
import org.jruby.embed.ScriptingContainer;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;

public class OnFocusChangeListenerRubyWrapper
    implements View.OnFocusChangeListener
{

    protected RubyObject backingObject;
    protected ExecutionBundle executionBundle;
    protected ScriptingContainer container;

    public OnFocusChangeListenerRubyWrapper(ExecutionBundle bundle, RubyObject rubyObject) {
        backingObject = rubyObject;
        executionBundle = bundle;
        container = bundle.getContainer();
    }

    public void onFocusChange(View param1, boolean param2) {
        try {
            Ruby runtime = container.getProvider().getRuntime();
            IRubyObject wrapped_param1 = JavaUtil.convertJavaToRuby(runtime, param1);
            IRubyObject wrapped_param2 = RubyBoolean.newBoolean(runtime, param2);
            IRubyObject[] args = new IRubyObject[] {wrapped_param1, wrapped_param2 };
            backingObject.callMethod(runtime.getCurrentContext(), "onFocusChange", args);
        } catch (RaiseException e) {
            e.printStackTrace();
            executionBundle.addError(e.getMessage());
        }
    }

}
