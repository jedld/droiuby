package com.droiuby.client.core.wrappers;

import org.jruby.RubyProc;

import com.droiuby.client.core.ExecutionBundle;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

public class ListViewAdapterWrapper implements ListAdapter {

	RubyProc getCountBody, getItemBody, getItemIdBody, getItemViewTypeBody, 
		getViewBody, getViewTypeCountBody, hasStableIdBody, isEmptyBody, registerDataSetObserverBody,
		unregisterDataSetObserverBody, areAllItemsEnabledBody, isEnabledBody;
	
	ExecutionBundle bundle;
	
	public ListViewAdapterWrapper(ExecutionBundle bundle) {
		this.bundle = bundle;
	}
	public RubyProc getGetCountBody() {
		return getCountBody;
	}

	public void setGetCountBody(RubyProc getCountBody) {
		this.getCountBody = getCountBody;
	}

	public RubyProc getGetItemBody() {
		return getItemBody;
	}

	public void setGetItemBody(RubyProc getItemBody) {
		this.getItemBody = getItemBody;
	}

	public RubyProc getGetItemIdBody() {
		return getItemIdBody;
	}

	public void setGetItemIdBody(RubyProc getItemIdBody) {
		this.getItemIdBody = getItemIdBody;
	}

	public RubyProc getGetItemViewTypeBody() {
		return getItemViewTypeBody;
	}

	public void setGetItemViewTypeBody(RubyProc getItemViewTypeBody) {
		this.getItemViewTypeBody = getItemViewTypeBody;
	}

	public RubyProc getGetViewBody() {
		return getViewBody;
	}

	public void setGetViewBody(RubyProc getViewBody) {
		this.getViewBody = getViewBody;
	}

	public RubyProc getGetViewTypeCountBody() {
		return getViewTypeCountBody;
	}

	public void setGetViewTypeCountBody(RubyProc getViewTypeCountBody) {
		this.getViewTypeCountBody = getViewTypeCountBody;
	}

	public RubyProc getHasStableIdBody() {
		return hasStableIdBody;
	}

	public void setHasStableIdBody(RubyProc hasStableIdBody) {
		this.hasStableIdBody = hasStableIdBody;
	}

	public RubyProc getIsEmptyBody() {
		return isEmptyBody;
	}

	public void setIsEmptyBody(RubyProc isEmptyBody) {
		this.isEmptyBody = isEmptyBody;
	}

	public RubyProc getRegisterDataSetObserverBody() {
		return registerDataSetObserverBody;
	}

	public void setRegisterDataSetObserverBody(RubyProc registerDataSetObserverBody) {
		this.registerDataSetObserverBody = registerDataSetObserverBody;
	}

	public RubyProc getUnregisterDataSetObserverBody() {
		return unregisterDataSetObserverBody;
	}

	public void setUnregisterDataSetObserverBody(
			RubyProc unregisterDataSetObserverBody) {
		this.unregisterDataSetObserverBody = unregisterDataSetObserverBody;
	}

	public RubyProc getAreAllItemsEnabledBody() {
		return areAllItemsEnabledBody;
	}

	public void setAreAllItemsEnabledBody(RubyProc areAllItemsEnabledBody) {
		this.areAllItemsEnabledBody = areAllItemsEnabledBody;
	}

	public RubyProc getIsEnabledBody() {
		return isEnabledBody;
	}

	public void setIsEnabledBody(RubyProc isEnabledBody) {
		this.isEnabledBody = isEnabledBody;
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getItemViewType(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		
	}

	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		
	}

	public boolean areAllItemsEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEnabled(int position) {
		// TODO Auto-generated method stub
		return false;
	}

}
