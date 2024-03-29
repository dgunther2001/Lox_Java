package lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    public static void main(String[] args) throws IOException{
        /* 
        if (args.length > 1) {
            System.out.println("Usage jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
        */
        runFile("loxtest");
    }

    /*
     * If given a file, our interpreter finds it and executes it
     */
    private static void runFile(String path) throws IOException{
        byte[] bytes  = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    /*
     * Allows us to enter and execute code 1 line at a time
     */
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for(;;) {
            // adds a prompt
            System.out.println("> ");
            String line = reader.readLine(); // reads in a line from the user
            if (line == null) break; //exits the loop if we type in nothing
            run(line); //runs and executes the line
            hadError = false;
        }
    }


    /*
     * Tokenizes the line and runs it
     */
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        if(hadError) return;

        interpreter.interpret(statements);
    }

    /*
     * Some basic error handling molechanisms
     */
    static void error(int line, String message) {
        report(line, "", message); // gives us a line
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.token.line + " ]");
        hadRuntimeError = true;
    }

    static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message); // this is the actual error message output to the user
        hadError = true;
    }

    static void error(Token token, String message) { 
        if (token.type == TokenType.EOF) { // if we're at the end of the file and an expression is unclosed, it says so
            report(token.line, " at end", message);
        } else { // otherwise, it reports the problematic lexeme and the line it's on
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }
}
