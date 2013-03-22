package com.droiuby.client.core.scripting;

import org.jruby.runtime.builtin.IRubyObject;


public interface ScriptProc {

	ScriptObject call(ThreadContext currentContext, ScriptObject[] args, Object object);

}
