package de.smasek.nmnp.transformator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ObjectArrays;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.expr.MethodCall;

public final class Transformator {

	private static final String METHOD_POSTFIX = "WhenNotNull";
	private static final String BODY = "{ if ($1 != null) { return ($r)$1.%s(%s); } return null; }";
	private static final String CALL = "$_ = ($r) %s" + METHOD_POSTFIX + "($0, $$);";
	private static final String ARGS = " $2, $3, $4, $5, $6, $7, $8, $9,$10,$11,$12,$13,$14,$15,$16,$17";

	private final CtClass mappingClass;
	private final CtClass listClass;
	private final CtClass objectClass;

	public Transformator(String className) throws Exception {
		ClassPool pool = ClassPool.getDefault();
		listClass = pool.get(List.class.getName());
		objectClass = pool.get(Object.class.getName());
		mappingClass = pool.getAndRename(className, className + "NullPrevented");
		mappingClass.setSuperclass(pool.get(className));
	}

	public Class<?> transform() throws Exception {
		mappingClass.addMethod(createSaveGetMethod());
		mappingClass.instrument(new MethodCallRedirectorToSaveGet());
		mappingClass.instrument(new MethodCallRedirectorToCheckNull());
		mappingClass.writeFile();
		return mappingClass.toClass();
	}

	private CtMethod createSaveGetMethod() throws CannotCompileException {
		return CtNewMethod.make(objectClass, "$saveGet", new CtClass[] { listClass, CtClass.intType },
				null, "{return $1 == null ? null : ($1.size() <= $2 ? null : $1.get($2));}", mappingClass);
	}

	private final class MethodCallRedirectorToSaveGet extends ExprEditorWithCondition<MethodCall> {
		@Override
		public boolean isCandidate(MethodCall methodCall) throws Exception {
			if (!methodCall.where().hasAnnotation(PreventNullPointerException.class)) {
				return false;
			}
			if (!methodCall.getMethodName().equals("get")) {
				return false;
			}
			if (!methodCall.getMethod().getDeclaringClass().subclassOf(listClass)) {
				return false;
			}
			return true;
		}

		@Override
		public void doEdit(MethodCall methodCall) throws Exception {
			methodCall.replace("$_ = $saveGet($0, $1);");
		}
	}

	private final class MethodCallRedirectorToCheckNull extends ExprEditorWithCondition<MethodCall> {
		private final Set<String> transformedMethods = new HashSet<String>();

		@Override
		public boolean isCandidate(MethodCall methodCall) throws Exception {
			return methodCall.where().hasAnnotation(PreventNullPointerException.class);
		}

		@Override
		public void doEdit(MethodCall methodCall) throws Exception {
			addWhenNotNullMethodDoesNotExist(methodCall);
			methodCall.replace(String.format(CALL, methodCall.getMethodName()));
		}

		private void addWhenNotNullMethodDoesNotExist(MethodCall methodCall) throws Exception {
			if (!transformedMethods.contains(methodCall.getMethod().getLongName())) {
				mappingClass.addMethod(new WhenNotNullMethodCreator(methodCall.getMethod()).createWhenNotNullMethod());
				transformedMethods.add(methodCall.getMethod().getLongName());
			}
		}

		private final class WhenNotNullMethodCreator {
			private final CtMethod method;

			public WhenNotNullMethodCreator(CtMethod method) {
				this.method = method;
			}

			private CtMethod createWhenNotNullMethod() throws CannotCompileException, NotFoundException {
				return CtNewMethod.make(method.getReturnType(), method.getName() + METHOD_POSTFIX,
						concatTargetToParameterTypes(), method.getExceptionTypes(),
						String.format(BODY, method.getName(), useArgs()), mappingClass);
			}

			private CtClass[] concatTargetToParameterTypes() throws NotFoundException {
				return ObjectArrays.concat(method.getDeclaringClass(), method.getParameterTypes());
			}

			private String useArgs() throws NotFoundException {
				return ARGS.substring(0, Math.max(0, method.getParameterTypes().length * 4 - 1));
			}
		}
	}
}