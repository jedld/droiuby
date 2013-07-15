
package com.droiuby.wrappers;

import android.view.View;
import com.droiuby.client.core.ExecutionBundle;
import org.jruby.Ruby;
import org.jruby.RubyObject;
import org.jruby.embed.ScriptingContainer;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;

public class OnClickListenerRubyWrapper
    implements View.OnClickListener
{

    protected RubyObject backingObject;
    protected ExecutionBundle executionBundle;
    protected ScriptingContainer container;

    public OnClickListenerRubyWrapper(ExecutionBundle bundle, RubyObject rubyObject) {
        backingObject = rubyObject;
        executionBundle = bundle;
        container = bundle.getContainer();
    }

    public void onClick(View param1) {
        try {
            Ruby runtime = container.getProvider().getRuntime();
            IRubyObject wrapped_param1 = JavaUtil.convertJavaToRuby(runtime, param1);
            IRubyObject[] args = new IRubyObject[] {wrapped_param1 };
            backingObject.callMethod(runtime.getCurrentContext(), "onClick", args);
        } catch (RaiseException e) {
            e.printStackTrace();
            executionBundle.addError(e.getMessage());
        }
    }

}
