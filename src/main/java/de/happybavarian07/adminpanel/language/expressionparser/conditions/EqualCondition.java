package de.happybavarian07.adminpanel.language.expressionparser.conditions;

import de.happybavarian07.adminpanel.language.expressionparser.interfaces.AbstractMaterialCondition;
import org.bukkit.Material;

/*
 * @Author HappyBavarian07
 * @Date Mai 24, 2025 | 15:49
 */
public class EqualCondition extends AbstractMaterialCondition {
    private final Object value1;
    private final Object value2;

    /**
     * Creates a new equal condition.
     *
     * @param value1 the first value
     * @param value2 the second value
     */
    public EqualCondition(Object value1, Object value2) {
        super("Equal");
        this.value1 = value1;
        this.value2 = value2;
    }

    /**
     * Creates a new equal condition with materials.
     *
     * @param value1 the first value
     * @param value2 the second value
     * @param trueMaterial the material to return when the condition is true
     * @param falseMaterial the material to return when the condition is false
     * @param defaultMaterial the default material to return if no condition is met
     */
    public EqualCondition(Object value1, Object value2, Material trueMaterial, Material falseMaterial, Material defaultMaterial) {
        super("Equal", trueMaterial, falseMaterial, defaultMaterial);
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override
    public boolean isTrue() {
        if (value1 == null && value2 == null) {
            return true;
        }
        if (value1 == null || value2 == null) {
            return false;
        }

        if (value1 instanceof Number && value2 instanceof Number) {
            double num1 = ((Number) value1).doubleValue();
            double num2 = ((Number) value2).doubleValue();
            return num1 == num2;
        }

        return value1.equals(value2);
    }
}