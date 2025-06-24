package de.happybavarian07.adminpanel.language.expressionparser;

import de.happybavarian07.adminpanel.language.expressionparser.conditions.*;
import de.happybavarian07.adminpanel.language.expressionparser.interfaces.Condition;
import de.happybavarian07.adminpanel.language.expressionparser.interfaces.MaterialCondition;
import de.happybavarian07.adminpanel.language.expressionparser.operations.MathOperation;
import de.happybavarian07.adminpanel.language.expressionparser.operations.MathOperations;
import org.bukkit.Material;

/*
 * @Author HappyBavarian07
 * @Date Mai 24, 2025 | 15:49
 */
public class ConditionBuilder {
    private MaterialCondition condition;

    /**
     * Creates a new condition builder.
     */
    public ConditionBuilder() {
    }

    /**
     * Creates a new condition builder with an initial condition.
     *
     * @param condition the initial condition
     */
    public ConditionBuilder(MaterialCondition condition) {
        this.condition = condition;
    }

    /**
     * Creates an equal condition.
     *
     * @param value1 the first value
     * @param value2 the second value
     * @return this builder
     */
    public ConditionBuilder equal(Object value1, Object value2) {
        condition = new EqualCondition(value1, value2);
        return this;
    }

    /**
     * Creates a not equal condition.
     *
     * @param value1 the first value
     * @param value2 the second value
     * @return this builder
     */
    public ConditionBuilder notEqual(Object value1, Object value2) {
        condition = new NotEqualCondition(value1, value2);
        return this;
    }

    /**
     * Creates a greater than condition.
     *
     * @param value1 the first value
     * @param value2 the second value
     * @return this builder
     */
    public ConditionBuilder greaterThan(Object value1, Object value2) {
        condition = new GreaterThanCondition(value1, value2);
        return this;
    }

    /**
     * Creates a less than condition.
     *
     * @param value1 the first value
     * @param value2 the second value
     * @return this builder
     */
    public ConditionBuilder lessThan(Object value1, Object value2) {
        condition = new LessThanCondition(value1, value2);
        return this;
    }

    /**
     * Creates a greater than or equal condition.
     *
     * @param value1 the first value
     * @param value2 the second value
     * @return this builder
     */
    public ConditionBuilder greaterThanOrEqual(Object value1, Object value2) {
        condition = new GreaterThanOrEqualCondition(value1, value2);
        return this;
    }

    /**
     * Creates a less than or equal condition.
     *
     * @param value1 the first value
     * @param value2 the second value
     * @return this builder
     */
    public ConditionBuilder lessThanOrEqual(Object value1, Object value2) {
        condition = new LessThanOrEqualCondition(value1, value2);
        return this;
    }

    /**
     * Creates a math condition.
     *
     * @param value1 the first value
     * @param value2 the second value
     * @param operationName the name of the operation
     * @param comparisonOperator the comparison operator
     * @param comparisonValue the value to compare to
     * @return this builder
     */
    public ConditionBuilder math(Number value1, Number value2, String operationName, String comparisonOperator, Number comparisonValue) {
        MathOperation operation = MathOperations.getOperation(operationName);
        if (operation == null) {
            throw new IllegalArgumentException("Unknown operation: " + operationName);
        }
        condition = new MathCondition(value1, value2, operation, comparisonOperator, comparisonValue);
        return this;
    }

    /**
     * Creates an AND condition.
     *
     * @param conditions the conditions to AND together
     * @return this builder
     */
    public ConditionBuilder and(Condition... conditions) {
        condition = new AndCondition(conditions);
        return this;
    }

    /**
     * Creates an OR condition.
     *
     * @param conditions the conditions to OR together
     * @return this builder
     */
    public ConditionBuilder or(Condition... conditions) {
        condition = new OrCondition(conditions);
        return this;
    }

    /**
     * Creates a NOT condition.
     *
     * @param condition the condition to negate
     * @return this builder
     */
    public ConditionBuilder not(Condition condition) {
        this.condition = new NotCondition(condition);
        return this;
    }

    /**
     * Creates a ternary condition.
     *
     * @param condition the condition to check
     * @param trueCondition the condition to use if true
     * @param falseCondition the condition to use if false
     * @return this builder
     */
    public ConditionBuilder ternary(Condition condition, MaterialCondition trueCondition, MaterialCondition falseCondition) {
        this.condition = new TernaryCondition(condition, trueCondition, falseCondition);
        return this;
    }

    /**
     * Creates a ternary condition with materials.
     *
     * @param condition the condition to check
     * @param trueMaterial the material to use if true
     * @param falseMaterial the material to use if false
     * @return this builder
     */
    public ConditionBuilder ternary(Condition condition, Material trueMaterial, Material falseMaterial) {
        this.condition = new TernaryCondition(condition, trueMaterial, falseMaterial);
        return this;
    }

    /**
     * Sets the true material for the condition.
     *
     * @param material the material to use when the condition is true
     * @return this builder
     */
    public ConditionBuilder trueMaterial(Material material) {
        if (condition != null) {
            condition.setTrueMaterial(material);
        }
        return this;
    }

    /**
     * Sets the false material for the condition.
     *
     * @param material the material to use when the condition is false
     * @return this builder
     */
    public ConditionBuilder falseMaterial(Material material) {
        if (condition != null) {
            condition.setFalseMaterial(material);
        }
        return this;
    }

    /**
     * Sets the default material for the condition.
     *
     * @param material the default material
     * @return this builder
     */
    public ConditionBuilder defaultMaterial(Material material) {
        if (condition != null) {
            condition.setDefaultMaterial(material);
        }
        return this;
    }

    /**
     * Builds the condition.
     *
     * @return the built condition
     */
    public MaterialCondition build() {
        if (condition == null) {
            throw new IllegalStateException("No condition has been created");
        }
        return condition;
    }
}