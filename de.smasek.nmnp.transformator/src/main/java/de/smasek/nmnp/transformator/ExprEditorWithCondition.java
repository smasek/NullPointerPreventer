package de.smasek.nmnp.transformator;

import javassist.CannotCompileException;
import javassist.expr.Cast;
import javassist.expr.ConstructorCall;
import javassist.expr.Expr;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.Handler;
import javassist.expr.Instanceof;
import javassist.expr.MethodCall;
import javassist.expr.NewArray;
import javassist.expr.NewExpr;

public abstract class ExprEditorWithCondition<E extends Expr> extends ExprEditor {
	
	public abstract boolean isCandidate(E canditate) throws Exception; 
	public abstract void doEdit(E c) throws Exception;
	
	@Override
	public final void edit(Cast c) throws CannotCompileException { _edit(c); }
	
	@Override
	public final void edit(ConstructorCall c) throws CannotCompileException { _edit(c); }
	
	@Override
	public final void edit(FieldAccess f) throws CannotCompileException { _edit(f); }
	
	@Override
	public final void edit(Handler h) throws CannotCompileException { _edit(h); }
	
	@Override
	public final void edit(Instanceof i) throws CannotCompileException { _edit(i); }
	
	@Override
	public final void edit(MethodCall m) throws CannotCompileException { _edit(m); }
	
	@Override
	public final void edit(NewArray a) throws CannotCompileException { _edit(a); }
	
	@Override
	public final void edit(NewExpr e) throws CannotCompileException { _edit(e); }
	
	@SuppressWarnings("unchecked")
	private void _edit(Object c) throws CannotCompileException {
		if (_isCandidate(c)) {
			try {
				doEdit((E) c);
			} catch (Exception e) {
				throw new CannotCompileException(e);
			}
		}
	}


	@SuppressWarnings("unchecked")
	private boolean _isCandidate(Object canditate) {
		try {
			return isCandidate((E)canditate);
		} catch (Exception e) {
			return false;
		}
	}


}
