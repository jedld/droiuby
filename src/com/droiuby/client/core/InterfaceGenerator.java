package com.droiuby.client.core;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.HashMap;

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
