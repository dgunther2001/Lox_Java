package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lox.TokenType.*;

// storing the raw code as a string
class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    Scanner(String source) { 
        this.source = source;
    }

    List<Token> scanTokens() {
        // scans tokens while not at the end of the program
        while(!isAtEnd()) {
            start = current;
            scanToken();
        }
    
        // creates individual lexemes and stores them in a list
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }
    
    private void scanToken() {
        char c = advance();
        switch(c) {
            // single character tokens
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            // single and two character tokens
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL: LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            // dealing with the / character as it delineates comments as well
            case '/':
                if(match('/')) { // makes a comment
                    while (peek() != '\n' && !isAtEnd()) advance(); // consumes characters until the end of the line
                } else { // adds the '/' character as a lexeme to the list
                    addToken(SLASH);
                }
                break;
            // dealing with whitespace and newlines
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;
            // dealing with string literals
            case '"': string(); break;
            // the default casee throws an error
            default:
                if (isDigit(c)) { // calls a number function if it is a number
                    number();
                } 
                else if (isAlpha(c)) {
                    identifier();
                }
                else {
                    Lox.error(line, "Unexpected character."); // sets has error, so we never try to execute the code, but do print all errors
                }
                break;
        }
    }

    // consumes an entire identifier into 1 token
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text); // seeing if the substring is a keyword
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    // consumes the rest of the number into one lexeme
    private void number() {
        while (isDigit(peek())) advance();
        
        // check to see if there is a fractional part
        if (peek() == '.' && isDigit(peekNext())) {
            advance(); // consumes the decimal

            while (isDigit(peek())) advance(); // proceeds as if its an integer
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current))); // adds the completed int/floating point number as 1 completer token
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) { // keeps advancing letters until the string is terminated, or we hit a newline
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) { // if the string isn't closed before the next line
            Lox.error(line, "Unterminated string.");
            return;
        }

        advance();

        String value = source.substring(start+1, current-1); // removes the " & " around the string and just saves the actual values
        addToken(STRING, value); // creates a string token of the entire string and adds it to the token list
    }

    // "consume" the next character if it matches what we're looking for in terms of the double character tokens
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    // allows me to peek ahead to characters in the sequence
    private char peek() {
        if(isAtEnd()) return '\0'; // like the advance function, but it DOES NOT consume a character
        return source.charAt(current);
    }

    // allows us to peek 2 characters ahead (for dealing with floating point numbers)
    private char peekNext() {
        if (current +1 >= source.length()) return '\0';
        return source.charAt(current+1);
    }

    // checks to see if a character is an alphabetic
    private boolean isAlpha(char c) {
        return (c <= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c == '_');
    }

    // checks to see if a character is an alpha numeric
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    // checks to see if a character is a digit
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // checks to see if the line is greater than or equal to number of lines in the source code
    private boolean isAtEnd() {
        return current >= source.length();
    }
    
    // helper methods
    private char advance() { // advances to next character
        current++;
        return source.charAt(current - 1);
    }
    
    private void addToken(TokenType type) { // adds a token to the list of lexemes
        addToken(type, null);
    }
    
    private void addToken(TokenType type, Object literal) { // adds a token to the list of lexemes
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

}



