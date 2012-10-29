package com.droiuby.client.core;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.droiuby.client.core.builder.ViewBuilder;

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
	
	public void apply(ActivityBuilder activityBuilder, Context context) {
		for(CssRule rule: rules) {
			Object result = activityBuilder.findViewByName(rule.getSelector());
			Log.d(this.getClass().toString(),"Apply CSS " + rule.getSelector());
			if (result instanceof View) {
				setRuleToView(activityBuilder, context, rule, result);
			} else
			if (result instanceof ArrayList) {
				Log.d(this.getClass().toString(),"Setting multiple view instances ...");
				for(View view : (ArrayList<View>)result) {
					setRuleToView(activityBuilder, context, rule, view);
				}
			}
		}
	}

	private void setRuleToView(ActivityBuilder activityBuilder,
			Context context, CssRule rule, Object result) {
		ViewBuilder viewBuilder = ViewBuilder.getBuilderForView((View)result, context, activityBuilder);
		HashMap <String,String> propertyMap = new HashMap <String,String>();
		for(PropertyValue property : rule.getProperties()) {
			propertyMap.put(property.getProperty(), property.getValue());
		}
		viewBuilder.setParamsFromProperty((View)result, propertyMap);
	}
}
