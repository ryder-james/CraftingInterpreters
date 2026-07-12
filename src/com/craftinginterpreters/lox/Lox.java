
package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
	private static Interpreter interpreter;
	static boolean hadError = false;
	static boolean hadRuntimeError = false;
	
	public static void main(String[] args) throws IOException {
		if (args.length == 1 && args[0].equals("-i")) {
			runInteractive();
		} else if (args.length == 2 && args[0].equals("-f")) {
			interpreter = new Interpreter();
			runFile(args[1]);
		} else if (args.length == 0) {
			runPrompt();
		} else {
			System.out.println(args.length + " " + args[0]);
			System.out.print("Usage: jlox [-i] [-f script_file]");
			System.exit(64);
		}
	}

	private static void runFile(String path) throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		run(new String(bytes, Charset.defaultCharset()));

		// Indicate an error in the exit code.
		if (hadError) System.exit(65);
		if (hadRuntimeError) System.exit(70);
	}

	private static void runInteractive() throws IOException {
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);
		String line = "";
		while (!line.equals("quit")) {
			System.out.print("Choose 'file', 'repl' or 'quit': ");
			line = reader.readLine().toLowerCase();
			if (line.equals("file")) {
				System.out.print("Enter file name: ");
				line = reader.readLine();
				interpreter = new Interpreter();
				runFile(".\\loxsrc\\" + line + ".lox");
				break;
			} else if (line.equals("repl")) {
				interpreter = new REPLInterpreter();
				runPrompt();
				break;
			} else if (!line.equals("quit")) {
				System.out.println("Try again.");
			}
		}
	}

	private static void runPrompt() throws IOException {
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);

		for (;;) {
			System.out.print("> ");
			String line = reader.readLine();
			if (line == null) break;
			run(line);
			hadError = false;
		}
	}

	private static void run(String source) {
		Scanner scanner = new Scanner(source);
		List<Token> tokens = scanner.scanTokens();
		Parser parser = new Parser(tokens);
		List<Stmt> statements = parser.parse();
		
		if (hadError) return;

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        if (hadError) return;

		interpreter.interpret(statements);
	}

	static void error(int line, String message) {
		report(line, "", message);
	}
	
	static void error(Token token, String message) {
		if (token.type == TokenType.EOF) {
			report(token.line, " at end", message);
		} else {
			report(token.line, " at '" + token.lexeme + "'", message);
		}
	}
	
	static void runtimeError(RuntimeError error) {
		System.err.println(error.getMessage() +
				"\n[line " + error.token.line + "]");
		hadRuntimeError = true;
	}

	private static void report(int line, String where, String message) {
		System.err.println("[line " + line + "] Error" + where + ": " + message);
		hadError = true;
	}
}
