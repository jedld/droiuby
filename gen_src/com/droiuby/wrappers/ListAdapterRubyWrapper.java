
package com.droiuby.wrappers;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import com.droiuby.client.core.ExecutionBundle;
import com.droiuby.client.core.utils.Utils;
import org.jruby.Ruby;
import org.jruby.RubyInteger;
import org.jruby.RubyObject;
import org.jruby.embed.ScriptingContainer;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;

public class ListAdapterRubyWrapper
    implements ListAdapter
{

    protected RubyObject backingObject;
    protected ExecutionBundle executionBundle;
    protected ScriptingContainer container;
    protected Ruby runtime;

    public ListAdapterRubyWrapper(ExecutionBundle bundle, RubyObject rubyObject) {
        backingObject = rubyObject;
        executionBundle = bundle;
        container = bundle.getContainer();
        runtime = container.getProvider().getRuntime();
    }

    @Override
    public boolean areAllItemsEnabled() {
        try {
            IRubyObject[] args = new IRubyObject[] { };
            boolean result = Utils.toBoolean(backingObject.callMethod(runtime.getCurrentContext(), "areAllItemsEnabled", args));
            return result;
        } catch (RaiseException e) {
            e.printStackTrace();
            executionBundle.addError(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean isEnabled(int param1) {
        try {
            IRubyObject wrapped_param1 = RubyInteger.int2fix(runtime, param1);
            IRubyObject[] args = new IRubyObject[] {wrapped_param1 };
            boolean result = Utils.toBoolean(backingObject.callMethod(runtime.getCurrentContext(), "isEnabled", args));
            return result;
        } catch (RaiseException e) {
            e.printStackTrace();
            executionBundle.addError(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        try {
            IRubyObject[] args = new IRubyObject[] { };
            boolean result = Utils.toBoolean(backingObject.callMethod(runtime.getCurrentContext(), "isEmpty", args));
            return result;
        } catch (RaiseException e) {
            e.printStackTrace();
            executionBundle.addError(e.getMessage());
        }
        return false;
    }

    @Override
    public int getCount() {
        try {
            IRubyObject[] args = new IRubyObject[] { };
            int result = Utils.toInteger(backingObject.callMethod(runtime.getCurrentContext(), "getCount", args));
            return result;
        } catch (RaiseException e) {
            e.printStackTrace();
            executionBundle.addError(e.getMessage());
        }
        return  0;
    }

    @Override
    public Object getItem(int param1) {
        try {
            IRubyObject wrapped_param1 = RubyInteger.int2fix(runtime, param1);
            IRubyObject[] args = new IRubyObject[] {wrapped_param1 };
            IRubyObject result = backingObject.callMethod(runtime.getCurrentContext(), "getItem", args);
            return ((Object) result.toJava(Object.class));
        } catch (RaiseException e) {
            e.printStackTrace();
            executionBundle.addError(e.getMessage());
        }
        return null;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver param1) {
        try {
            IRubyObject wrapped_param1 = JavaUtil.convertJavaToRuby(runtime, param1);
            IRubyObject[] args = new IRubyObject[] {wrapped_param1 };
            backingObject.callMethod(runtime.getCurrentContext(), "registerDataSetObserver", args);
        } catch (RaiseException e) {
            e.printStackTrace();
            executionBundle.addError(e.getMessage());
        }
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver param1) {
        try {
            IRubyObject wrapped_param1 = JavaUtil.convertJavaToRuby(runtime, param1);
            IRubyObject[] args = new IRubyObject[] {wrapped_param1 };
            backingObject.callMethod(runtime.getCurrentContext(), "unregisterDataSetObserver", args);
        } catch (RaiseException e) {
            e.printStackTrace();
            executionBundle.addError(e.getMessage());
        }
    }

    @Override
    public long getItemId(int param1) {
        try {
            IRubyObject wrapped_param1 = RubyInteger.int2fix(runtime, param1);
            IRubyObject[] args = new IRubyObject[] {wrapped_param1 };
            long result = Utils.toLong(backingObject.callMethod(runtime.getCurrentContext(), "getItemId", args));
            return result;
        } catch (RaiseException e) {
            e.printStackTrace();
            executionBundle.addError(e.getMessage());
        }
        return  0;
    }

    @Override
    public boolean hasStableIds() {
        try {
            IRubyObject[] args = new IRubyObject[] { };
            boolean result = Utils.toBoolean(backingObject.callMethod(runtime.getCurrentContext(), "hasStableIds", args));
            return result;
        } catch (RaiseException e) {
            e.printStackTrace();
            executionBundle.addError(e.getMessage());
        }
        return false;
    }

    @Override
    public View getView(int param1, View param2, ViewGroup param3) {
        try {
            IRubyObject wrapped_param1 = RubyInteger.int2fix(runtime, param1);
            IRubyObject wrapped_param2 = JavaUtil.convertJavaToRuby(runtime, param2);
            IRubyObject wrapped_param3 = JavaUtil.convertJavaToRuby(runtime, param3);
            IRubyObject[] args = new IRubyObject[] {wrapped_param1, wrapped_param2, wrapped_param3 };
            IRubyObject result = backingObject.callMethod(runtime.getCurrentContext(), "getView", args);
            return ((View) result.toJava(View.class));
        } catch (RaiseException e) {
            e.printStackTrace();
            executionBundle.addError(e.getMessage());
        }
        return null;
    }

    @Override
    public int getItemViewType(int param1) {
        try {
            IRubyObject wrapped_param1 = RubyInteger.int2fix(runtime, param1);
            IRubyObject[] args = new IRubyObject[] {wrapped_param1 };
            int result = Utils.toInteger(backingObject.callMethod(runtime.getCurrentContext(), "getItemViewType", args));
            return result;
        } catch (RaiseException e) {
            e.printStackTrace();
            executionBundle.addError(e.getMessage());
        }
        return  0;
    }

    @Override
    public int getViewTypeCount() {
        try {
            IRubyObject[] args = new IRubyObject[] { };
            int result = Utils.toInteger(backingObject.callMethod(runtime.getCurrentContext(), "getViewTypeCount", args));
            return result;
        } catch (RaiseException e) {
            e.printStackTrace();
            executionBundle.addError(e.getMessage());
        }
        return  0;
    }

}
