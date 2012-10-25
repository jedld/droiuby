package com.droiuby.client.core.postprocessor;

import java.util.List;

import com.droiuby.client.core.AssetDownloadCompleteListener;
import com.droiuby.client.core.CssRules;
import com.droiuby.client.core.ExecutionBundle;
import com.osbcp.cssparser.CSSParser;
import com.osbcp.cssparser.PropertyValue;
import com.osbcp.cssparser.Rule;
import com.osbcp.cssparser.Selector;

public class CssPreloadParser implements AssetDownloadCompleteListener {

	public Object onComplete(ExecutionBundle bundle, String name, Object result) {
		CssRules parsed_rules = new CssRules();
		List<Rule> rules = null;
		try {
			rules = CSSParser.parse((String)result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		parsed_rules.setRules(rules);
		
		for (Rule rule : rules) {

		    // Get all the selectors (such as 'table', 'table td', 'a')
		    List<Selector> selectors = rule.getSelectors();

		    // Get all the property (such as 'width') and its value (such as '100px')   
		    List<PropertyValue> propertyValues = rule.getPropertyValues();
		    
		    for(Selector selector : selectors) {
		    	if (selector.toString().startsWith(".")) {
		    		String s = selector.toString().substring(1);
		    		parsed_rules.addClass(s, propertyValues);
		    	}
		    }
		    
		}
		
		return parsed_rules;
	}

}
