package com.droiuby.client.core;

import java.util.HashMap;
import java.util.List;

import com.osbcp.cssparser.PropertyValue;
import com.osbcp.cssparser.Rule;

public class CssRules {
	List<Rule> rules;
	HashMap <String, List<PropertyValue>> classStyleLookup = new HashMap <String, List<PropertyValue>>();

	public HashMap<String, List<PropertyValue>> getClassStyleLookup() {
		return classStyleLookup;
	}

	public void setClassStyleLookup(
			HashMap<String, List<PropertyValue>> classStyleLookup) {
		this.classStyleLookup = classStyleLookup;
	}

	public void addClass(String cssClass, List<PropertyValue> property) {
		List<PropertyValue> current_property = classStyleLookup.get(cssClass);
		if (current_property!=null) {
			current_property.addAll(property);
		} else {
			classStyleLookup.put(cssClass, property);
		}
	}
	
	public List<Rule> getRules() {
		return rules;
	}

	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}
}
