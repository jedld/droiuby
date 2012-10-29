package com.droiuby.client.core.postprocessor;

import java.util.List;

import android.util.Log;

import com.droiuby.client.core.AssetDownloadCompleteListener;
import com.droiuby.client.core.CssRule;
import com.droiuby.client.core.CssRules;
import com.droiuby.client.core.ExecutionBundle;
import com.droiuby.client.core.PropertyValue;
import com.osbcp.cssparser.CSSParser;
import com.osbcp.cssparser.Rule;
import com.osbcp.cssparser.Selector;

public class CssPreloadParser implements AssetDownloadCompleteListener {

	public Object onComplete(ExecutionBundle bundle, String name, Object result) {
		CssRules parsed_rules = new CssRules();
		List<Rule> rules = null;
		try {
			Log.d(this.getClass().toString(), (String)result);
			rules = CSSParser.parse((String) result);

			for (Rule rule : rules) {
				// Get all the selectors (such as 'table', 'table td', 'a')
				List<Selector> selectors = rule.getSelectors();
				// Get all the property (such as 'width') and its value (such as
				// '100px')
				List<PropertyValue> propertyValues = rule.getPropertyValues();

				for (Selector selector : selectors) {
					CssRule ruleItem = new CssRule();
					Log.d(this.getClass().toString(),"selector = " + selector.toString());
					ruleItem.setProperties(propertyValues);
					ruleItem.setSelector(selector.toString());
					parsed_rules.addRule(ruleItem);
				}

			}
			return parsed_rules;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			bundle.addError(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

}
