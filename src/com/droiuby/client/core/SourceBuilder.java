package com.droiuby.client.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;
import org.jruby.Ruby;
import org.jruby.RubyBoolean;
import org.jruby.RubyFloat;
import org.jruby.RubyInteger;
import org.jruby.RubyObject;
import org.jruby.embed.ScriptingContainer;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;

import com.droiuby.client.core.utils.Utils;
import com.sun.codemodel.JArray;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JVar;

public class SourceBuilder {

	public static boolean hasDefaultConstructor(Class<?> baseClass) {
		// check if there is a default constructor
		for (Constructor<?> c : baseClass.getConstructors()) {
			if (c.getParameterTypes().length == 0)
				return true;
		}
		return false;
	}

	public static boolean inWhiteList(String name, String[] methodWhitelist) {
		if (methodWhitelist == null || methodWhitelist.length == 0)
			return true;

		for (String item : methodWhitelist) {
			if (item.equals(name))
				return true;
		}
		return false;
	}

	public static JDefinedClass buildModel(JCodeModel cm, String targetClass,
			Class<?> baseClass, String[] methodWhitelist,
			boolean includeOverridable) {
		try {
			cm.directClass("//This class has been automatically generated by the Droiuby Source Builder");
			JDefinedClass klass = cm._class(targetClass);

			JFieldVar methodCache = null;

			if (baseClass.isInterface()) {
				klass._implements(baseClass);
			} else {
				klass._extends(baseClass);
				methodCache = klass.field(JMod.PROTECTED, HashSet.class,
						"methodCache");
			}

			JFieldVar backingObject = klass.field(JMod.PROTECTED,
					RubyObject.class, "backingObject");
			JFieldVar executionBundle = klass.field(JMod.PROTECTED,
					ExecutionBundle.class, "executionBundle");
			JFieldVar container = klass.field(JMod.PROTECTED,
					ScriptingContainer.class, "container");
			JFieldVar runtime = klass.field(JMod.PROTECTED, cm.ref(Ruby.class),
					"runtime");

			// Handle constructors
			// check if there is a default constructor
			defineConstructor(cm, baseClass, klass, backingObject,
					executionBundle, container, runtime, methodCache);

			HashSet<Method> method_list = new HashSet<Method>();
			
			for (Method m : baseClass.getMethods()) {
				method_list.add(m);
			}

			for (Method m : baseClass.getDeclaredMethods()) {
				method_list.add(m);
			}

			for (Method m : method_list) {
				if (!Modifier.isFinal(m.getModifiers())
						&& !Modifier.isStatic(m.getModifiers())
						&& (Modifier.isAbstract(m.getModifiers()) || (includeOverridable
								&& !Modifier.isPrivate(m.getModifiers()) && inWhiteList(
									m.getName(), methodWhitelist)))) {
					int mod = processModifiers(m);
					JMethod rmethod = klass.method(mod, m.getReturnType(),
							m.getName());
					rmethod.annotate(java.lang.Override.class);
					JTryBlock method_block = rmethod.body()._try();
					JCatchBlock catch_block = method_block._catch(cm
							.ref(org.jruby.exceptions.RaiseException.class));

					JBlock cb = catch_block.body();
					JVar catchVar = catch_block.param("e");
					cb.invoke(catchVar, "printStackTrace");
					cb.invoke(executionBundle, "addError").arg(
							catchVar.invoke("getMessage"));

					JBlock body = method_block.body();
					String return_type = m.getReturnType().getName();

					JInvocation superInvocation = null;
					if (!Modifier.isAbstract(m.getModifiers())) {
						JConditional if_decl = body._if(methodCache.invoke(
								"contains").arg(m.getName()));
						body = if_decl._then();
						JBlock else_block = if_decl._else();

						if (!return_type.equals("void")) {
							superInvocation = JExpr._super().invoke(rmethod);
							else_block._return(superInvocation);
						} else {
							superInvocation = else_block.invoke(JExpr._super(),
									rmethod);
						}
					}

					JArray wrappedArgs = JExpr.newArray(cm
							.ref(IRubyObject.class));

					buildWrappedArgs(cm, runtime, m, rmethod, body,
							superInvocation, wrappedArgs, null);

					JVar declaration = body.decl(cm.ref(IRubyObject.class)
							.array(), "args", wrappedArgs);
					JInvocation rubyCall = JExpr.invoke(backingObject,
							"callMethod");
					rubyCall.arg(runtime.invoke("getCurrentContext"))
							.arg(JExpr.lit(m.getName())).arg(declaration);

					if (return_type.equals("void")) {
						body.add(rubyCall);
					} else {
						if (return_type.equals("boolean")) {
							JVar result = body.decl(cm.BOOLEAN, "result", cm
									.ref(Utils.class).staticInvoke("toBoolean")
									.arg(rubyCall));
							body._return(result);
							rmethod.body()._return(JExpr.lit(false));
						} else if (return_type.equals("int")) {
							JVar result = body.decl(
									cm.INT,
									"result",
									cm.ref(Utils.class)
											.staticInvoke("toInteger")
											.arg(rubyCall));
							body._return(result);
							rmethod.body()._return(JExpr.lit(0));
						} else if (return_type.equals("long")) {
							JVar result = body.decl(cm.LONG, "result",
									cm.ref(Utils.class).staticInvoke("toLong")
											.arg(rubyCall));
							body._return(result);
							rmethod.body()._return(JExpr.lit(0));
						} else if (return_type.equals("double")) {
							JInvocation returnExp = cm.ref(Utils.class)
									.staticInvoke("toFloat").arg(rubyCall);

							JVar result = body.decl(cm.FLOAT, "result",
									JExpr.cast(cm.DOUBLE, returnExp));

							body._return(result);
							rmethod.body()._return(JExpr.lit(0d));
						} else if (return_type.equals("float")) {
							JInvocation returnExp = cm.ref(Utils.class)
									.staticInvoke("toFloat").arg(rubyCall);

							JVar result = body.decl(cm.FLOAT, "result",
									JExpr.cast(cm.FLOAT, returnExp));

							body._return(result);
							rmethod.body()._return(JExpr.lit(0f));
						} else {
							JVar result = body.decl(cm.ref(IRubyObject.class),
									"result", rubyCall);
							body._return(JExpr.cast(
									cm.ref(m.getReturnType()),
									result.invoke("toJava").arg(
											JExpr.dotclass(cm.ref(m
													.getReturnType())))));
							rmethod.body()._return(JExpr._null());
						}
					}

				}
			}
			return klass;
		} catch (JClassAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	private static void buildWrappedArgs(JCodeModel cm, JFieldVar runtime,
			Object method_spec, JMethod rmethod, JBlock body,
			JInvocation superInvocation, JArray wrappedArgs, ArrayList <JVar>params) {
		int i = 0;
		
		Class<?>[] param_list = null;
		
		if (method_spec instanceof Method) {
			param_list = ((Method)method_spec).getParameterTypes();
		} else if (method_spec instanceof Constructor) {
			param_list = ((Constructor<?>)method_spec).getParameterTypes();
		}
		
		for (Class<?> param_class :param_list) {
			JVar p = null; 
			
			if (params == null) {
				p =	rmethod.param(param_class, "param" + (i++ + 1));
			} else {
				p = params.get(i++);
			}
			
			JInvocation convertStmt = null;

			if (superInvocation != null) {
				superInvocation.arg(p);
			}

			if (param_class.isPrimitive()) {
				if (param_class.getName().equals("boolean")) {
					convertStmt = cm.ref(RubyBoolean.class)
							.staticInvoke("newBoolean");
				} else if (param_class.getName().equals("int")
						|| param_class.getName().equals("long")) {
					convertStmt = cm.ref(RubyInteger.class)
							.staticInvoke("int2fix");
				} else if (param_class.getName().equals("float")
						|| param_class.getName().equals("double")) {
					convertStmt = cm.ref(RubyFloat.class)
							.staticInvoke("newFloat");
				} else {
					System.out.println("not sure how to handle "
							+ param_class.getName());
					body.directStatement("// not sure how to handle primitive type "
							+ param_class.getName());
					continue;
				}
				convertStmt.arg(runtime).arg(p);
			} else {
				convertStmt = cm.ref(JavaUtil.class).staticInvoke(
						"convertJavaToRuby");
				convertStmt.arg(runtime);
				convertStmt.arg(p);
			}
			JVar arg = body.decl(cm.ref(IRubyObject.class),
					"wrapped_" + p.name(), convertStmt);
			wrappedArgs.add(arg);
		}
	}

	private static void defineConstructor(JCodeModel cm, Class<?> baseClass,
			JDefinedClass klass, JFieldVar backingObject,
			JFieldVar executionBundle, JFieldVar container, JFieldVar runtime,
			JFieldVar methodCache) {

		if (!baseClass.isInterface()) {
			for (Constructor<?> c : baseClass.getConstructors()) {

				JMethod constructor = klass.constructor(c.getModifiers());

				JVar executionBundleObject = constructor.param(
						ExecutionBundle.class, "bundle");
				JVar rubyObject = constructor.param(RubyObject.class,
						"rubyObject");

				int i = 1;

				JInvocation invocation = constructor.body().invoke("super");

				ArrayList <JVar>params = new ArrayList<JVar>();
				for (Class<?> paramType : c.getParameterTypes()) {
					JVar param = constructor.param(paramType, "param" + i++);

					if (paramType == android.content.Context.class) {
						invocation.arg(executionBundleObject
								.invoke("getCurrentActivity"));
					} else {
						invocation.arg(param);
					}
					params.add(param);
				}

				makeConstructorBody(cm, backingObject, executionBundle,
						container, runtime, c, constructor, executionBundleObject,
						rubyObject, methodCache, params);
			}
		} else {
			JMethod constructor = klass.constructor(JMod.PUBLIC);

			JVar executionBundleObject = constructor.param(
					ExecutionBundle.class, "bundle");
			JVar rubyObject = constructor.param(RubyObject.class, "rubyObject");

			makeConstructorBody(cm, backingObject, executionBundle, container,
					runtime, null, constructor, executionBundleObject, rubyObject,
					methodCache, null);
		}
	}

	private static void makeConstructorBody(JCodeModel cm,
			JFieldVar backingObject, JFieldVar executionBundle,
			JFieldVar container, JFieldVar runtime, Constructor<?> c, JMethod constructor,
			JVar executionBundleObject, JVar rubyObject, JFieldVar methodCache, ArrayList <JVar>params) {
		constructor.body().assign(backingObject, rubyObject);
		constructor.body().assign(executionBundle, executionBundleObject);
		constructor.body().assign(container,
				executionBundleObject.invoke("getContainer"));
		constructor.body().assign(runtime,
				container.invoke("getProvider").invoke("getRuntime"));
		if (methodCache != null) {
			constructor.body().assign(
					methodCache,
					cm.ref(Utils.class)
							.staticInvoke("toStringSet")
							.arg(backingObject
									.invoke("callMethod")
									.arg("methods")
									.arg(JExpr.newArray(cm
											.ref(IRubyObject.class)))));
			
			JConditional if_decl = constructor.body()._if(methodCache.invoke(
					"contains").arg("on_initialize"));
			JBlock body = if_decl._then();
			JArray wrappedArgs = JExpr.newArray(cm
					.ref(IRubyObject.class));
			buildWrappedArgs(cm, runtime, c, constructor, body,
					null, wrappedArgs, params);
			body.invoke(backingObject,"callMethod").arg(runtime.invoke("getCurrentContext")).arg("on_initialize").arg(wrappedArgs);
		}
	}

	public static void build(String targetClass, Class<?> baseClass,
			String methodWhitelist, String outDir) {
		File f = new File(outDir);
		f.mkdirs();
		try {
			JCodeModel cm = new JCodeModel();
			String[] whitelist = StringUtils.split(methodWhitelist, '.');
			if (whitelist != null) {
				for (String item : whitelist) {
					System.out.println("allow method " + item);
				}
			}

			buildModel(cm, targetClass, baseClass, whitelist, true);
			cm.build(f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static int processModifiers(Method m) {
		int mod = 0;
		if (Modifier.isPrivate(m.getModifiers())) {
			mod |= JMod.PRIVATE;
		}
		if (Modifier.isPublic(m.getModifiers())) {
			mod |= JMod.PUBLIC;
		}
		if (Modifier.isProtected(m.getModifiers())) {
			mod |= JMod.PROTECTED;
		}
		if (Modifier.isStatic(m.getModifiers())) {
			mod |= JMod.STATIC;
		}
		return mod;
	}

}
