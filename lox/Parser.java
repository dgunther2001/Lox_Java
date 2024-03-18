package lox;

import java.util.List;

import static lox.TokenType.*;

/*
 *  DEFINING OUR ORDER OF OPERATIONS AND NESTED GRAMMARS
 *  expression -> equality ; 
 *  equality   -> comparison ( ( "!=" | "==" ) comparison )* ;
 *  comparison -> term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
 *  term       -> factor ( ( "-" | "+" ) factor )* ;
 *  factor     -> unary ( ( "/" | "*" ) unary )* ;
 *  unary      -> ( "!" | "-" ) unary | primary ;
 *  primary    -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
 */

class Parser  { // consumes a flat input sequence of tokens, which are eventually going to be parsed
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0; // sets the current token to 0

    Parser(List<Token> tokens) { // pass in a token list to parse for the constructor
        this.tokens = tokens;
    }

    Expr parse() {
        try {
            return expression(); // tries to parse the expression
        } catch (ParseError error) {
            return null; // throws an error if the expression is broken
        }
    }
    
    private Expr expression() { // based solely on equality, so just calles that method
        return equality();
    }

    private Expr equality() { // defines our equality grammar
        Expr expr = comparison(); // equality takes two comparisons, so we set our expression to a compariosn

        while(match(BANG_EQUAL, EQUAL_EQUAL)) { // iterates over tokens until we get to an equality operator
            Token operator = previous(); // sets operator to the pervious token, as the while loop iterates over it once
            Expr right = comparison(); // gets the right comparison expression
            expr = new Expr.Binary(expr, operator, right); // creates a new binary expression based on the operator (RECURSIVELY)
        }

        return expr; // returns the expression back to expression, which is just returned as the final expression
    }

    private Expr comparison() { // defines out comparison grammar
        Expr expr = term(); // creates a new term object, which will be our leftmost part of the expression

        while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) { // iterates until it finds the correct operator, and then stores it
            Token operator = previous();
            Expr right = term(); // figures out the term on the right
            expr = new Expr.Binary(expr, operator, right); // creates and returns the new binary expression (DOES SO RECURSIVELY FROM ABOVE)
        }

        return expr;
    }

    private Expr term() { // same idea as previous language layer, but creates factor based binary expressions recursively
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() { // same idea as previous language layer, but creates unary based binary expressions recursively
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() { // instead of having two sided expressions, just returns a unary expression, but with a similar mechanism to all of the binary expressions
        if (match(BANG, MINUS)) { 
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() { // checks all of our primary expressions and returns a literal (A LEAF NODE). Also checks for open and closed parenthesis
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if(match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Exprec expression.");
    }
    
    private boolean match(TokenType... types) { // this method checks to see if a token matches a particular token type (sees which part of our heirarchy an expression belongs to)
        for (TokenType type : types) { // iterates over a list of type
            if (check(type)) { // calls the check method to see if they are equal
                advance(); // advances to the next item in the list
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance(); // advances looking for the closing parenthesis

        throw error(peek(), message);

    }

    private boolean check(TokenType type) { // this method checks to see a TokenType is matched
        if (isAtEnd()) return false; 
        return peek().type == type; // checks to see if the current token matches the type we are checking
    }

    private Token advance() {
        if (!isAtEnd())current++;
        return previous(); // returns the previous token to match the while loop iterating over our operator
    }

    private boolean isAtEnd() {
        return peek().type == EOF; // checks to see if we are at the end of an expression
    }

    private Token peek() {
        return tokens.get(current); // returns the current token
    }

    private Token previous() {
        return tokens.get(current - 1); // returns the token before the current token in the list
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() { // discards tokens until it thinks it has found a statement boundary
        advance();

        while(!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

}