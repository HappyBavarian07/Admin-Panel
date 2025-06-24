package de.happybavarian07.adminpanel.language.expressionparser.conditions;

import de.happybavarian07.adminpanel.language.expressionparser.interfaces.AbstractMaterialCondition;
import org.bukkit.Material;

/*
 * @Author HappyBavarian07
 * @Date Mai 24, 2025 | 15:49
 */
public class LessThanOrEqualCondition extends AbstractMaterialCondition {
    private final Object value1;
    private final Object value2;

    /**
     * Creates a new less than or equal condition.
     *
     * @param value1 the first value
     * @param value2 the second value
     */
    public LessThanOrEqualCondition(Object value1, Object value2) {
        super("LessThanOrEqual");
        this.value1 = value1;
        this.value2 = value2;
    }

    /**
     * Creates a new less than or equal condition with materials.
     *
     * @param value1 the first value
     * @param value2 the second value
     * @param trueMaterial the material to return when the condition is true
     * @param falseMaterial the material to return when the condition is false
     * @param defaultMaterial the default material to return if no condition is met
     */
    public LessThanOrEqualCondition(Object value1, Object value2, Material trueMaterial, Material falseMaterial, Material defaultMaterial) {
        super("LessThanOrEqual", trueMaterial, falseMaterial, defaultMaterial);
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override
    public boolean isTrue() {
        if (value1 == null || value2 == null) {
            return value1 == value2; // Both null is true, one null is false
        }

        if (value1 instanceof Number && value2 instanceof Number) {
            double num1 = ((Number) value1).doubleValue();
            double num2 = ((Number) value2).doubleValue();
            return num1 <= num2;
        }

        if (value1 instanceof Comparable && value1.getClass().isInstance(value2)) {
            @SuppressWarnings("unchecked")
            Comparable<Object> comparable = (Comparable<Object>) value1;
            return comparable.compareTo(value2) <= 0;
        }

        throw new IllegalArgumentException("Cannot compare values: " + value1 + " and " + value2);
    }
}