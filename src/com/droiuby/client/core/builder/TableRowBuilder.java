package com.droiuby.client.core.builder;

import java.util.HashMap;

import org.jdom2.Element;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TableRow;


import com.droiuby.client.core.ActivityBuilder;

public class TableRowBuilder extends ViewGroupBuilder {

	@Override
	public View getView() {
		return new TableRow(context);
	}
	
	@Override
	public LayoutParams setLayoutParams(LayoutParams currentParams, HashMap<String, String> propertyMap) {
		TableRow.LayoutParams tableLayoutParams = new TableRow.LayoutParams(
				super.setLayoutParams(currentParams, propertyMap));
		Log.d(this.getClass().toString(),"Setting Layout Params");
		for (String key : propertyMap.keySet()) {
			String attribute_name = key;
			String attribute_value = propertyMap.get(key);
			if (attribute_name.equals("column")) {
				tableLayoutParams.column = Integer.parseInt(attribute_value);
			} else if (attribute_name.equals("span")) {
				tableLayoutParams.span = Integer.parseInt(attribute_value);
			}
		}
		return tableLayoutParams;
	}

	@Override
	public View setParams(View child, Element e) {
		return super.setParams(child, e);
	}

}
