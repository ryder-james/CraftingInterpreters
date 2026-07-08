package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Stmt.Expression;
import com.craftinginterpreters.lox.Stmt.Var;

class REPLInterpreter extends Interpreter {

	@Override
	public Void visitExpressionStmt(Expression stmt) {
		super.visitExpressionStmt(stmt);
		Object val = evaluate(stmt.expression);
		System.out.println(stringify(val));
		return null;
	}

	@Override
	public Void visitVarStmt(Var stmt) {
		// TODO Auto-generated method stub
		super.visitVarStmt(stmt);
		Object val = environment.get(stmt.name);
		System.out.println(stringify(val));
		return null;
	}
	
}
