package de.happybavarian07.adminpanel.language.expressionparser.conditions;

import de.happybavarian07.adminpanel.language.expressionparser.interfaces.AbstractMaterialCondition;
import de.happybavarian07.adminpanel.language.expressionparser.operations.MathOperation;
import org.bukkit.Material;

/*
 * @Author HappyBavarian07
 * @Date Mai 24, 2025 | 15:49
 */
public class MathCondition extends AbstractMaterialCondition {
    private final Number value1;
    private final Number value2;
    private final MathOperation operation;
    private final String comparisonOperator;
    private final Number comparisonValue;

    /**
     * Creates a new math condition.
     *
     * @param value1 the first value for the math operation
     * @param value2 the second value for the math operation
     * @param operation the math operation to perform
     * @param comparisonOperator the comparison operator (">", "<", ">=", "<=", "==", "!=")
     * @param comparisonValue the value to compare the result of the math operation to
     */
    public MathCondition(Number value1, Number value2, MathOperation operation, String comparisonOperator, Number comparisonValue) {
        super("Math");
        this.value1 = value1;
        this.value2 = value2;
        this.operation = operation;
        this.comparisonOperator = comparisonOperator;
        this.comparisonValue = comparisonValue;
    }

    /**
     * Creates a new math condition with materials.
     *
     * @param value1 the first value for the math operation
     * @param value2 the second value for the math operation
     * @param operation the math operation to perform
     * @param comparisonOperator the comparison operator (">", "<", ">=", "<=", "==", "!=")
     * @param comparisonValue the value to compare the result of the math operation to
     * @param trueMaterial the material to return when the condition is true
     * @param falseMaterial the material to return when the condition is false
     * @param defaultMaterial the default material to return if no condition is met
     */
    public MathCondition(Number value1, Number value2, MathOperation operation, String comparisonOperator, Number comparisonValue,
                         Material trueMaterial, Material falseMaterial, Material defaultMaterial) {
        super("Math", trueMaterial, falseMaterial, defaultMaterial);
        this.value1 = value1;
        this.value2 = value2;
        this.operation = operation;
        this.comparisonOperator = comparisonOperator;
        this.comparisonValue = comparisonValue;
    }

    @Override
    public boolean isTrue() {
        if (value1 == null || value2 == null || operation == null || comparisonOperator == null || comparisonValue == null) {
            return false;
        }

        double result = operation.perform(value1.doubleValue(), value2.doubleValue());
        double compareValue = comparisonValue.doubleValue();

        switch (comparisonOperator) {
            case ">":
                return result > compareValue;
            case "<":
                return result < compareValue;
            case ">=":
                return result >= compareValue;
            case "<=":
                return result <= compareValue;
            case "==":
                return result == compareValue;
            case "!=":
                return result != compareValue;
            default:
                throw new IllegalArgumentException("Invalid comparison operator: " + comparisonOperator);
        }
    }

    @Override
    public String getName() {
        return "Math(" + value1 + " " + operation.getName() + " " + value2 + " " + comparisonOperator + " " + comparisonValue + ")";
    }
}