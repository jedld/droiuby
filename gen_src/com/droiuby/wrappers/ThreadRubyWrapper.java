
package com.droiuby.wrappers;

import com.droiuby.client.core.ExecutionBundle;
import org.jruby.Ruby;
import org.jruby.RubyObject;
import org.jruby.embed.ScriptingContainer;
import org.jruby.exceptions.RaiseException;
import org.jruby.runtime.builtin.IRubyObject;

public class ThreadRubyWrapper
    extends Thread
{

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
    }

    public ThreadRubyWrapper(ExecutionBundle bundle, RubyObject rubyObject, Runnable param1) {
        super(param1);
        backingObject = rubyObject;
        executionBundle = bundle;
        container = bundle.getContainer();
        runtime = container.getProvider().getRuntime();
    }

    public ThreadRubyWrapper(ExecutionBundle bundle, RubyObject rubyObject, ThreadGroup param1, Runnable param2) {
        super(param1, param2);
        backingObject = rubyObject;
        executionBundle = bundle;
        container = bundle.getContainer();
        runtime = container.getProvider().getRuntime();
    }

    public ThreadRubyWrapper(ExecutionBundle bundle, RubyObject rubyObject, String param1) {
        super(param1);
        backingObject = rubyObject;
        executionBundle = bundle;
        container = bundle.getContainer();
        runtime = container.getProvider().getRuntime();
    }

    public ThreadRubyWrapper(ExecutionBundle bundle, RubyObject rubyObject, ThreadGroup param1, String param2) {
        super(param1, param2);
        backingObject = rubyObject;
        executionBundle = bundle;
        container = bundle.getContainer();
        runtime = container.getProvider().getRuntime();
    }

    public ThreadRubyWrapper(ExecutionBundle bundle, RubyObject rubyObject, Runnable param1, String param2) {
        super(param1, param2);
        backingObject = rubyObject;
        executionBundle = bundle;
        container = bundle.getContainer();
        runtime = container.getProvider().getRuntime();
    }

    public ThreadRubyWrapper(ExecutionBundle bundle, RubyObject rubyObject, ThreadGroup param1, Runnable param2, String param3) {
        super(param1, param2, param3);
        backingObject = rubyObject;
        executionBundle = bundle;
        container = bundle.getContainer();
        runtime = container.getProvider().getRuntime();
    }

    public ThreadRubyWrapper(ExecutionBundle bundle, RubyObject rubyObject, ThreadGroup param1, Runnable param2, String param3, long param4) {
        super(param1, param2, param3, param4);
        backingObject = rubyObject;
        executionBundle = bundle;
        container = bundle.getContainer();
        runtime = container.getProvider().getRuntime();
    }

    @Override
    public void run() {
        try {
            IRubyObject[] args = new IRubyObject[] { };
            backingObject.callMethod(runtime.getCurrentContext(), "run", args);
        } catch (RaiseException e) {
            e.printStackTrace();
            executionBundle.addError(e.getMessage());
        }
    }

}
