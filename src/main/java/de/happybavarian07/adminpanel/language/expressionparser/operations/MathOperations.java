package de.happybavarian07.adminpanel.language.expressionparser.operations;

import java.util.HashMap;
import java.util.Map;

/*
 * @Author HappyBavarian07
 * @Date Mai 24, 2025 | 15:49
 */
public class MathOperations {
    private static final Map<String, MathOperation> operations = new HashMap<>();

    static {
        registerOperation(new AdditionOperation());
        registerOperation(new SubtractionOperation());
        registerOperation(new MultiplicationOperation());
        registerOperation(new DivisionOperation());
        registerOperation(new ModuloOperation());
        registerOperation(new PowerOperation());
    }

    /**
     * Registers a math operation.
     *
     * @param operation the operation to register
     */
    public static void registerOperation(MathOperation operation) {
        operations.put(operation.getName().toLowerCase(), operation);
    }

    /**
     * Gets a math operation by name.
     *
     * @param name the name of the operation
     * @return the operation, or null if not found
     */
    public static MathOperation getOperation(String name) {
        return operations.get(name.toLowerCase());
    }

    /**
     * Gets all registered operations.
     *
     * @return a map of all operations
     */
    public static Map<String, MathOperation> getOperations() {
        return new HashMap<>(operations);
    }

    /**
     * Performs a math operation.
     *
     * @param operationName the name of the operation
     * @param value1 the first value
     * @param value2 the second value
     * @return the result of the operation
     * @throws IllegalArgumentException if the operation is not found
     */
    public static double performOperation(String operationName, double value1, double value2) {
        MathOperation operation = getOperation(operationName);
        if (operation == null) {
            throw new IllegalArgumentException("Operation not found: " + operationName);
        }
        return operation.perform(value1, value2);
    }
}