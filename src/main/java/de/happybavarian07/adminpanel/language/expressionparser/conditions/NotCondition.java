package de.happybavarian07.adminpanel.language.expressionparser.conditions;

import de.happybavarian07.adminpanel.language.expressionparser.interfaces.AbstractMaterialCondition;
import de.happybavarian07.adminpanel.language.expressionparser.interfaces.Condition;
import org.bukkit.Material;

/*
 * @Author HappyBavarian07
 * @Date Mai 24, 2025 | 15:49
 */
public class NotCondition extends AbstractMaterialCondition {
    private final Condition condition;

    /**
     * Creates a new NOT condition.
     *
     * @param condition the condition to negate
     */
    public NotCondition(Condition condition) {
        super("NOT");
        this.condition = condition;
    }

    /**
     * Creates a new NOT condition with materials.
     *
     * @param condition the condition to negate
     * @param trueMaterial the material to return when the condition is true
     * @param falseMaterial the material to return when the condition is false
     * @param defaultMaterial the default material to return if no condition is met
     */
    public NotCondition(Condition condition, Material trueMaterial, Material falseMaterial, Material defaultMaterial) {
        super("NOT", trueMaterial, falseMaterial, defaultMaterial);
        this.condition = condition;
    }

    @Override
    public boolean isTrue() {
        if (condition == null) {
            return false;
        }

        return !condition.isTrue();
    }

    @Override
    public String getName() {
        return "NOT(" + (condition != null ? condition.getName() : "null") + ")";
    }
}