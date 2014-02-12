
package com.droiuby.wrappers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import com.droiuby.client.core.ExecutionBundle;
import org.jruby.Ruby;
import org.jruby.RubyObject;
import org.jruby.embed.ScriptingContainer;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;

public class InvocationHandlerRubyWrapper
    implements InvocationHandler
{

    protected RubyObject backingObject;
    protected ExecutionBundle executionBundle;
    protected ScriptingContainer container;
    protected Ruby runtime;

    public InvocationHandlerRubyWrapper(ExecutionBundle bundle, RubyObject rubyObject) {
        backingObject = rubyObject;
        executionBundle = bundle;
        container = bundle.getContainer();
        runtime = container.getProvider().getRuntime();
    }

    @Override
    public Object invoke(Object param1, Method param2, Object[] param3) {
        try {
            IRubyObject wrapped_param1 = JavaUtil.convertJavaToRuby(runtime, param1);
            IRubyObject wrapped_param2 = JavaUtil.convertJavaToRuby(runtime, param2);
            IRubyObject wrapped_param3 = JavaUtil.convertJavaToRuby(runtime, param3);
            IRubyObject[] args = new IRubyObject[] {wrapped_param1, wrapped_param2, wrapped_param3 };
            IRubyObject result = backingObject.callMethod(runtime.getCurrentContext(), "invoke", args);
            return ((Object) result.toJava(Object.class));
        } catch (RaiseException e) {
            e.printStackTrace();
            executionBundle.addError(e.getMessage());
        }
        return null;
    }

}
