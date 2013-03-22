package com.droiuby.client.core.scripting;

public interface ScriptObject {

	ScriptObject callMethod(ThreadContext context, String string);

}
