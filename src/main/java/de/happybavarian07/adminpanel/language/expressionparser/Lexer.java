package de.happybavarian07.adminpanel.language.expressionparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts source text into a sequence of tokens for parsing.
 * <p>
 * The lexer breaks down expression strings into tokens that represent different
 * components of the expression (operators, identifiers, literals, etc.).
 * It handles string literals, numbers, identifiers, and operators according to
 * the expression language syntax.
 * </p>
 */
public class Lexer {
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", TokenType.AND);
        keywords.put("or", TokenType.OR);
        keywords.put("not", TokenType.NOT);
        keywords.put("if", TokenType.IF);
        keywords.put("elif", TokenType.ELIF);
        keywords.put("else", TokenType.ELSE);
    }

    private String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    /**
     * Creates a new lexer with the given source text.
     *
     * @param source The source text to tokenize
     */
    public Lexer(String source) {
        this.source = source != null ? source : "";
    }

    /**
     * Sets new source text and resets the lexer state.
     *
     * @param source The new source text to tokenize
     */
    public void setSource(String source) {
        this.source = source != null ? source : "";
        this.tokens.clear();
        this.start = 0;
        this.current = 0;
        this.line = 1;
    }

    /**
     * Scans the source text and returns a list of tokens.
     * <p>
     * This method tokenizes the entire source string, breaking it down into
     * individual tokens based on the language syntax. The resulting token list
     * can then be passed to a parser for further processing.
     * </p>
     *
     * @return A list of tokens representing the source text
     * @throws RuntimeException If the source contains invalid or unexpected characters
     */
    public List<Token> scanTokens() {
        tokens.clear();
        current = 0;
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, current));

        return tokens;
    }

    /**
     * Scans the current character and adds the appropriate token.
     * <p>
     * This method processes a single character from the source text and
     * determines what kind of token it represents, handling special cases
     * like operators, delimiters, strings, and identifiers.
     * </p>
     */
    private void scanToken() {
        char c = advance();
        switch (c) {
            case '\n':
                addToken(TokenType.NEWLINE);
                break;
            case '\r':
                if (match('\n')) addToken(TokenType.NEWLINE);
                else addToken(TokenType.NEWLINE);
                break;
            case '(':
                addToken(TokenType.LPAREN);
                break;
            case ')':
                addToken(TokenType.RPAREN);
                break;
            case '?':
                addToken(TokenType.QUESTION);
                break;
            case ':':
                addToken(TokenType.COLON);
                break;
            case '!':
                addToken(match('=') ? TokenType.NOT_EQUAL : TokenType.NOT);
                break;
            case '=':
                addToken(TokenType.EQUAL);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '&':
                if (match('&')) addToken(TokenType.AND);
                break;
            case '|':
                if (match('|')) addToken(TokenType.OR);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '*':
                addToken(TokenType.MULTIPLY);
                break;
            case '/':
                addToken(TokenType.DIVIDE);
                break;
            case '%':
                addToken(TokenType.MODULO);
                break;
            case '^':
                addToken(TokenType.POWER);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case ' ':
            case '\t':
                break;
            case '"':
            case '\'':
                string(c);
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    throw new RuntimeException("Unexpected character: " + c);
                }
                break;
        }
    }

    /**
     * Processes an identifier token starting at the current position.
     * <p>
     * Identifiers are sequences of alphanumeric characters that represent
     * variable names, function names, or keywords in the language.
     * </p>
     */
    private void identifier() {
        int start = current - 1;
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text.toLowerCase());
        if (type != null) {
            addToken(type);
            return;
        }
        addToken(TokenType.IDENTIFIER, text);
    }

    /**
     * Processes a numeric literal starting at the current position.
     * <p>
     * Handles both integer and floating-point numbers, converting them
     * to the appropriate Java numeric type.
     * </p>
     */
    private void number() {
        boolean isFloat = false;
        while (isDigit(peek())) advance();
        if (peek() == '.' && isDigit(peekNext())) {
            isFloat = true;
            advance();
            while (isDigit(peek())) advance();
        }
        String numStr = source.substring(start, current);
        if (isFloat) {
            addToken(TokenType.NUMBER, Double.parseDouble(numStr));
        } else {
            // Try to parse as Integer, then Long if too large
            try {
                int intValue = Integer.parseInt(numStr);
                addToken(TokenType.NUMBER, intValue);
            } catch (NumberFormatException e) {
                long longValue = Long.parseLong(numStr);
                addToken(TokenType.NUMBER, longValue);
            }
        }
    }

    /**
     * Processes a string literal starting at the current position.
     * <p>
     * Handles both single and double quoted strings, including escape sequences.
     * </p>
     *
     * @param delimiter The quote character (single or double quote) that started the string
     */
    private void string(char delimiter) {
        StringBuilder value = new StringBuilder();
        advance(); // Consume the opening quote

        while (!isAtEnd() && peek() != delimiter) {
            if (peek() == '\\') {
                advance();
                if (isAtEnd()) break;
                switch (peek()) {
                    case 'n':
                        value.append('\n');
                        break;
                    case 't':
                        value.append('\t');
                        break;
                    case 'r':
                        value.append('\r');
                        break;
                    case '\'':
                        value.append('\'');
                        break;
                    case '"':
                        value.append('"');
                        break;
                    case '\\':
                        value.append('\\');
                        break;
                    default:
                        value.append(peek());
                }
            } else {
                if (peek() == '\n') line++;
                value.append(peek());
            }
            advance();
        }

        if (isAtEnd()) {
            throw new RuntimeException("Unterminated string.");
        }

        advance(); // Consume the closing quote
        addToken(TokenType.STRING, value.toString());
    }

    /**
     * Checks if the current position is at the end of the source text.
     *
     * @return true if at end of source, false otherwise
     */
    private boolean isAtEnd() {
        return current >= source.length();
    }

    /**
     * Consumes the next character in the source and returns it.
     *
     * @return The consumed character
     */
    private char advance() {
        return source.charAt(current++);
    }

    /**
     * Adds a token of the specified type with no literal value.
     *
     * @param type The token type
     */
    private void addToken(TokenType type) {
        addToken(type, source.substring(start, current));
    }

    /**
     * Adds a token of the specified type with a literal value.
     *
     * @param type The token type
     * @param literal The literal value associated with the token
     */
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    /**
     * Checks if the next character matches the expected character.
     * <p>
     * If it matches, the character is consumed; otherwise, no action is taken.
     * </p>
     *
     * @param expected The character to match against
     * @return true if the character matched and was consumed, false otherwise
     */
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    /**
     * Returns the current character without consuming it.
     *
     * @return The current character, or '\0' if at end of source
     */
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    /**
     * Returns the next character without consuming it.
     *
     * @return The next character, or '\0' if at end of source
     */
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    /**
     * Checks if a character is a digit (0-9).
     *
     * @param c The character to check
     * @return true if the character is a digit, false otherwise
     */
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Checks if a character is alphabetic (a-z, A-Z) or underscore.
     *
     * @param c The character to check
     * @return true if the character is alphabetic or underscore, false otherwise
     */
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    /**
     * Checks if a character is alphanumeric (a-z, A-Z, 0-9) or underscore.
     *
     * @param c The character to check
     * @return true if the character is alphanumeric or underscore, false otherwise
     */
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}
