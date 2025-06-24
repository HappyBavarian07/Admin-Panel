package de.happybavarian07.adminpanel.language.expressionparser.interfaces;

import org.bukkit.Material;

/*
 * @Author HappyBavarian07
 * @Date Mai 24, 2025 | 15:49
 */
public abstract class AbstractMaterialCondition extends AbstractCondition implements MaterialCondition {
    private Material trueMaterial;
    private Material falseMaterial;
    private Material defaultMaterial;

    /**
     * Creates a new abstract material condition.
     *
     * @param name the name of the condition
     * @param trueMaterial the material to return when the condition is true
     * @param falseMaterial the material to return when the condition is false
     * @param defaultMaterial the default material to return if no condition is met
     */
    public AbstractMaterialCondition(String name, Material trueMaterial, Material falseMaterial, Material defaultMaterial) {
        super(name);
        this.trueMaterial = trueMaterial;
        this.falseMaterial = falseMaterial;
        this.defaultMaterial = defaultMaterial;
    }

    /**
     * Creates a new abstract material condition with default materials set to null.
     *
     * @param name the name of the condition
     */
    public AbstractMaterialCondition(String name) {
        this(name, null, null, Material.STONE);
    }

    @Override
    public Material getMaterial() {
        if (isTrue()) {
            return trueMaterial != null ? trueMaterial : defaultMaterial;
        } else {
            return falseMaterial != null ? falseMaterial : defaultMaterial;
        }
    }

    @Override
    public MaterialCondition setTrueMaterial(Material material) {
        this.trueMaterial = material;
        return this;
    }

    @Override
    public MaterialCondition setFalseMaterial(Material material) {
        this.falseMaterial = material;
        return this;
    }

    @Override
    public MaterialCondition setDefaultMaterial(Material material) {
        this.defaultMaterial = material;
        return this;
    }
}