package com.droiuby.client.core;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.droiuby.client.core.builder.ActivityBuilder;
import com.droiuby.client.core.builder.ViewBuilder;

class Selector {
	public static final int SELECT_ID = 0;
	public static final int SELECT_CLASS = 1;
	public static final int SELECT_TAG = 2;
	public static final int COMPOUND_SELECTOR = 3;

	String identifier;
	int selectorType;

	public int getSelectorType() {
		return selectorType;
	}

	public void setSelectorType(int selectorType) {
		this.selectorType = selectorType;
	}

	ArrayList<Selector> compound = new ArrayList<Selector>();
	Selector child;

	public Selector getChild() {
		return child;
	}

	public void setChild(Selector child) {
		this.child = child;
	}

	public void addSelector(Selector selector) {
		compound.add(selector);
	}

	public ArrayList<Selector> getCompound() {
		return compound;
	}

	public void setCompound(ArrayList<Selector> compound) {
		this.compound = compound;
	}

	static final int STATE_TYPE_INITIAL = 0;
	static final int STATE_TYPE_PARSE_IDENTIFIER = 0;
	static final int SUB_STATE_TYPE_COMPOUND_SELECTOR = 1;

	public static Selector parseSelector(String selector) {
		Selector mainSelector = new Selector();
		Selector current = mainSelector;
		char selectorChars[] = selector.toCharArray();
		int pos = 0;
		int state = STATE_TYPE_INITIAL;
		int sub_state = 0;
		StringBuilder identifierNameBuffer = new StringBuilder();
		do {
			char c = selectorChars[pos];
			if (state == STATE_TYPE_INITIAL) {
				if (c == '#') {
					current.setSelectorType(SELECT_ID);
					state = STATE_TYPE_PARSE_IDENTIFIER;
				} else if (c == '.') {
					current.setSelectorType(SELECT_CLASS);
					state = STATE_TYPE_PARSE_IDENTIFIER;
				} else if (Character.isLetterOrDigit(c)) {
					current.setSelectorType(SELECT_TAG);
					state = STATE_TYPE_PARSE_IDENTIFIER;
				}
			} else
				if (state == STATE_TYPE_PARSE_IDENTIFIER) {
					
					if (Character.isLetterOrDigit(c)) {
						identifierNameBuffer.append(c);
					} else
						if ( (c == '.') || (c == '#') ) {
							Selector subSelector = new Selector();
							
							subSelector.identifier = identifierNameBuffer.toString();
							
							identifierNameBuffer = new StringBuilder();
							
							if (sub_state!=SUB_STATE_TYPE_COMPOUND_SELECTOR) {
								subSelector.setSelectorType(current.getSelectorType());
								current.setSelectorType(COMPOUND_SELECTOR);
							} else {
								current.setSelectorType(SELECT_CLASS);
							}
							current.addSelector(subSelector);
							sub_state = SUB_STATE_TYPE_COMPOUND_SELECTOR;
							state = STATE_TYPE_PARSE_IDENTIFIER; 
						}
				}
			pos++;
		} while (pos < selectorChars.length);
		return mainSelector;
	}

}

public class CssRules {
	ArrayList<CssRule> rules = new ArrayList<CssRule>();

	public ArrayList<CssRule> getRules() {
		return rules;
	}

	public void setRules(ArrayList<CssRule> rules) {
		this.rules = rules;
	}

	public void addRule(CssRule rule) {
		rules.add(rule);
	}

	@SuppressWarnings("unchecked")
	public void apply(ActivityBuilder activityBuilder, Context context) {
		for (CssRule rule : rules) {
			
			Log.v(this.getClass().toString(), "apply " + rule.selector);
			Object result = activityBuilder.findViewByName(rule.getSelector());
			if (result instanceof View) {
				Log.v(this.getClass().toString(), "applying to view");
				setRuleToView(activityBuilder, context, rule, result);
				//apply element properties
				activityBuilder.applyProperties((View)result);
			} else if (result instanceof ArrayList<?>) {
				for (View view : ((ArrayList<View>) result)) {
					Log.v(this.getClass().toString(), "applying to view " + view.getId());
					setRuleToView(activityBuilder, context, rule, (View)view);
					activityBuilder.applyProperties((View)view);
				}
			}
		}
	}

	private void setRuleToView(ActivityBuilder activityBuilder,
			Context context, CssRule rule, Object result) {
		ViewBuilder viewBuilder = ViewBuilder.getBuilderForView((View) result,
				context, activityBuilder);
		HashMap<String, String> propertyMap = new HashMap<String, String>();
		for (PropertyValue property : rule.getProperties()) {
			Log.v(this.getClass().toString(), "set " + property.getProperty() + " = " + property.getValue());
			propertyMap.put(property.getProperty(), property.getValue());
		}
		try {
			viewBuilder.setParamsFromProperty((View) result, propertyMap);
		} catch (Exception e) {
			Log.e(this.getClass().toString(), e.getMessage());
			String message =  e.getClass().toString() + " " + e.getMessage();
			activityBuilder.addViewError(message);
		}
	}
}
