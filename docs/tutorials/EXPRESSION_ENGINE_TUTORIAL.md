# Expression Engine Tutorial

## Overview
The Expression Engine is a modern, extensible parser and evaluator for expressions in language files or plugin logic. It supports math, logic, function calls, variable references, conditional chains, short-circuiting, type inference, context injection, and security features like whitelisting/blacklisting.

---

## Core Concepts
- **Lexer/Parser/Interpreter:** Expressions are tokenized, parsed into ASTs, and then evaluated.
- **Functions:** Register custom or built-in functions with type and argument checks.
- **Out Function:** All output from expressions is handled and formatted by the built-in `Out` function: `Out<type>(output)`.
- **Variables:** Set, get, and remove variables for use in expressions.
- **Type Inference:** Arguments and return values are automatically converted when possible.
- **Conditional Chains:** Support for `if ... elif ... else ...` logic in expressions.
- **Material/Head Parsing:** Special parsing for Bukkit `Material`, custom heads, and head textures.
- **Formatting:** Pretty-print or minify expressions for logs or UI.
- **Security:** Whitelist/blacklist for functions and variables.

---

## Basic Usage

### 1. Creating an Engine
```java
ExpressionEngine engine = new ExpressionEngine();
```

### 2. Evaluating Expressions
```java
Object result = engine.parsePrimitive("Out<double>(2 + 3 * 4)"); // 14.0
Double value = engine.parse("Out<double>(2 + 3 * 4)", Double.class); // 14.0
```

### 3. Variables
```java
engine.setVariable("x", 10);
Object result = engine.parsePrimitive("Out<double>(x * 2)"); // 20.0
engine.removeVariable("x");
```

### 4. Functions
```java
engine.registerFunction("square", (i, args, t) -> {
    double v = ((Number) args.get(0)).doubleValue();
    return Out<double>(v * v);
}, "double", new Class<?>[]{Double.class}, Double.class);
Object result = engine.parsePrimitive("Out<double>(square(5))"); // 25.0
engine.unregisterFunction("square");
```

---

## Advanced Features

### Conditional Chains
```java
String expr = "if x > 10: Out<string>('big') elif x > 5: Out<string>('medium') else: Out<string>('small')";
engine.setVariable("x", 8);
String result = engine.parse(expr, String.class); // "medium"
```

### Out Function (Output Handling)
All evaluated output is wrapped using the `Out` function, which is registered by default. The syntax is:
```java
Out<type>(output)
```
- `type` can be `string`, `double`, `boolean`, `Material`, etc.
- This ensures type safety and consistent formatting of results.
- Example:
```java
String expr = "Out<double>(2.5 * 2)";
Double result = engine.parse(expr, Double.class); // 5.0
```

### Material/Head Parsing
```java
MaterialCondition cond = engine.parse("Out<Material>(DIAMOND_BLOCK)", Material.BARRIER);
MaterialCondition head = engine.parse("Out<Material>(HEAD('PlayerName'))", Material.BARRIER);
```

### Type Inference & Auto conversion
```java
engine.registerFunction("boolTest", (i, a, t) -> Out<boolean>(a.get(0)), "boolean", new Class<?>[]{Boolean.class}, Boolean.class);
Object b = engine.parsePrimitive("Out<boolean>(boolTest('true'))"); // true
```

### Formatting Expressions
```java
String pretty = engine.formatExpression("Out<double>(a+b*2)", true);
String compact = engine.formatExpression("Out<double>(a + b * 2)", false);
```

### Security/Sandboxing
```java
engine.setFunctionWhitelist(List.of("square", "lang"));
engine.setVariableBlacklist(List.of("internalSecret"));
```

### Localization
If you want to use localization keys:
```java
engine.registerLangFunction(langManager); // Registers lang(key) function
String localized = (String) engine.parsePrimitive("Out<string>(lang('welcome_message'))");
```

---

## Best Practices
- Always register argument and return types for custom functions.
- Use whitelists/blacklists in untrusted environments.
- Use debug mode (if available) for troubleshooting.
- Use pretty/minified formatting for logs or UI.
- Always wrap expression output with `Out<type>(...)` for type safety and clarity.

---

## Example JUnit Test
Create a test class (e.g., `ExpressionEngineTest.java`) in your `test` source folder:
```java
import de.happybavarian07.coolstufflib.languagemanager.expressionengine.ExpressionEngine;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ExpressionEngineTest {
    @Test
    void testBasicMath() {
        ExpressionEngine engine = new ExpressionEngine();
        assertEquals(7.0, engine.parse("Out<double>(1 + 2 * 3)", Double.class));
    }

    @Test
    void testVariables() {
        ExpressionEngine engine = new ExpressionEngine();
        engine.setVariable("x", 4);
        assertEquals(16.0, engine.parse("Out<double>(x * x)", Double.class));
    }

    @Test
    void testFunction() {
        ExpressionEngine engine = new ExpressionEngine();
        engine.registerFunction("double", (i, args, t) -> Out<double>(((Number) args.get(0)).doubleValue() * 2),
            "double", new Class<?>[]{Double.class}, Double.class);
        assertEquals(10.0, engine.parse("Out<double>(double(5))", Double.class));
    }

    @Test
    void testConditionalChain() {
        ExpressionEngine engine = new ExpressionEngine();
        engine.setVariable("x", 9);
        String expr = "if x > 10: Out<string>('big') elif x > 5: Out<string>('medium') else: Out<string>('small')";
        assertEquals("medium", engine.parse(expr, String.class));
    }

    @Test
    void testMaterialParsing() {
        ExpressionEngine engine = new ExpressionEngine();
        assertNotNull(engine.parse("Out<Material>(DIAMOND_BLOCK)", org.bukkit.Material.class));
    }

    @Test
    void testTypeInference() {
        ExpressionEngine engine = new ExpressionEngine();
        engine.registerFunction("boolTest", (i, a, t) -> Out<boolean>(a.get(0)), "boolean", new Class<?>[]{Boolean.class}, Boolean.class);
        assertEquals(true, engine.parse("Out<boolean>(boolTest('true'))", Boolean.class));
    }
}
```

---

## References
- `ExpressionEngine.java`
- `Interpreter.java`
- `Parser.java`
- `Lexer.java`
- `LanguageFunctionManager.java`

For integration with the Language Manager, see `LANGUAGE_MANAGER_TUTORIAL.md`.
