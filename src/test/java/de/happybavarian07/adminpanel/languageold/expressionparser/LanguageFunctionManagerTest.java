package de.happybavarian07.adminpanel.languageold.expressionparser;

import de.happybavarian07.coolstufflib.languagemanager.expressionengine.ExpressionEngine;
import de.happybavarian07.coolstufflib.languagemanager.expressionengine.LanguageFunctionManager;
import de.happybavarian07.coolstufflib.languagemanager.expressionengine.interfaces.MaterialCondition;
import org.bukkit.Material;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LanguageFunctionManagerTest {
    private ExpressionEngine parser;
    private LanguageFunctionManager functionManager;

    @BeforeEach
    void setUp() {
        parser = new ExpressionEngine();
        functionManager = parser.getFunctionManager();
    }

    @Test
    void testRegisterAndEvaluateSimpleFunction() {
        functionManager.registerFunction("add", (interpreter, args, type) -> {
            if (args == null || args.size() != 2) throw new RuntimeException("Argument count mismatch");
            Object a = args.get(0);
            Object b = args.get(1);
            if (!(a instanceof Number) || !(b instanceof Number)) throw new RuntimeException("Argument type mismatch");
            return ((Number) a).doubleValue() + ((Number) b).doubleValue();
        });

        Object result = parser.evaluate("add(2, 3)");
        Assertions.assertInstanceOf(Number.class, result);
        Assertions.assertEquals(5.0, ((Number) result).doubleValue());

        functionManager.unregisterFunction("add");

        functionManager.registerFunction("add", (interpreter, args, type) -> {
            if (args == null || args.size() != 2) throw new RuntimeException("Argument count mismatch");
            Object a = args.get(0);
            Object b = args.get(1);
            if (!(a instanceof Number) || !(b instanceof Number)) throw new RuntimeException("Argument type mismatch");
            return ((Number) a).doubleValue() + ((Number) b).doubleValue();
        });

        result = parser.evaluate("add(5, 3)");
        Assertions.assertInstanceOf(Number.class, result);
        Assertions.assertEquals(8.0, ((Number) result).doubleValue());
    }

    @Test
    void testInvalidFunctionFormatMissingArrow() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            functionManager.registerFunction("add(a, b) a + b");
        });
    }

    @Test
    void testInvalidFunctionFormatMismatchedParentheses() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            functionManager.registerFunction("add(a, b => a + b");
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            functionManager.registerFunction("add(a, b)) => a + b");
        });
    }

    @Test
    void testArgumentCountMismatch() {
        functionManager.registerFunction("add", (interpreter, args, type) -> {
            if (args == null || args.size() != 2) throw new RuntimeException("Argument count mismatch");
            Object a = args.get(0);
            Object b = args.get(1);
            if (!(a instanceof Number) || !(b instanceof Number)) throw new RuntimeException("Argument type mismatch");
            return ((Number) a).doubleValue() + ((Number) b).doubleValue();
        });
        Assertions.assertThrows(RuntimeException.class, () -> parser.evaluate("add(1)"));
    }

    @Test
    void testArgumentTypeMismatch() {
        functionManager.registerFunction("add", (interpreter, args, type) -> {
            if (args == null || args.size() != 2) throw new RuntimeException("Argument count mismatch");
            Object a = args.get(0);
            Object b = args.get(1);
            if (!(a instanceof Number) || !(b instanceof Number)) throw new RuntimeException("Argument type mismatch");
            return ((Number) a).doubleValue() + ((Number) b).doubleValue();
        });
        Assertions.assertThrows(RuntimeException.class, () -> parser.evaluate("add(1, 'str')"));
    }

    @Test
    void testIfElifElseMaterialCondition() {
        functionManager.registerFunction("Out", (interpreter, args, type) -> {
            if (args == null || args.isEmpty()) return null;
            Object arg = args.get(0);
            if ("Material".equals(type) && arg instanceof String) {
                try {
                    return Material.valueOf(((String) arg).toUpperCase());
                } catch (IllegalArgumentException e) {
                    return Material.BARRIER;
                }
            }
            return arg;
        }, "String");

        parser.getInterpreter().setVariable("x", 4);
        String expr = "if x > 3: Out<Material>(STONE) elif x == 2: Out<Material>(GLASS) elif x < 0: Out<Material>(DIRT) else: Out<Material>(BARRIER)";
        MaterialCondition cond = Assertions.assertDoesNotThrow(() -> parser.parse(expr, Material.BARRIER));
        Assertions.assertEquals(Material.STONE, cond.getMaterial());

        parser.getInterpreter().setVariable("x", 2);
        cond = Assertions.assertDoesNotThrow(() -> parser.parse(expr, Material.BARRIER));
        Assertions.assertEquals(Material.GLASS, cond.getMaterial());

        parser.getInterpreter().setVariable("x", -5);
        cond = Assertions.assertDoesNotThrow(() -> parser.parse(expr, Material.BARRIER));
        Assertions.assertEquals(Material.DIRT, cond.getMaterial());

        parser.getInterpreter().setVariable("x", 0);
        cond = Assertions.assertDoesNotThrow(() -> parser.parse(expr, Material.BARRIER));
        Assertions.assertEquals(Material.BARRIER, cond.getMaterial());
    }
}
