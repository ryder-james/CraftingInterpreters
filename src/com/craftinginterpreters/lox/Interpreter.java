package com.craftinginterpreters.lox;

import java.util.List;

import com.craftinginterpreters.lox.Expr.Variable;
import com.craftinginterpreters.lox.Stmt.Expression;
import com.craftinginterpreters.lox.Stmt.Print;
import com.craftinginterpreters.lox.Stmt.Var;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
	private Environment environment = new Environment();
	
	void interpret(List<Stmt> statements) {
		try {
			for (Stmt stmt : statements) {
				execute(stmt);
			}
		} catch (RuntimeError er) {
			Lox.runtimeError(er);
		}
	}
	
	@Override
	public Object visitAssignExpr(Expr.Assign expr) {
		Object value = evaluate(expr.value);
		environment.assign(expr.name, value);
		return value;
	}
	
	@Override
	public Object visitBinaryExpr(Expr.Binary expr) {
		Object left = evaluate(expr.left);
		Object right = evaluate(expr.right);
		
		switch (expr.operator.type) {
		case GREATER:
			checkNumberOperands(expr.operator, left, right);
			return (double)left > (double)right;
		case GREATER_EQUAL:
			checkNumberOperands(expr.operator, left, right);
			return (double)left >= (double)right;
		case LESS:
			checkNumberOperands(expr.operator, left, right);
			return (double)left < (double)right;
		case LESS_EQUAL:
			checkNumberOperands(expr.operator, left, right);
			return (double)left <= (double)right;
		case BANG_EQUAL:
			return !isEqual(left, right);
		case EQUAL_EQUAL:
			return isEqual(left, right);
		case MINUS:
			checkNumberOperands(expr.operator, left, right);
			return (double)left - (double)right;
		case PLUS:
			if (left instanceof Double && right instanceof Double) {
				return (double)left + (double)right;
			}
			if (left instanceof String || right instanceof String) {
				return stringify(left) + stringify(right);
			}
			
			throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
		case SLASH:
			checkNumberOperands(expr.operator, left, right);
			if ((double)right == 0.0) {
				throw new RuntimeError(expr.operator, "Cannot divide by 0.");
			}
			return (double)left / (double)right;
		case STAR:
			checkNumberOperands(expr.operator, left, right);
			return (double)left * (double)right;
		default:
			// Unreachable.
			return null;
		}
	}

	@Override
	public Object visitGroupingExpr(Expr.Grouping expr) {
		return evaluate(expr.expression);
	}

	@Override
	public Object visitLiteralExpr(Expr.Literal expr) {
		return expr.value;
	}

	@Override
	public Object visitUnaryExpr(Expr.Unary expr) {
		Object right = evaluate(expr.right);
		
		switch (expr.operator.type) {
		case BANG:
			return !isTruthy(right);
		case MINUS:
			checkNumberOperand(expr.operator, right);
			return -(double)right;
		default:
			// Unreachable.
			return null;
		}
	}

	@Override
	public Object visitVariableExpr(Variable expr) {
		return environment.get(expr.name);
	}


	@Override
	public Void visitExpressionStmt(Expression stmt) {
		evaluate(stmt.expression);
		return null;
	}

	@Override
	public Void visitPrintStmt(Print stmt) {
		Object value = evaluate(stmt.expression);
		System.out.println(stringify(value));
		return null;
	}

	@Override
	public Void visitVarStmt(Var stmt) {
		Object value = null;
		if (stmt.initializer != null) {
			value = evaluate(stmt.initializer);
		}
		
		environment.define(stmt.name.lexeme, value);
		return null;
	}
	private Object evaluate(Expr expr) {
		return expr.accept(this);
	}
	
	private void execute(Stmt stmt) {
		stmt.accept(this);
	}
	
	private boolean isEqual(Object a, Object b) {
		if (a == null) return b == null;
		
		return a.equals(b);
	}
	
	private boolean isTruthy(Object obj) {
		if (obj == null) return false;
		if (obj instanceof Boolean) return (boolean)obj;
		return true;
	}
	
	private void checkNumberOperand(Token operator, Object operand) {
		if (operand instanceof Double) return;
		throw new RuntimeError(operator, "Operand must be a number.");
	}
	
	private void checkNumberOperands(Token operator, Object left, Object right) {
		if (left instanceof Double && right instanceof Double) return;
		throw new RuntimeError(operator, "Operands must be numbers.");
	}
	
	private String stringify(Object obj) {
		if (obj == null) return "nil";
		
		if (obj instanceof Double) {
			String text = obj.toString();
			if (text.endsWith(".0")) {
				text = text.substring(0, text.length() - 2);
			}
			return text;
		}
		
		return obj.toString();
	}
}
