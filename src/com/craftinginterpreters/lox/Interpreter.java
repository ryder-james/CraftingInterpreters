package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.Binary;
import com.craftinginterpreters.lox.Expr.Grouping;
import com.craftinginterpreters.lox.Expr.Literal;
import com.craftinginterpreters.lox.Expr.Unary;
import com.craftinginterpreters.lox.Expr.Visitor;

class Interpreter implements Visitor<Object> {
	void interpret(Expr expression) {
		try {
			Object value = evaluate(expression);
			System.out.println(stringify(value));
		} catch (RuntimeError er) {
			Lox.runtimeError(er);
		}
	}
	
	@Override
	public Object visitBinaryExpr(Binary expr) {
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
			if (left instanceof String && right instanceof String) {
				return (String)left + (String)right;
			}
			
			throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
		case SLASH:
			checkNumberOperands(expr.operator, left, right);
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
	public Object visitGroupingExpr(Grouping expr) {
		return evaluate(expr.expression);
	}

	@Override
	public Object visitLiteralExpr(Literal expr) {
		return expr.value;
	}

	@Override
	public Object visitUnaryExpr(Unary expr) {
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

	private Object evaluate(Expr expr) {
		return expr.accept(this);
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
