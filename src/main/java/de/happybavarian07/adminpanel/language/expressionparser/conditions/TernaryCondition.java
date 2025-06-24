package de.happybavarian07.adminpanel.language.expressionparser.conditions;

import de.happybavarian07.adminpanel.language.expressionparser.interfaces.AbstractMaterialCondition;
import de.happybavarian07.adminpanel.language.expressionparser.interfaces.Condition;
import de.happybavarian07.adminpanel.language.expressionparser.interfaces.MaterialCondition;
import org.bukkit.Material;

/*
 * @Author HappyBavarian07
 * @Date Mai 24, 2025 | 15:49
 */
public class TernaryCondition extends AbstractMaterialCondition {
    private final Condition condition;
    private final MaterialCondition trueCondition;
    private final MaterialCondition falseCondition;

    /**
     * Creates a new ternary condition.
     *
     * @param condition the condition to check
     * @param trueCondition the condition to use if the condition is true
     * @param falseCondition the condition to use if the condition is false
     */
    public TernaryCondition(Condition condition, MaterialCondition trueCondition, MaterialCondition falseCondition) {
        super("Ternary");
        this.condition = condition;
        this.trueCondition = trueCondition;
        this.falseCondition = falseCondition;
    }

    /**
     * Creates a new ternary condition with materials.
     *
     * @param condition the condition to check
     * @param trueMaterial the material to return when the condition is true
     * @param falseMaterial the material to return when the condition is false
     */
    public TernaryCondition(Condition condition, Material trueMaterial, Material falseMaterial) {
        super("Ternary");
        this.condition = condition;

        // Create simple material conditions for true and false cases
        this.trueCondition = new AbstractMaterialCondition("TrueCase") {
            @Override
            public boolean isTrue() {
                return true;
            }
        }.setTrueMaterial(trueMaterial).setFalseMaterial(trueMaterial).setDefaultMaterial(trueMaterial);

        this.falseCondition = new AbstractMaterialCondition("FalseCase") {
            @Override
            public boolean isTrue() {
                return true;
            }
        }.setTrueMaterial(falseMaterial).setFalseMaterial(falseMaterial).setDefaultMaterial(falseMaterial);
    }

    @Override
    public boolean isTrue() {
        if (condition == null) {
            return false;
        }

        return condition.isTrue();
    }

    @Override
    public Material getMaterial() {
        if (condition == null) {
            return super.getMaterial();
        }

        if (condition.isTrue()) {
            return trueCondition != null ? trueCondition.getMaterial() : super.getMaterial();
        } else {
            return falseCondition != null ? falseCondition.getMaterial() : super.getMaterial();
        }
    }

    @Override
    public String getName() {
        return "(" + (condition != null ? condition.getName() : "null") + " ? " +
                (trueCondition != null ? trueCondition.getName() : "null") + " : " +
                (falseCondition != null ? falseCondition.getName() : "null") + ")";
    }
}