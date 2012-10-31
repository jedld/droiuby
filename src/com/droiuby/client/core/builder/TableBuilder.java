package com.droiuby.client.core.builder;

import android.content.Context;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.droiuby.client.core.ActivityBuilder;

public class TableBuilder extends ViewGroupBuilder {
	
	public View getView() {
		return new TableLayout(context);
	}

}
