package lox;

class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    // Token constructor
    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    // this is useful for dealing with errors later
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}