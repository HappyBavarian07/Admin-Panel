package de.happybavarian07.adminpanel.language.expressionparser;

import de.happybavarian07.adminpanel.language.expressionparser.interfaces.FunctionCall;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Manages function registration and execution for the expression parser system.
 * <p>
 * This class allows for registering both built-in functions and custom user-defined
 * functions that can be used within expressions. The function manager supports type
 * checking, argument validation, and provides a flexible function definition syntax.
 * </p>
 * <p>
 * Functions can be defined in two ways:
 * <ul>
 *   <li>Programmatically using the registerFunction methods</li>
 *   <li>Using a string-based syntax: "name(param1, param2) => expression"</li>
 * </ul>
 * </p>
 */
public class LanguageFunctionManager {
    private final ExpressionParser expressionParser;

    /**
     * Creates a new LanguageFunctionManager instance.
     *
     * @param expressionParser The expression parser instance to manage functions for
     */
    public LanguageFunctionManager(ExpressionParser expressionParser) {
        this.expressionParser = expressionParser;
        registerDefaultFunctions();
    }

    /**
     * Registers default built-in functions.
     */
    private void registerDefaultFunctions() {
        // Example function: func:hasPermission(player, permission)
        expressionParser.registerFunction("hasPermission", (interp, args, callType) -> {
            if (args.size() != 2) {
                throw new RuntimeException("hasPermission() expects 2 arguments: player and permission");
            }

            Object playerObj = args.get(0);
            Object permissionObj = args.get(1);

            if (!(playerObj instanceof Player player)) {
                throw new RuntimeException("First argument to hasPermission() must be a player");
            }
            if (!(permissionObj instanceof String permission)) {
                throw new RuntimeException("Second argument to hasPermission() must be a string");
            }

            return player.hasPermission(permission);
        }, "boolean");

        // Example function: func:hasItem(player, material, amount)
        expressionParser.registerFunction("hasItem", (interp, args, callType) -> {
            if (args.size() != 2 && args.size() != 3) {
                throw new RuntimeException("hasItem() expects 2 or 3 arguments: player, material, and optional amount");
            }

            Object playerObj = args.get(0);
            Object materialObj = args.get(1);
            int amount = 1;

            if (args.size() == 3) {
                if (!(args.get(2) instanceof Number)) {
                    throw new RuntimeException("Third argument to hasItem() must be a number");
                }
                amount = ((Number) args.get(2)).intValue();
            }

            if (!(playerObj instanceof Player player)) {
                throw new RuntimeException("First argument to hasItem() must be a player");
            }

            Material material;
            if (materialObj instanceof Material) {
                material = (Material) materialObj;
            } else if (materialObj instanceof String) {
                try {
                    material = Material.valueOf(((String) materialObj).toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Invalid material: " + materialObj);
                }
            } else {
                throw new RuntimeException("Second argument to hasItem() must be a material or material name");
            }

            int count = 0;

            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == material) {
                    count += item.getAmount();
                    if (count >= amount) {
                        return true;
                    }
                }
            }

            return count >= amount;
        }, "boolean");

        // Example function: func:random(min, max)
        expressionParser.registerFunction("random", (interp, args, callType) -> {
            if (args.size() != 2) {
                throw new RuntimeException("random() expects 2 arguments: min and max");
            }

            if (!(args.get(0) instanceof Number) || !(args.get(1) instanceof Number)) {
                throw new RuntimeException("Both arguments to random() must be numbers");
            }

            double min = ((Number) args.get(0)).doubleValue();
            double max = ((Number) args.get(1)).doubleValue();

            return min + Math.random() * (max - min);
        });

        // Example function: func:add(a, b)
        registerFunction("add", (interpreter, args, callType) -> {
            if (args.size() != 2) {
                throw new RuntimeException("add() expects 2 arguments");
            }
            if (!(args.get(0) instanceof Number) || !(args.get(1) instanceof Number)) {
                throw new RuntimeException("Both arguments to add() must be numbers");
            }
            if ("int".equalsIgnoreCase(callType)) {
                return ((Number) args.get(0)).intValue() + ((Number) args.get(1)).intValue();
            } else if ("double".equalsIgnoreCase(callType) || callType == null) {
                return ((Number) args.get(0)).doubleValue() + ((Number) args.get(1)).doubleValue();
            }
            throw new RuntimeException("Unsupported add() return type: " + callType);
        }, "double");

        registerFunction("Out", (interpreter, args, callType) -> {
            if (args == null || args.isEmpty()) return null;
            Object value = args.get(0);
            String type = callType != null ? callType : "Object";
            try {
                switch (type.toLowerCase()) {
                    case "int":
                    case "integer":
                        return Integer.parseInt(value.toString());
                    case "double":
                        return Double.parseDouble(value.toString());
                    case "float":
                        return Float.parseFloat(value.toString());
                    case "long":
                        return Long.parseLong(value.toString());
                    case "boolean":
                        return Boolean.parseBoolean(value.toString());
                    case "material":
                        String sanitized = value.toString().replace("'", "").replace("\"", "").trim();
                        Material mat = Material.matchMaterial(sanitized);
                        if (mat != null) return mat;
                        try {
                            return Material.valueOf(sanitized.toUpperCase());
                        } catch (IllegalArgumentException ignored) {
                        }
                        break;
                    default:
                        return value;
                }
            } catch (Exception e) {
                return value;
            }
            return value;
        }, "Object");
    }

    /**
     * Registers a function from a string definition.
     * <p>
     * The function string should follow the format: "name(param1, param2) => expression"
     * </p>
     * <p>
     * Examples:
     * <ul>
     *   <li>"add(a, b) => a + b"</li>
     *   <li>"isEven<number>(n:number) => n % 2 == 0"</li>
     *   <li>"getColor(rank) => if rank == 'admin': '#ff0000' elif rank == 'mod': '#00ff00' else: '#ffffff'"</li>
     * </ul>
     * </p>
     *
     * @param functionString The function definition string
     * @throws IllegalArgumentException If the function string has invalid format
     */
    public void registerFunction(String functionString) {
        String[] parts = functionString.split("=>", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid function format. Expected: 'name(params) => expression'");
        }

        String header = parts[0].trim();
        String body = parts[1].trim();

        // Extract return type if present in angle brackets before '('
        String name;
        String defaultType = null;
        int paramStart = header.indexOf('(');
        int paramEnd = header.lastIndexOf(')');

        if (paramStart == -1 || paramEnd == -1 || paramStart >= paramEnd) {
            throw new IllegalArgumentException("Invalid function parameters. Expected: 'name(param1, param2)'");
        }

        // Find angle brackets before '('
        int angleStart = header.lastIndexOf('<', paramStart);
        int angleEnd = header.indexOf('>', angleStart);
        if (angleStart != -1 && angleEnd != -1 && angleEnd < paramStart) {
            name = header.substring(0, angleStart).trim();
            defaultType = header.substring(angleStart + 1, angleEnd).trim();
        } else {
            name = header.substring(0, paramStart).trim();
        }

        // if number of brackets dont match up, then throw an error
        if (header.chars().filter(ch -> ch == '(').count() != header.chars().filter(ch -> ch == ')').count()) {
            int mismatchIndex = -1;
            int open = 0;
            int close = 0;
            for (int i = 0; i < header.length(); i++) {
                if (header.charAt(i) == '(') {
                    open++;
                } else if (header.charAt(i) == ')') {
                    close++;
                    if (close > open) {
                        mismatchIndex = i;
                        break;
                    }
                }
            }
            if (mismatchIndex == -1) {
                throw new IllegalArgumentException("Mismatched parentheses in function definition: " + header);
            } else {
                throw new IllegalArgumentException("Mismatched parentheses in function definition: " + header + " at position " + mismatchIndex);
            }
        }

        String[] paramDefs = header.substring(paramStart + 1, paramEnd).split(",");
        String[] params = new String[paramDefs.length];
        Class<?>[] paramTypes = new Class<?>[paramDefs.length];

        for (int i = 0; i < paramDefs.length; i++) {
            String param = paramDefs[i].trim();
            int typeSep = param.indexOf(':');
            if (typeSep != -1) {
                params[i] = param.substring(0, typeSep).trim();
                String typeName = param.substring(typeSep + 1).trim();
                paramTypes[i] = parseType(typeName);
            } else {
                params[i] = param;
                paramTypes[i] = Object.class;
            }
        }

        String finalDefaultType = defaultType;
        registerFunction(name, (interpreter, args, callType) -> {
            if (args.size() != params.length) {
                throw new RuntimeException(String.format(
                        "Function %s expects %d arguments, got %d",
                        name, params.length, args.size()));
            }

            for (int i = 0; i < params.length; i++) {
                Object arg = args.get(i);
                Class<?> expectedType = paramTypes[i];
                if (expectedType != Object.class && arg != null && !expectedType.isInstance(arg)) {
                    try {
                        arg = castToType(arg, expectedType);
                    } catch (Exception e) {
                        throw new RuntimeException(String.format(
                                "Argument %d ('%s') to function %s is not of type %s (got %s)",
                                i + 1, params[i], name, expectedType.getSimpleName(), arg.getClass().getSimpleName()), e);
                    }
                }
                interpreter.setVariable(params[i], arg, 1);
            }

            Object result;
            try {
                result = interpreter.interpret(expressionParser.parse(body, Parser.Expression.class));
            } finally {
                for (String param : params) {
                    interpreter.removeVariable(param);
                }
            }

            String typeToCast = callType != null ? callType : finalDefaultType;
            Class<?> targetType = parseType(typeToCast);
            try {
                result = castToType(result, targetType);
            } catch (Exception e) {
                throw new RuntimeException(String.format(
                        "Return value of function %s cannot be cast to type %s: %s",
                        name, typeToCast, e.getMessage()), e);
            }
            return result;
        }, defaultType);
    }

    /**
     * Parses a type name to a Java class.
     * <p>
     * Converts common type names like "int", "string", "boolean" to their
     * corresponding Java class types.
     * </p>
     *
     * @param typeName The name of the type to parse
     * @return The corresponding Java class for the type
     */
    private Class<?> parseType(String typeName) {
        return switch (typeName.toLowerCase()) {
            case "int", "integer", "number" -> Integer.class;
            case "double" -> Double.class;
            case "float" -> Float.class;
            case "long" -> Long.class;
            case "string" -> String.class;
            case "boolean", "bool" -> Boolean.class;
            default -> Object.class;
        };
    }

    /**
     * Casts an object to the given type, if possible.
     * <p>
     * Performs type conversion for common types like numbers and strings.
     * </p>
     *
     * @param value The value to cast
     * @param type The target type class
     * @return The value cast to the target type
     * @throws RuntimeException If the value cannot be cast to the target type
     */
    private Object castToType(Object value, Class<?> type) {
        if (type == Integer.class) {
            if (value instanceof Number) return ((Number) value).intValue();
            return Integer.parseInt(value.toString());
        } else if (type == Double.class) {
            if (value instanceof Number) return ((Number) value).doubleValue();
            return Double.parseDouble(value.toString());
        } else if (type == Float.class) {
            if (value instanceof Number) return ((Number) value).floatValue();
            return Float.parseFloat(value.toString());
        } else if (type == Long.class) {
            if (value instanceof Number) return ((Number) value).longValue();
            return Long.parseLong(value.toString());
        } else if (type == String.class) {
            return value.toString();
        } else if (type == Boolean.class) {
            if (value instanceof Boolean) return value;
            String s = value.toString().toLowerCase();
            if (s.equals("true") || s.equals("1")) return true;
            if (s.equals("false") || s.equals("0")) return false;
            throw new IllegalArgumentException("Cannot cast to boolean: " + value);
        }
        return value;
    }

    /**
     * Registers a function with the expression parser.
     *
     * @param name The name of the function
     * @param function The function implementation
     */
    public void registerFunction(String name, FunctionCall function) {
        registerFunction(name, function, null);
    }

    /**
     * Registers a function with the expression parser and specifies a default return type.
     *
     * @param name The name of the function
     * @param function The function implementation
     * @param defaultType The default return type for the function
     */
    public void registerFunction(String name, FunctionCall function, String defaultType) {
        expressionParser.registerFunction(name, function, defaultType);
    }

    /**
     * Unregisters a function from the expression parser.
     *
     * @param name The name of the function to unregister
     */
    public void unregisterFunction(String name) {
        expressionParser.unregisterFunction(name);
    }

    /**
     * Clears all registered functions except built-in ones.
     */
    public void clearFunctions() {
        expressionParser.clearFunctions();
    }
}
