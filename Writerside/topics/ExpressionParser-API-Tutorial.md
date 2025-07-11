# Expression Parser API Tutorial

## Introduction

The Expression Parser is a powerful tool that allows you to evaluate dynamic expressions within your language files.
This enables conditional rendering and runtime computations based on context, making your language files more flexible
and powerful.

## Core Components

The expression parser system consists of several components:

- **Lexer**: Tokenizes the input string into a sequence of tokens
- **Parser**: Builds an abstract syntax tree from tokens
- **Interpreter**: Evaluates the syntax tree to produce a result
- **ExpressionParser**: Combines all of the above for easy use

## Basic Usage

### Creating an ExpressionParser

```java
// Create a new instance
ExpressionParser parser = new ExpressionParser();
```

### Parsing Simple Expressions

```java
// Parse a numeric expression
Double result = parser.parse("2 * (3 + 4)", Double.class);
// result = 14.0

// Parse a string expression
String text = parser.parse("'Hello' + ' ' + 'World'", String.class);
// text = "Hello World"

// Parse a boolean expression
Boolean isTrue = parser.parse("5 > 3 && true", Boolean.class);
// isTrue = true
```

### Using Variables

You can set variables that can be referenced in expressions:

```java
// Set variables
parser.setVariable("playerName", "Steve");
parser.setVariable("level", 10);
parser.setVariable("isAdmin", true);

// Use variables in expressions
String message = parser.parse(
    "'Player ' + playerName + ' is level ' + level", 
    String.class
);
// message = "Player Steve is level 10"

Boolean hasAccess = parser.parse("level > 5 && isAdmin", Boolean.class);
// hasAccess = true
```

### Conditional Expressions

The parser supports if-elif-else conditional chains:

```java
String result = parser.parse(
    "if level > 15: 'Master' " +
    "elif level > 10: 'Expert' " +
    "elif level > 5: 'Intermediate' " +
    "else: 'Beginner'",
    String.class
);
// result = "Intermediate" (assuming level = 10)
```

### Material Conditions

For Bukkit/Spigot development, the parser supports working with Material objects:

```java
// Parse a direct material reference
Material material = parser.parse("DIAMOND_BLOCK", Material.class);

// Parse a conditional material expression
MaterialCondition condition = parser.parse(
    "if player.hasPermission('admin'): DIAMOND_BLOCK " +
    "elif player.hasPermission('mod'): GOLD_BLOCK " +
    "else: STONE",
    Material.STONE,     // default material
    Material.GREEN_WOOL, // true material (for boolean conditions)
    Material.RED_WOOL    // false material (for boolean conditions)
);

// Get the resolved material
Material result = condition.getMaterial();
```

## Custom Functions

### Registering Functions

You can register custom functions that can be called within expressions:

```java
// Register a simple function
parser.registerFunction("double", (interpreter, args, type) -> {
    if (args.isEmpty()) return 0.0;
    Object arg = args.get(0);
    if (arg instanceof Number) {
        return ((Number) arg).doubleValue() * 2;
    }
    return arg;
});

// Use the function in an expression
Double doubled = parser.parse("double(5)", Double.class);
// doubled = 10.0
```

### Generic Functions with Type Parameters

You can create functions that accept type parameters:

```java
// Register a function with a default type
parser.registerFunction("Out", (interpreter, args, type) -> {
    if (args.isEmpty()) return null;
    Object arg = args.get(0);
    
    // Handle type conversion based on the type parameter
    if ("Material".equals(type) && arg instanceof String) {
        try {
            return Material.valueOf(((String) arg).toUpperCase());
        } catch (IllegalArgumentException e) {
            return Material.STONE;
        }
    }
    
    return arg;
}, "String"); // Default type is String

// Call the function with explicit type parameter
Material material = parser.parse("Out<Material>(DIAMOND_BLOCK)", Material.class);

// Call the function without type parameter (uses default)
String text = parser.parse("Out('Hello')", String.class);
```

## Advanced Usage

### Ternary Operator

The parser supports the ternary conditional operator:

```java
String result = parser.parse("isAdmin ? 'Admin Panel' : 'User Panel'", String.class);
```

### Boolean Logic

```java
Boolean result = parser.parse("(level > 5 && isAdmin) || specialAccess", Boolean.class);
```

### Mathematical Operations

```java
Double result = parser.parse("2 * (power(2, 3) + 4) / 2", Double.class);
// Assuming power() is a registered function for exponentiation
```

## Accessing Internal Components

If needed, you can access the internal components:

```java
// Get the lexer
Lexer lexer = parser.getLexer();

// Get the parser
Parser syntaxParser = parser.getParser();

// Get the interpreter
Interpreter interpreter = parser.getInterpreter();

// Get the function manager
LanguageFunctionManager functionManager = parser.getFunctionManager();
```

## Error Handling

The parser throws `IllegalArgumentException` for most errors:

```java
try {
    parser.parse("invalid + expression", String.class);
} catch (IllegalArgumentException e) {
    // Handle the error
    System.err.println("Error parsing expression: " + e.getMessage());
}
```

## Performance Considerations

- **Variable Reuse**: Set variables once and reuse them in multiple expressions
- **Function Optimization**: Implement functions efficiently, especially if called frequently
- **Caching**: Consider caching parse results for frequently used expressions

## Integration with LanguageManager

The ExpressionParser is integrated with the LanguageManager system, allowing dynamic content in language files. See the
LanguageManager tutorial for details on how to use expressions within your language configuration files.
