package de.happybavarian07.adminpanel.language.expressionparser;

public enum TokenType {
    IDENTIFIER, NUMBER, STRING,
    PLUS, MINUS, MULTIPLY, DIVIDE, MODULO, POWER,
    EQUAL, NOT_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,
    AND, OR, NOT,
    QUESTION, COLON, COMMA,
    LPAREN, RPAREN,
    IF, ELIF, ELSE,
    NEWLINE,
    EOF
}
