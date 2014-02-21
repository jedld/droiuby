package com.droiuby.client.core.builder;

import android.view.View;
import android.widget.TableLayout;


public class TableBuilder extends ViewGroupBuilder {
	
	public View getView() {
		return new TableLayout(context);
	}

}
