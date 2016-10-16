package de.smasek.nmnp.transformator;

import javassist.CannotCompileException;
import javassist.expr.Cast;
import javassist.expr.Expr;
import javassist.expr.ExprEditor;

public abstract class ExprEditorWithCondition<E extends Expr> extends ExprEditor {
	
	public abstract boolean isCandidate(E canditate) throws Exception; 
	
	
	@SuppressWarnings("unchecked")
	@Override
	public final void edit(Cast c) throws CannotCompileException {
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


	public abstract void doEdit(E c) throws Exception;
}
