package com.droiuby.client.core.wrappers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashSet;

import org.jruby.Ruby;
import org.jruby.RubyBoolean;
import org.jruby.RubyFixnum;
import org.jruby.embed.ScriptingContainer;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;

import android.util.Log;

import com.droiuby.client.core.ExecutionBundle;
import com.droiuby.client.core.utils.Utils;
import com.google.dexmaker.stock.ProxyBuilder;

public class InvocationHandlerRubyWrapper implements InvocationHandler {

	protected IRubyObject backingObject;
	protected ExecutionBundle executionBundle;
	protected ScriptingContainer container;
	boolean direct;
	protected Ruby runtime;
	private HashSet<String> methodCache;

	public InvocationHandlerRubyWrapper(ExecutionBundle bundle, Object object) {
		setProperties(bundle, object);
	}

	public InvocationHandlerRubyWrapper(ExecutionBundle bundle, Object object,
			boolean direct) {
		setProperties(bundle, object);
		this.direct = direct;
	}

	private void setProperties(ExecutionBundle bundle, Object object) {
		executionBundle = bundle;
		container = bundle.getContainer();
		runtime = container.getProvider().getRuntime();
		backingObject = JavaUtil.convertJavaToRuby(runtime, object);
		methodCache = Utils.toStringSet(backingObject.getType().callMethod(
				runtime.getCurrentContext(), "instance_methods",
				new IRubyObject[] { RubyBoolean.createFalseClass(runtime) }));
	}

	public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
		try {
			Log.d(this.getClass().toString(),
					"wrapper method = " + method.getName());
			IRubyObject wrapped_param1 = JavaUtil.convertJavaToRuby(runtime,
					proxy);
			IRubyObject wrapped_param2 = JavaUtil.convertJavaToRuby(runtime,
					method);
			IRubyObject wrapped_arguments = JavaUtil.convertJavaToRuby(runtime,
					arguments);
			Log.d(this.getClass().toString(), "callMethod()");
			for (Object a : arguments) {
				Log.d(this.getClass().toString(), "arg "
						+ a.getClass().toString() + " val = " + a.toString());
			}
			IRubyObject[] args = new IRubyObject[] { wrapped_param1,
					wrapped_param2, wrapped_arguments };
			IRubyObject result;
			if (direct && !methodCache.contains(method.getName())) {
				Log.d(this.getClass().toString(),"Calling " + method.getName() + "super()");
				return ProxyBuilder.callSuper(proxy, method, arguments);
			} else {
				result = backingObject.callMethod(
				runtime.getCurrentContext(), "invoke", args);
			}
			Log.d(this.getClass().toString(), "method "
					+ method.getReturnType().toString() + " return = "
					+ result.getClass().toString());
			String return_type = method.getReturnType().toString();

			if (return_type.equals("int")) {
				Log.d(this.getClass().toString(), "casting to integer");
				return (int) ((RubyFixnum) result).getLongValue();
			} else {
				return ((Object) result.toJava(Object.class));
			}
		} catch (RaiseException e) {
			e.printStackTrace();
			executionBundle.addError(e.getMessage());
		}
		return null;
	}
}
