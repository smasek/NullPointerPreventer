package de.smasek.nmnp.transformator;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ObjectArrays;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public final class Transformator {
	
	private static final String METHOD_POSTFIX = "WhenNotNull";
	private static final String BODY = "{ if ($1 != null) { return ($r)$1.%s(%s); } return null; }";
	private static final String CALL = "$_ = ($r) %s" + METHOD_POSTFIX + "($0, $$);";
	private static final String ARGS = " $2, $3, $4, $5, $6, $7, $8, $9,$10,$11,$12,$13,$14,$15,$16,$17";

	private final CtClass mappingClass;

	public Transformator(String className) throws Exception {
		ClassPool pool = ClassPool.getDefault();
		mappingClass = pool.getAndRename(className, className + "NullPrevented");
		mappingClass.setSuperclass(pool.get(className));
	}

	public Class<?> transform() throws Exception {
		mappingClass.instrument(new MethodCallRedirector());
		return mappingClass.toClass();
	}
	
	private final class MethodCallRedirector extends ExprEditor {
		private final Set<String> transformedMethods = new HashSet<String>();
		@Override
		public void edit(MethodCall methodCall) throws CannotCompileException {
				if (!methodCall.where().hasAnnotation(PreventNullPointerException.class)) {
					return;
				}
				addWhenNotNullMethod(methodCall);
				methodCall.replace(String.format(CALL, methodCall.getMethodName()));
		}

		private void addWhenNotNullMethod(MethodCall methodCall) throws CannotCompileException {
			try {
				if (!transformedMethods.contains(methodCall.getMethod().getLongName())) {
					mappingClass.addMethod(new WhenNotNullMethodCreator(methodCall.getMethod()).createWhenNotNullMethod());
					transformedMethods.add(methodCall.getMethod().getLongName());
				}
			} catch (NotFoundException e) {
				throw new CannotCompileException(e);
			}
		}

		private final class WhenNotNullMethodCreator {
			private final CtMethod method;
			
			public WhenNotNullMethodCreator(CtMethod method) {
				this.method = method;
			}
			
			private CtMethod createWhenNotNullMethod() throws CannotCompileException, NotFoundException  {
				return CtNewMethod.make(method.getReturnType(), method.getName() + METHOD_POSTFIX,
						concatTargetToParameterTypes(), method.getExceptionTypes(), 
						String.format(BODY, method.getName(), useArgs()), mappingClass);
			}

			private CtClass[] concatTargetToParameterTypes() throws NotFoundException {
				return ObjectArrays.concat(method.getDeclaringClass(), method.getParameterTypes());
			}

			private String useArgs() throws NotFoundException {
				return ARGS.substring(0, Math.max(0, method.getParameterTypes().length * 4 -1));
			}
		}
	}
}