package com.droiuby.client.core;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;

import org.jruby.RubyArray;
import org.jruby.javasupport.JavaClass;
import org.jruby.javasupport.JavaObject;

import android.content.Context;

import com.google.dexmaker.stock.ProxyBuilder;

public class InterfaceGenerator {

	static HashMap<String, Object> cache = new HashMap<String, Object>();

	public static Class wrapperForClass(Context context, String className,
			InvocationHandler wrapper) throws ClassNotFoundException {

				Class<?> klass = Class.forName(className);
				ProxyBuilder<?> builder = ProxyBuilder.forClass(klass).dexCache(
						context.getDir("dx", Context.MODE_PRIVATE));
				try {
					return builder.handler(wrapper).buildProxyClass();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
	}
	
	public static Object wrapperForInstance(Context context, String className,
			InvocationHandler wrapper, RubyArray argTypes, RubyArray argValues) throws ClassNotFoundException {
		Class<?> klass = Class.forName(className);
		Object builder = ProxyBuilder.forClass(klass).dexCache(
						context.getDir("dx", Context.MODE_PRIVATE));
		try {
			return ((ProxyBuilder<?>) builder).handler(wrapper).constructorArgTypes(toTypeArray(argTypes)).constructorArgValues(toValueArray(argValues)).build();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		return null;
	}

	private static Class<?>[] toTypeArray(RubyArray argTypes) {
		ArrayList<Class<?>> constructorType = new ArrayList<Class<?>>();
		for(int i = 0; i < argTypes.getLength(); i++) {
			constructorType.add( (Class<?>)argTypes.get(i));
		}
		Class<?> ctype[] = new Class<?>[argTypes.getLength()];
		return constructorType.toArray(ctype);
	}

	private static Object[] toValueArray(RubyArray valueType) {
		ArrayList<Object> constructorType = new ArrayList<Object>();
		
		for(int i = 0; i < valueType.getLength(); i++) {
			constructorType.add(valueType.get(i));
		}
		
		Object ctype[] = new Object[valueType.getLength()];
		return constructorType.toArray(ctype);
	}
	
	public static Object wrapperForClassOrInterface(Context context, String className,
			InvocationHandler wrapper) throws ClassNotFoundException {

		Object builder;
		if (cache.containsKey(className)) {
			builder = cache.get(className);
		} else {
			Class<?> klass = Class.forName(className);
			if (klass.isInterface()) {
				builder = Proxy.getProxyClass(klass.getClassLoader(),
						new Class[] { klass });
			} else {
				builder = ProxyBuilder.forClass(klass).dexCache(
						context.getDir("dx", Context.MODE_PRIVATE));
			}
		}

		try {

			if (builder instanceof ProxyBuilder) {
				return ((ProxyBuilder<?>) builder).handler(wrapper).build();
			} else if (builder instanceof Class) {
				return ((Class<?>) builder).getConstructor(
						new Class[] { InvocationHandler.class }).newInstance(
						new Object[] { wrapper });
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}

}
