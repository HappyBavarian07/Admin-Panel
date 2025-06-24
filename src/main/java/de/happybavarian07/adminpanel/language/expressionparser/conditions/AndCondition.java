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
public class AndCondition extends AbstractMaterialCondition {
    private final List<Condition> conditions;

    /**
     * Creates a new AND condition.
     *
     * @param conditions the conditions to check
     */
    public AndCondition(Condition... conditions) {
        super("AND");
        this.conditions = Arrays.asList(conditions);
    }

    /**
     * Creates a new AND condition with materials.
     *
     * @param trueMaterial the material to return when the condition is true
     * @param falseMaterial the material to return when the condition is false
     * @param defaultMaterial the default material to return if no condition is met
     * @param conditions the conditions to check
     */
    public AndCondition(Material trueMaterial, Material falseMaterial, Material defaultMaterial, Condition... conditions) {
        super("AND", trueMaterial, falseMaterial, defaultMaterial);
        this.conditions = Arrays.asList(conditions);
    }

    @Override
    public boolean isTrue() {
        if (conditions.isEmpty()) {
            return true; // Empty AND is true (logical identity)
        }

        for (Condition condition : conditions) {
            if (!condition.isTrue()) {
                return false; // If any condition is false, the AND is false
            }
        }

        return true; // All conditions are true
    }

    @Override
    public String getName() {
        StringBuilder name = new StringBuilder("AND(");
        for (int i = 0; i < conditions.size(); i++) {
            name.append(conditions.get(i).getName());
            if (i < conditions.size() - 1) {
                name.append(" && ");
            }
        }
        name.append(")");
        return name.toString();
    }
}