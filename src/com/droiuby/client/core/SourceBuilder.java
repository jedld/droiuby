package com.droiuby.client.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import org.jruby.Ruby;
import org.jruby.RubyBoolean;
import org.jruby.RubyInteger;
import org.jruby.RubyObject;
import org.jruby.embed.ScriptingContainer;
import org.jruby.ext.jruby.JRubyUtilLibrary.StringUtils;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.builtin.IRubyObject;

import com.droiuby.client.core.ExecutionBundle;
import com.droiuby.client.core.utils.Utils;
import com.sun.codemodel.JArray;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpressionImpl;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

public class SourceBuilder {

	public static JDefinedClass buildModel(JCodeModel cm, String targetClass, Class<?> baseClass) {
		try {
			JDefinedClass klass = cm._class(targetClass);
			if (baseClass.isInterface()) {
				klass._implements(baseClass);
			} else {
				klass._extends(baseClass);
			}

			JFieldVar backingObject = klass.field(JMod.PROTECTED,
					RubyObject.class, "backingObject");
			JFieldVar executionBundle = klass.field(JMod.PROTECTED,
					ExecutionBundle.class, "executionBundle");
			JFieldVar container = klass.field(JMod.PROTECTED,
					ScriptingContainer.class, "container");

			JMethod constructor = klass.constructor(JMod.PUBLIC);

			JVar executionBundleObject = constructor.param(
					ExecutionBundle.class, "bundle");
			JVar rubyObject = constructor.param(RubyObject.class, "rubyObject");
			constructor.body().assign(backingObject, rubyObject);
			constructor.body().assign(executionBundle, executionBundleObject);
			constructor.body().assign(container,
					executionBundleObject.invoke("getContainer"));

			for (Method m : baseClass.getMethods()) {
				if (Modifier.isAbstract(m.getModifiers())) {
					int mod = processModifiers(m);
					JMethod rmethod = klass.method(mod, m.getReturnType(),
							m.getName());
					int i = 1;
					JTryBlock method_block = rmethod.body()._try();
					JCatchBlock catch_block = method_block._catch(cm
							.ref(org.jruby.exceptions.RaiseException.class));

					JBlock cb = catch_block.body();
					JVar catchVar = catch_block.param("e");
					cb.invoke(catchVar, "printStackTrace");
					cb.invoke(executionBundle, "addError").arg(
							catchVar.invoke("getMessage"));

					JBlock body = method_block.body();
					JVar runtime = body.decl(cm.ref(Ruby.class), "runtime",
							container.invoke("getProvider")
									.invoke("getRuntime"));

					JArray wrappedArgs = JExpr.newArray(cm
							.ref(IRubyObject.class));

					for (Class<?> param_class : m.getParameterTypes()) {
						JVar p = rmethod.param(param_class, "param" + i++);
						JInvocation convertStmt = null;
						if (param_class.isPrimitive()) {
							if (param_class.getName().equals("boolean")) {
								convertStmt = cm.ref(RubyBoolean.class)
										.staticInvoke("newBoolean");
							} else if (param_class.getName().equals("int")) {
								convertStmt = cm.ref(RubyInteger.class)
										.staticInvoke("int2fix");
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

					JVar declaration = body.decl(cm.ref(IRubyObject.class)
							.array(), "args", wrappedArgs);
					JInvocation rubyCall = JExpr.invoke(backingObject,
							"callMethod");
					rubyCall.arg(runtime.invoke("getCurrentContext")).arg(JExpr.lit(m.getName())).arg(
							declaration);
					
					String return_type = m.getReturnType().getName();
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
						} else {
							body.decl(cm.ref(IRubyObject.class), "result",
									rubyCall);
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

	public static void build(String targetClass, Class<?> baseClass, String outDir) {
		File f = new File(outDir);
		f.mkdirs();
		try {
			JCodeModel cm = new JCodeModel();
			buildModel(cm, targetClass, baseClass);
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
