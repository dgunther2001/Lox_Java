package lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class Lox {
    static boolean hadError = false;
    public static void main(String[] args) throws IOException{
        if (args.length > 1) {
            System.out.println("Usage jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    /*
     * If given a file, our interpreter finds it and executes it
     */
    private static void runFile(String path) throws IOException{
        byte[] bytes  = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) System.exit(65);
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

        for(Token token: tokens) {
            System.out.println(token);
        }
    }

    /*
     * Some basic error handline mechanisms
     */
    static void error(int line, String message) {
        report(line, "", message); // gives us a line
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message); // this is the actual error message output to the user
        hadError = true;
    }
}
