package com.dayosoft.activeapp.core;

import org.jruby.embed.ScriptingContainer;

public class ExecutionBundle {
	ScriptingContainer container;
	public ScriptingContainer getContainer() {
		return container;
	}
	public void setContainer(ScriptingContainer container) {
		this.container = container;
	}
	public RubyContainerPayload getPayload() {
		return payload;
	}
	public void setPayload(RubyContainerPayload payload) {
		this.payload = payload;
	}
	RubyContainerPayload payload;
}
