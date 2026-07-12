package com.craftinginterpreters.lox;

class LoxInstance {
	private LoxClass clazz;

	LoxInstance(LoxClass clazz) {
		this.clazz = clazz;
	}

	@Override
	public String toString() {
		return clazz.name + " instance";
	}
}
