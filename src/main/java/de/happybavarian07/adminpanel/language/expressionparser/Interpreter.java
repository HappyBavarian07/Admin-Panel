package de.happybavarian07.adminpanel.language.expressionparser;

import de.happybavarian07.adminpanel.language.expressionparser.interfaces.FunctionCall;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The interpreter evaluates parsed expressions and computes their values.
 * <p>
 * This class implements the Visitor pattern to traverse the expression tree produced by
 * the {@link Parser} and computes a result value. It supports mathematical operations,
 * logical operations, variable references, and function calls.
 * </p>
 * <p>
 * The interpreter maintains internal state in the form of variables and registered functions
 * that can be referenced during expression evaluation.
 * </p>
 */
public class Interpreter implements Parser.Expression.Visitor<Object> {
    private final Map<String, VariableWithUses> variables = new HashMap<>();
    private final Map<String, RegisteredFunction> functions = new HashMap<>();

    /**
     * Evaluates the given expression and returns its result.
     * <p>
     * This is the main entry point for expression evaluation. It handles any runtime errors
     * that may occur during evaluation and wraps them with additional context.
     * </p>
     *
     * @param expression The expression to evaluate
     * @return The result of evaluating the expression
     * @throws RuntimeException if an error occurs during evaluation
     */
    public Object interpret(Parser.Expression expression) {
        try {
            return evaluate(expression);
        } catch (RuntimeException error) {
            throw new RuntimeException("Runtime error: " + error.getMessage(), error);
        }
    }

    @Override
    public Object visitBinaryExpr(Parser.Expression.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type()) {
            case PLUS:
                if (left instanceof Number && right instanceof Number) {
                    return ((Number) left).doubleValue() + ((Number) right).doubleValue();
                }
                if (left instanceof String || right instanceof String) {
                    return stringify(left) + stringify(right);
                }
                throw new RuntimeException("Operands must be two numbers or at least one string.");
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return ((Number) left).doubleValue() - ((Number) right).doubleValue();
            case MULTIPLY:
                checkNumberOperands(expr.operator, left, right);
                return ((Number) left).doubleValue() * ((Number) right).doubleValue();
            case DIVIDE:
                checkNumberOperands(expr.operator, left, right);
                if (((Number) right).doubleValue() == 0) throw new RuntimeException("Division by zero.");
                return ((Number) left).doubleValue() / ((Number) right).doubleValue();
            case MODULO:
                checkNumberOperands(expr.operator, left, right);
                return ((Number) left).doubleValue() % ((Number) right).doubleValue();
            case POWER:
                checkNumberOperands(expr.operator, left, right);
                return Math.pow(((Number) left).doubleValue(), ((Number) right).doubleValue());
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return ((Number) left).doubleValue() > ((Number) right).doubleValue();
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return ((Number) left).doubleValue() >= ((Number) right).doubleValue();
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return ((Number) left).doubleValue() < ((Number) right).doubleValue();
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return ((Number) left).doubleValue() <= ((Number) right).doubleValue();
            case EQUAL:
                return isEqual(left, right);
            case NOT_EQUAL:
                return !isEqual(left, right);
        }
        return null;
    }

    @Override
    public Object visitLogicalExpr(Parser.Expression.Logical expr) {
        Object left = evaluate(expr.left);
        if (expr.operator.type() == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else if (expr.operator.type() == TokenType.AND) {
            if (!isTruthy(left)) return left;
        }
        return evaluate(expr.right);
    }

    @Override
    public Object visitUnaryExpr(Parser.Expression.Unary expr) {
        Object right = evaluate(expr.right);
        return switch (expr.operator.type()) {
            case MINUS -> {
                checkNumberOperand(expr.operator, right);
                yield -(double) right;
            }
            case NOT -> !isTruthy(right);
            default -> null;
        };
    }

    @Override
    public Object visitLiteralExpr(Parser.Expression.Literal expr) {
        if (expr.value instanceof String str) {
            // Check if it's a variable first
            if (variables.containsKey(str)) {
                return variables.get(str).value;
            }

            // Try to parse as a number
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                // Not a number, continue
            }

            // Try to parse as a boolean
            if (str.equalsIgnoreCase("true")) return true;
            if (str.equalsIgnoreCase("false")) return false;

            // Then try to parse as a material
            try {
                return Material.valueOf(str.toUpperCase().replace("'", "").replace("\"", ""));
            } catch (IllegalArgumentException e) {
                return str;
            }
        }
        return expr.value;
    }

    @Override
    public Object visitVariableExpr(Parser.Expression.Variable expr) {
        String name = expr.name.lexeme();
        if (!variables.containsKey(name)) {
            // Try to interpret as a material name
            try {
                return Material.valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Undefined variable: " + name);
            }
        }
        return variables.get(name).value;
    }

    @Override
    public Object visitGroupingExpr(Parser.Expression.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitTernaryExpr(Parser.Expression.Ternary expr) {
        Object condition = evaluate(expr.condition);
        if (isTruthy(condition)) {
            return evaluate(expr.trueExpression);
        } else {
            return evaluate(expr.falseExpression);
        }
    }

    @Override
    public Object visitConditionalChainExpr(Parser.Expression.ConditionalChain expr) {
        for (Parser.Expression.ConditionalBranch branch : expr.branches) {
            Object condResult = evaluate(branch.condition);
            if (condResult instanceof Boolean && (Boolean) condResult) {
                return evaluate(branch.output);
            }
        }
        if (expr.elseBranch != null) {
            return evaluate(expr.elseBranch);
        }
        return null;
    }

    @Override
    public Object visitConditionalBranchExpr(Parser.Expression.ConditionalBranch expr) {
        // Not used directly; handled in visitConditionalChainExpr
        return null;
    }

    private Object evaluate(Parser.Expression expression) {
        if (expression == null) {
            throw new RuntimeException("Cannot evaluate null expression");
        }
        try {
            return expression.accept(this);
        } catch (Exception e) {
            throw new RuntimeException("Error evaluating expression: " + e.getMessage(), e);
        }
    }

    @Override
    public Object visitCallExpr(Parser.Expression.Call expr) {
        String fullFunctionName = expr.name.lexeme();
        String functionName = fullFunctionName;
        String callType = null;

        // Extract the base function name and type parameter
        int lt = fullFunctionName.indexOf('<');
        int gt = fullFunctionName.indexOf('>');
        if (lt != -1 && gt != -1 && gt > lt) {
            functionName = fullFunctionName.substring(0, lt);
            callType = fullFunctionName.substring(lt + 1, gt);
        }

        RegisteredFunction reg = functions.get(functionName);
        if (reg == null) {
            throw new RuntimeException("Undefined function: " + fullFunctionName);
        }
        List<Object> arguments = new ArrayList<>();
        // Only evaluate arguments, do NOT cast them to the callType/defaultType here
        for (Parser.Expression argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        // If no type was found in the function name, use the default type
        if (callType == null && reg.defaultType != null) {
            callType = reg.defaultType;
        }

        try {
            // Only the function itself should handle casting the result to callType
            return reg.function.call(this, arguments, callType);
        } catch (Exception e) {
            throw new RuntimeException("Error calling function " + fullFunctionName + ": " + e.getMessage(), e);
        }
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        if (object instanceof Number) return ((Number) object).doubleValue() != 0;
        if (object instanceof String) return !((String) object).isEmpty();
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        if (a instanceof Number && b instanceof Number) {
            return ((Number) a).doubleValue() == ((Number) b).doubleValue();
        }

        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Number) return;
        throw new RuntimeException("Invalid operand for operator " + operator.lexeme() + ": " + operand + " (must be a number)");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        checkNumberOperand(operator, left);
        checkNumberOperand(operator, right);
    }

    private String stringify(Object object) {
        if (object == null) return "null";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    public void setVariable(String name, Object value) {
        setVariable(name, value, -1);
    }

    public void setVariable(String name, Object value, int uses) {
        variables.put(name, new VariableWithUses(value, uses));
    }

    public void setVariable(String name, Object value, int uses, boolean overwrite) {
        if (overwrite || !variables.containsKey(name)) {
            variables.put(name, new VariableWithUses(value, uses));
        }
    }

    public boolean hasVariable(String name, boolean checkUses) {
        VariableWithUses var = variables.get(name);
        if (var == null) return false;
        return !checkUses || var.remainingUses > 0;
    }

    public void removeVariable(String name) {
        variables.remove(name);
    }

    /**
     * Retrieves a variable by name and decrements its remaining uses.
     * If the variable has no remaining uses, it is removed from the map.
     *
     * @param name The name of the variable to retrieve.
     * @return The value of the variable, or null if it does not exist or has no remaining uses.
     */
    public Object getVariable(String name) {
        VariableWithUses var = variables.get(name);
        if (var == null) return null;

        Object value = var.getValue();
        if (var.remainingUses == 0) {
            variables.remove(name);
        }
        return value;
    }

    /**
     * Peeks at a variable's value without decrementing its uses.
     * This is useful for checking the value without modifying its state.
     *
     * @param name The name of the variable to peek at.
     * @return The value of the variable, or null if it does not exist.
     */
    public Object peekVariable(String name) {
        VariableWithUses var = variables.get(name);
        if (var == null) return null;
        return var.value;
    }

    public void clearVariables() {
        variables.clear();
    }

    public boolean hasVariable(String name) {
        return variables.containsKey(name);
    }

    public void registerFunction(String name, FunctionCall function) {
        registerFunction(name, function, null);
    }

    public void registerFunction(String name, FunctionCall function, String defaultType) {
        if (functions.containsKey(name))
            return;
        functions.put(name, new RegisteredFunction(function, defaultType));
    }

    public void unregisterFunction(String name) {
        functions.remove(name);
    }

    public void clearFunctions() {
        functions.clear();
    }

    private static class VariableWithUses {
        final Object value;
        int remainingUses;

        VariableWithUses(Object value, int uses) {
            this.value = value;
            this.remainingUses = uses;
        }

        Object getValue() {
            if (remainingUses < 0) {
                return value; // Unlimited uses
            }
            if (remainingUses > 0) {
                remainingUses--;
                return value;
            }
            return null;
        }
    }

    private record RegisteredFunction(FunctionCall function, String defaultType) {
    }
}
