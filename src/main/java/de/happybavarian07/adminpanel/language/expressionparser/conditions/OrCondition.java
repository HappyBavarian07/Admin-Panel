package de.happybavarian07.adminpanel.language.expressionparser.conditions;

import de.happybavarian07.adminpanel.language.expressionparser.interfaces.AbstractMaterialCondition;
import de.happybavarian07.adminpanel.language.expressionparser.interfaces.Condition;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

/*
 * @Author HappyBavarian07
 * @Date Mai 24, 2025 | 15:49
 */
public class OrCondition extends AbstractMaterialCondition {
    private final List<Condition> conditions;

    /**
     * Creates a new OR condition.
     *
     * @param conditions the conditions to check
     */
    public OrCondition(Condition... conditions) {
        super("OR");
        this.conditions = Arrays.asList(conditions);
    }

    /**
     * Creates a new OR condition with materials.
     *
     * @param trueMaterial the material to return when the condition is true
     * @param falseMaterial the material to return when the condition is false
     * @param defaultMaterial the default material to return if no condition is met
     * @param conditions the conditions to check
     */
    public OrCondition(Material trueMaterial, Material falseMaterial, Material defaultMaterial, Condition... conditions) {
        super("OR", trueMaterial, falseMaterial, defaultMaterial);
        this.conditions = Arrays.asList(conditions);
    }

    @Override
    public boolean isTrue() {
        if (conditions.isEmpty()) {
            return false; // Empty OR is false (logical identity)
        }

        for (Condition condition : conditions) {
            if (condition.isTrue()) {
                return true; // If any condition is true, the OR is true
            }
        }

        return false; // All conditions are false
    }

    @Override
    public String getName() {
        StringBuilder name = new StringBuilder("OR(");
        for (int i = 0; i < conditions.size(); i++) {
            name.append(conditions.get(i).getName());
            if (i < conditions.size() - 1) {
                name.append(" || ");
            }
        }
        name.append(")");
        return name.toString();
    }
}