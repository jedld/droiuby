package com.droiuby.client.core;

import java.util.List;

import com.osbcp.cssparser.Rule;

public class CssRule {
	List<PropertyValue> properties;
	String selector;
	
	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	public List<PropertyValue> getProperties() {
		return properties;
	}

	public void setProperties(List<PropertyValue> properties) {
		this.properties = properties;
	}

}
