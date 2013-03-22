package com.droiuby.client.core.scripting;

import java.io.StringWriter;

import org.jruby.runtime.builtin.IRubyObject;

import com.droiuby.client.core.RubyContainerPayload;
import com.droiuby.client.core.listeners.ScriptRuntime;


public interface ScriptingEngine {

	public EmbedEvalUnit parse(String statement, int i);

	public ScriptObject runScriptlet(String string);

	public ThreadContext getCurrentContext();

	public IRubyObject convertJavaToScriptObject(Object loadAppAsset);

	public void put(String string, Object currentController);

	public void setWriter(StringWriter writer);

	public ScriptRuntime getRuntime();

  
}
