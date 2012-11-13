package com.droiuby.client.core.wrappers;

import org.jruby.RubyProc;
import org.jruby.embed.ScriptingContainer;
import org.jruby.javasupport.JavaObject;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import com.droiuby.client.core.ExecutionBundle;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

public class ListViewAdapterWrapper implements ListAdapter {

	RubyProc getCountBody, getItemBody, getItemIdBody, getItemViewTypeBody,
			getViewBody, getViewTypeCountBody, hasStableIdBody, isEmptyBody,
			registerDataSetObserverBody, unregisterDataSetObserverBody,
			areAllItemsEnabledBody, isEnabledBody;

	ExecutionBundle bundle;
	ScriptingContainer container;

	private ThreadContext ruby_context;

	public ListViewAdapterWrapper(ExecutionBundle bundle) {
		this.bundle = bundle;
		this.container = bundle.getContainer();
		this.ruby_context = container.getProvider().getRuntime().getCurrentContext();
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

	public void setRegisterDataSetObserverBody(
			RubyProc registerDataSetObserverBody) {
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
		try {
			IRubyObject args[] = new IRubyObject[] {};
			if (this.getCountBody != null) {
				IRubyObject result = this.getCountBody.call19(container
						.getProvider().getRuntime().getCurrentContext(), args,
						null);
				return (int) result.convertToInteger().getLongValue();
			}
		} catch (org.jruby.exceptions.RaiseException e) {
			bundle.addError(e.getMessage());
			e.printStackTrace();
		}
		return 0;
	}

	public Object getItem(int element) {
		try {
			IRubyObject args[] = new IRubyObject[] { JavaUtil.convertJavaToRuby(container.getProvider().getRuntime(), element)};
			if (this.getItemBody != null) {
				IRubyObject result = this.getItemBody.call19(container
						.getProvider().getRuntime().getCurrentContext(), args,
						null);
				return result;
			}
		} catch (org.jruby.exceptions.RaiseException e) {
			bundle.addError(e.getMessage());
			e.printStackTrace();
		}
		return 0;
	}

	public long getItemId(int element) {
		try {
			IRubyObject args[] = new IRubyObject[] {JavaUtil.convertJavaToRuby(container.getProvider().getRuntime(), element)};
			if (this.getItemIdBody != null) {
				IRubyObject result = this.getItemIdBody.call19(container
						.getProvider().getRuntime().getCurrentContext(), args,
						null);
				return result.convertToInteger().getLongValue();
			}
		} catch (org.jruby.exceptions.RaiseException e) {
			bundle.addError(e.getMessage());
			e.printStackTrace();
		}
		return 0;
	}

	public int getItemViewType(int element) {
		try {
			IRubyObject args[] = new IRubyObject[] {JavaUtil.convertJavaToRuby(container.getProvider().getRuntime(), element)};
			if (this.getItemViewTypeBody != null) {
				IRubyObject result = this.getItemViewTypeBody.call19(container
						.getProvider().getRuntime().getCurrentContext(), args,
						null);
				return (int)result.convertToInteger().getLongValue();
			}
		} catch (org.jruby.exceptions.RaiseException e) {
			bundle.addError(e.getMessage());
			e.printStackTrace();
		}
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		try {
			IRubyObject args[] = new IRubyObject[] {JavaUtil.convertJavaToRuby(container.getProvider().getRuntime(), position),
					JavaUtil.convertJavaToRuby(container.getProvider().getRuntime(), convertView), JavaUtil.convertJavaToRuby(container.getProvider().getRuntime(), parent)};
			if (this.getViewBody != null) {
				JavaObject result = (JavaObject)this.getViewBody.call19(container
						.getProvider().getRuntime().getCurrentContext(), args,
						null);
				return (View)result.toJava(View.class);
			}
		} catch (org.jruby.exceptions.RaiseException e) {
			bundle.addError(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public int getViewTypeCount() {
		try {
			IRubyObject args[] = new IRubyObject[] {};
			if (this.getViewTypeCountBody != null) {
				IRubyObject result = this.getViewTypeCountBody.call19(container
						.getProvider().getRuntime().getCurrentContext(), args,
						null);
				return (int)result.convertToInteger().getLongValue();
			}
		} catch (org.jruby.exceptions.RaiseException e) {
			bundle.addError(e.getMessage());
			e.printStackTrace();
		}
		return 0;
	}

	public boolean hasStableIds() {
		try {
			IRubyObject args[] = new IRubyObject[] {};
			if (this.hasStableIdBody != null) {
				IRubyObject result = this.hasStableIdBody.call19(container
						.getProvider().getRuntime().getCurrentContext(), args,
						null);
				return JavaUtil.convertRubyToJavaBoolean(result);
			}
		} catch (org.jruby.exceptions.RaiseException e) {
			bundle.addError(e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	public boolean isEmpty() {
		try {
			IRubyObject args[] = new IRubyObject[] {};
			if (this.isEmptyBody != null) {
				IRubyObject result = this.isEmptyBody.call19(container
						.getProvider().getRuntime().getCurrentContext(), args,
						null);
				return JavaUtil.convertRubyToJavaBoolean(result);
			}
		} catch (org.jruby.exceptions.RaiseException e) {
			bundle.addError(e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	public void registerDataSetObserver(DataSetObserver observer) {
		try {
			IRubyObject args[] = new IRubyObject[] {JavaUtil.convertJavaToRuby(container.getProvider().getRuntime(), observer)};
			if (this.registerDataSetObserverBody != null) {
				this.registerDataSetObserverBody.call19(container
						.getProvider().getRuntime().getCurrentContext(), args,
						null);
			}
		} catch (org.jruby.exceptions.RaiseException e) {
			bundle.addError(e.getMessage());
			e.printStackTrace();
		}
	}

	public void unregisterDataSetObserver(DataSetObserver observer) {
		try {
			IRubyObject args[] = new IRubyObject[] {JavaUtil.convertJavaToRuby(container.getProvider().getRuntime(), observer)};
			if (this.unregisterDataSetObserverBody!= null) {
				this.unregisterDataSetObserverBody.call19(container
						.getProvider().getRuntime().getCurrentContext(), args,
						null);
			}
		} catch (org.jruby.exceptions.RaiseException e) {
			bundle.addError(e.getMessage());
			e.printStackTrace();
		}

	}

	public boolean areAllItemsEnabled() {
		try {
			IRubyObject args[] = new IRubyObject[] {};
			if (this.areAllItemsEnabledBody != null) {
				IRubyObject result = this.areAllItemsEnabledBody.call19(container
						.getProvider().getRuntime().getCurrentContext(), args,
						null);
				return JavaUtil.convertRubyToJavaBoolean(result);
			}
		} catch (org.jruby.exceptions.RaiseException e) {
			bundle.addError(e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	public boolean isEnabled(int position) {
		try {
			IRubyObject args[] = new IRubyObject[] {JavaUtil.convertJavaToRuby(container.getProvider().getRuntime(), position)};
			if (this.isEnabledBody != null) {
				IRubyObject result = this.isEnabledBody.call19(container
						.getProvider().getRuntime().getCurrentContext(), args,
						null);
				return JavaUtil.convertRubyToJavaBoolean(result);
			}
		} catch (org.jruby.exceptions.RaiseException e) {
			bundle.addError(e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

}
