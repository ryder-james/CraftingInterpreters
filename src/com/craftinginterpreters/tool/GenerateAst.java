package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("Usage: generate_ast <output directory>");
			System.exit(64);
		}
		String outputDir = args[0];
		defineAst(outputDir, "Expr", Arrays.asList(
			"Assign		: Token name, Expr value",
			"Binary		: Expr left, Token operator, Expr right",
			"Grouping	: Expr expression",
			"Literal	: Object value",
			"Logical	: Expr left, Token operator, Expr right",
			"Unary		: Token operator, Expr right",
			"Variable	: Token name"
		));
		
		defineAst(outputDir, "Stmt", Arrays.asList(
			"Block		: List<Stmt> statements",
			"Expression : Expr expression",
			"If			: Expr condition, Stmt thenBranch," +
						" Stmt elseBranch",
			"Print		: Expr expression",
			"Var		: Token name, Expr initializer",
			"While		: Expr condition, Stmt body"
		));
	}

	private static void defineAst(
			String outputDir, String baseName, List<String> types) 
			throws IOException 
	{
		String path = outputDir + "\\" + baseName + ".java";
		ClassWriter writer = new ClassWriter(path);

		writer.println("package com.craftinginterpreters.lox;");
		writer.println();
		writer.println("import java.util.List;");
		writer.println();
		writer.openScope("abstract class " + baseName);

		defineVisitor(writer, baseName, types);

		// The AST classes.
		for (String type : types) {
			writer.println();
			String className = type.split(":")[0].trim();
			String fields = type.split(":")[1].trim();
			defineType(writer, baseName, className, fields);
		}

		// The base accept() method.
		writer.println();
		writer.println("abstract <R> R accept(Visitor<R> visitor);");

		writer.closeScope();
		writer.close();
	}


	private static void defineVisitor(
			ClassWriter writer, String baseName, List<String> types) 
	{
		writer.openScope("interface Visitor<R>");

		for (String type : types) {
			String typeName = type.split(":")[0].trim();
			writer.println("R visit" + typeName + baseName + "(" +
					typeName + " " + baseName.toLowerCase() + ");");
		}

		writer.closeScope();
	}


	private static void defineType(
			ClassWriter writer, String baseName,
			String className, String fieldList) 
	{
		writer.openScope("static class " + className + " extends " + baseName);
		
		// Store parameters in fields.
		String[] fields = fieldList.split(", ");

		// Fields.
		for (String field : fields) {
			writer.println("final " + field + ";");
		}

		writer.println();

		// Constructor.
		writer.openScope(className + "(" + fieldList + ")");
		for (String field : fields) {
			String name = field.split(" ")[1];
			writer.println("this." + name + " = " + name + ";");
		}
		writer.closeScope();

		writer.println();
		writer.println("@Override");
		writer.openScope("<R> R accept(Visitor<R> visitor)");
		writer.println("return visitor.visit" + className + baseName + "(this);");
		writer.closeScope();

		writer.closeScope();
	}

	static class ClassWriter extends PrintWriter {
		private final boolean indentAsTabs;
		private int depth = 0;

		ClassWriter(String path) throws IOException {
			this(path, true);
		}

		ClassWriter(String path, boolean indentAsTabs) throws IOException {
			super(path, "UTF-8");
			this.indentAsTabs = indentAsTabs;
		}

		void openScope(String scopeLine) {
			println(scopeLine + " {");
			depth++;
		}

		void closeScope() {
			depth--;
			if (depth < 0) {
				depth = 0;
			}
			println("}");
		}

		@Override
		public void println() {
			super.print(getIndentation());
			super.println();
		}

		@Override
		public void println(String x) {
			super.println(getIndentation() + x);
		}

		private String getIndentation() {
			if (indentAsTabs)
				return "\t".repeat(depth);
			return "    ".repeat(depth);
		}
	}
}
