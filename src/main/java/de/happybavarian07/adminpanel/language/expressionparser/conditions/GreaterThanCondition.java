package de.happybavarian07.adminpanel.language.expressionparser.conditions;

import de.happybavarian07.adminpanel.language.expressionparser.interfaces.AbstractMaterialCondition;
import de.happybavarian07.adminpanel.language.expressionparser.interfaces.Condition;
import de.happybavarian07.adminpanel.language.expressionparser.interfaces.MaterialCondition;
import org.bukkit.Material;

/*
 * @Author HappyBavarian07
 * @Date Mai 24, 2025 | 15:49
 */
public class GreaterThanCondition extends AbstractMaterialCondition {
    private final Object value1;
    private final Object value2;

    /**
     * Creates a new greater than condition.
     *
     * @param value1 the first value
     * @param value2 the second value
     */
    public GreaterThanCondition(Object value1, Object value2) {
        super("GreaterThan");
        this.value1 = value1;
        this.value2 = value2;
    }

    /**
     * Creates a new greater than condition with materials.
     *
     * @param value1          the first value
     * @param value2          the second value
     * @param trueMaterial    the material to return when the condition is true
     * @param falseMaterial   the material to return when the condition is false
     * @param defaultMaterial the default material to return if no condition is met
     */
    public GreaterThanCondition(Object value1, Object value2, Material trueMaterial, Material falseMaterial, Material defaultMaterial) {
        super("GreaterThan", trueMaterial, falseMaterial, defaultMaterial);
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override
    public boolean isTrue() {
        if (value1 == null || value2 == null) {
            return false;
        }

        Object val1 = value1;
        Object val2 = value2;

        if (val1 instanceof MaterialCondition) {
            val1 = ((MaterialCondition) val1).getMaterial();
        } else if (val1 instanceof Condition) {
            val1 = ((Condition) val1).isTrue();
        }

        if (val2 instanceof MaterialCondition) {
            val2 = ((MaterialCondition) val2).getMaterial();
        } else if (val2 instanceof Condition) {
            val2 = ((Condition) val2).isTrue();
        }

        if (val1 instanceof Number && val2 instanceof Number) {
            double num1 = ((Number) val1).doubleValue();
            double num2 = ((Number) val2).doubleValue();
            return num1 > num2;
        }

        if (val1 instanceof Comparable && val1.getClass().isInstance(val2)) {
            @SuppressWarnings("unchecked")
            Comparable<Object> comparable = (Comparable<Object>) val1;
            return comparable.compareTo(val2) > 0;
        }

        throw new IllegalArgumentException("Cannot compare values: " + val1 + " and " + val2);
    }
}