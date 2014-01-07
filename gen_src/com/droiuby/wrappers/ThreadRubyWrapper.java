
package com.droiuby.wrappers;

import java.util.HashSet;
import com.droiuby.client.core.ExecutionBundle;
import com.droiuby.client.core.utils.Utils;
import org.jruby.Ruby;
import org.jruby.RubyInteger;
import org.jruby.RubyObject;
import org.jruby.embed.ScriptingContainer;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;

public class ThreadRubyWrapper
    extends Thread
{

    protected HashSet methodCache;
    protected RubyObject backingObject;
    protected ExecutionBundle executionBundle;
    protected ScriptingContainer container;
    protected Ruby runtime;

    public ThreadRubyWrapper(ExecutionBundle bundle, RubyObject rubyObject) {
        super();
        backingObject = rubyObject;
        executionBundle = bundle;
        container = bundle.getContainer();
        runtime = container.getProvider().getRuntime();
        methodCache = Utils.toStringSet(backingObject.callMethod("methods", new IRubyObject[] { }));
        if (methodCache.contains("on_initialize")) {
            backingObject.callMethod(runtime.getCurrentContext(), "on_initialize", new IRubyObject[] { });
        }
    }

    public ThreadRubyWrapper(ExecutionBundle bundle, RubyObject rubyObject, Runnable param1) {
        super(param1);
        backingObject = rubyObject;
        executionBundle = bundle;
        container = bundle.getContainer();
        runtime = container.getProvider().getRuntime();
        methodCache = Utils.toStringSet(backingObject.callMethod("methods", new IRubyObject[] { }));
        if (methodCache.contains("on_initialize")) {
            IRubyObject wrapped_param1 = JavaUtil.convertJavaToRuby(runtime, param1);
            backingObject.callMethod(runtime.getCurrentContext(), "on_initialize", new IRubyObject[] {wrapped_param1 });
        }
    }

    public ThreadRubyWrapper(ExecutionBundle bundle, RubyObject rubyObject, ThreadGroup param1, Runnable param2) {
        super(param1, param2);
        backingObject = rubyObject;
        executionBundle = bundle;
        container = bundle.getContainer();
        runtime = container.getProvider().getRuntime();
        methodCache = Utils.toStringSet(backingObject.callMethod("methods", new IRubyObject[] { }));
        if (methodCache.contains("on_initialize")) {
            IRubyObject wrapped_param1 = JavaUtil.convertJavaToRuby(runtime, param1);
            IRubyObject wrapped_param2 = JavaUtil.convertJavaToRuby(runtime, param2);
            backingObject.callMethod(runtime.getCurrentContext(), "on_initialize", new IRubyObject[] {wrapped_param1, wrapped_param2 });
        }
    }

    public ThreadRubyWrapper(ExecutionBundle bundle, RubyObject rubyObject, String param1) {
        super(param1);
        backingObject = rubyObject;
        executionBundle = bundle;
        container = bundle.getContainer();
        runtime = container.getProvider().getRuntime();
        methodCache = Utils.toStringSet(backingObject.callMethod("methods", new IRubyObject[] { }));
        if (methodCache.contains("on_initialize")) {
            IRubyObject wrapped_param1 = JavaUtil.convertJavaToRuby(runtime, param1);
            backingObject.callMethod(runtime.getCurrentContext(), "on_initialize", new IRubyObject[] {wrapped_param1 });
        }
    }

    public ThreadRubyWrapper(ExecutionBundle bundle, RubyObject rubyObject, ThreadGroup param1, String param2) {
        super(param1, param2);
        backingObject = rubyObject;
        executionBundle = bundle;
        container = bundle.getContainer();
        runtime = container.getProvider().getRuntime();
        methodCache = Utils.toStringSet(backingObject.callMethod("methods", new IRubyObject[] { }));
        if (methodCache.contains("on_initialize")) {
            IRubyObject wrapped_param1 = JavaUtil.convertJavaToRuby(runtime, param1);
            IRubyObject wrapped_param2 = JavaUtil.convertJavaToRuby(runtime, param2);
            backingObject.callMethod(runtime.getCurrentContext(), "on_initialize", new IRubyObject[] {wrapped_param1, wrapped_param2 });
        }
    }

    public ThreadRubyWrapper(ExecutionBundle bundle, RubyObject rubyObject, Runnable param1, String param2) {
        super(param1, param2);
        backingObject = rubyObject;
        executionBundle = bundle;
        container = bundle.getContainer();
        runtime = container.getProvider().getRuntime();
        methodCache = Utils.toStringSet(backingObject.callMethod("methods", new IRubyObject[] { }));
        if (methodCache.contains("on_initialize")) {
            IRubyObject wrapped_param1 = JavaUtil.convertJavaToRuby(runtime, param1);
            IRubyObject wrapped_param2 = JavaUtil.convertJavaToRuby(runtime, param2);
            backingObject.callMethod(runtime.getCurrentContext(), "on_initialize", new IRubyObject[] {wrapped_param1, wrapped_param2 });
        }
    }

    public ThreadRubyWrapper(ExecutionBundle bundle, RubyObject rubyObject, ThreadGroup param1, Runnable param2, String param3) {
        super(param1, param2, param3);
        backingObject = rubyObject;
        executionBundle = bundle;
        container = bundle.getContainer();
        runtime = container.getProvider().getRuntime();
        methodCache = Utils.toStringSet(backingObject.callMethod("methods", new IRubyObject[] { }));
        if (methodCache.contains("on_initialize")) {
            IRubyObject wrapped_param1 = JavaUtil.convertJavaToRuby(runtime, param1);
            IRubyObject wrapped_param2 = JavaUtil.convertJavaToRuby(runtime, param2);
            IRubyObject wrapped_param3 = JavaUtil.convertJavaToRuby(runtime, param3);
            backingObject.callMethod(runtime.getCurrentContext(), "on_initialize", new IRubyObject[] {wrapped_param1, wrapped_param2, wrapped_param3 });
        }
    }

    public ThreadRubyWrapper(ExecutionBundle bundle, RubyObject rubyObject, ThreadGroup param1, Runnable param2, String param3, long param4) {
        super(param1, param2, param3, param4);
        backingObject = rubyObject;
        executionBundle = bundle;
        container = bundle.getContainer();
        runtime = container.getProvider().getRuntime();
        methodCache = Utils.toStringSet(backingObject.callMethod("methods", new IRubyObject[] { }));
        if (methodCache.contains("on_initialize")) {
            IRubyObject wrapped_param1 = JavaUtil.convertJavaToRuby(runtime, param1);
            IRubyObject wrapped_param2 = JavaUtil.convertJavaToRuby(runtime, param2);
            IRubyObject wrapped_param3 = JavaUtil.convertJavaToRuby(runtime, param3);
            IRubyObject wrapped_param4 = RubyInteger.int2fix(runtime, param4);
            backingObject.callMethod(runtime.getCurrentContext(), "on_initialize", new IRubyObject[] {wrapped_param1, wrapped_param2, wrapped_param3, wrapped_param4 });
        }
    }

    @Override
    public void run() {
        try {
            if (methodCache.contains("run")) {
                IRubyObject[] args = new IRubyObject[] { };
                backingObject.callMethod(runtime.getCurrentContext(), "run", args);
            } else {
                super.run();
            }
        } catch (RaiseException e) {
            e.printStackTrace();
            executionBundle.addError(e.getMessage());
        }
    }

}
