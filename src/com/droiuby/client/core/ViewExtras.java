package com.droiuby.client.core;

import java.util.HashMap;

public class ViewExtras {

	String view_name;
	String view_class;
	String view_id;
	HashMap <String, String> dataAttributes = new HashMap<String,String>();
	Object wrapper;

	public HashMap<String, String> getDataAttributes() {
		return dataAttributes;
	}

	public void setDataAttributes(HashMap<String, String> dataAttributes) {
		this.dataAttributes = dataAttributes;
	}

	public String getView_name() {
		return view_name;
	}

	public void setView_name(String view_name) {
		this.view_name = view_name;
	}

	public String getView_class() {
		return view_class;
	}

	public void setView_class(String view_class) {
		this.view_class = view_class;
	}

	public String getView_id() {
		return view_id;
	}

	public void setView_id(String view_id) {
		this.view_id = view_id;
	}

	public Object getWrapper() {
		return wrapper;
	}

	public void setWrapper(Object wrapper) {
		this.wrapper = wrapper;
	}
}
