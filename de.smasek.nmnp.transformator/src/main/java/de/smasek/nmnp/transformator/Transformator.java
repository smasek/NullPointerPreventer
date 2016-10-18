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
		mappingClass.instrument(new WhenNotNullMethodCreator());
		mappingClass.instrument(new MethodCallRedirectorToCheckNull());
		mappingClass.writeFile();
		return mappingClass.toClass();
	}

	private CtMethod createSaveGetMethod() throws CannotCompileException {
		return CtNewMethod.make(objectClass, "$saveGet", new CtClass[] { listClass, CtClass.intType }, null,
				"{return $1 == null ? null : ($1.size() <= $2 ? null : $1.get($2));}", mappingClass);
	}

	private boolean isListGetMethodCall(MethodCall methodCall) throws NotFoundException {
		if (!methodCall.getMethodName().equals("get")) {
			return false;
		}
		if (!methodCall.getMethod().getDeclaringClass().subclassOf(listClass)) {
			return false;
		}
		return true;
	}

	private boolean isInPrecentNullPointerMethod(MethodCall methodCall) {
		return methodCall.where().hasAnnotation(PreventNullPointerException.class);
	}

	private final class MethodCallRedirectorToSaveGet extends ExprEditorWithCondition<MethodCall> {
		@Override
		public boolean isCandidate(MethodCall methodCall) throws Exception {
			if (!isInPrecentNullPointerMethod(methodCall)) {
				return false;
			}
			return isListGetMethodCall(methodCall);
		}

		@Override
		public void doEdit(MethodCall methodCall) throws Exception {
			methodCall.replace("$_ = $saveGet($0, $1);");
		}
	}

	private final class MethodCallRedirectorToCheckNull extends ExprEditorWithCondition<MethodCall> {

		@Override
		public boolean isCandidate(MethodCall methodCall) throws Exception {
			if (!isInPrecentNullPointerMethod(methodCall)) {
				return false;
			}
			return !isListGetMethodCall(methodCall);
		}

		@Override
		public void doEdit(MethodCall methodCall) throws Exception {
			methodCall.replace(String.format(CALL, methodCall.getMethodName()));
		}
	}

	private final class WhenNotNullMethodCreator extends ExprEditorWithCondition<MethodCall> {
		private final Set<String> transformedMethods = new HashSet<String>();

		@Override
		public boolean isCandidate(MethodCall methodCall) throws Exception {
			if (!isInPrecentNullPointerMethod(methodCall)) {
				return false;
			}
			if  (isListGetMethodCall(methodCall)) {
				return false;
			}
			return transformedMethods.add(methodCall.getMethod().getLongName());
		}
		
		@Override
		public void doEdit(MethodCall methodCall) throws Exception {
			mappingClass.addMethod(createWhenNotNullMethod(methodCall.getMethod()));
		}

		private CtMethod createWhenNotNullMethod(CtMethod method) throws CannotCompileException, NotFoundException {
			return CtNewMethod.make(method.getReturnType(), method.getName() + METHOD_POSTFIX,
					concatTargetToParameterTypes(method), method.getExceptionTypes(),
					String.format(BODY, method.getName(), useArgs(method)), mappingClass);
		}

		private CtClass[] concatTargetToParameterTypes(CtMethod method) throws NotFoundException {
			return ObjectArrays.concat(method.getDeclaringClass(), method.getParameterTypes());
		}

		private String useArgs(CtMethod method) throws NotFoundException {
			return ARGS.substring(0, Math.max(0, method.getParameterTypes().length * 4 - 1));
		}
	}
}