package de.happybavarian07.adminpanel.language.expressionparser.interfaces;

import org.bukkit.Material;

/*
 * @Author HappyBavarian07
 * @Date Mai 24, 2025 | 15:49
 */
public interface MaterialCondition extends Condition {
    /**
     * Evaluates the condition and returns the appropriate material.
     *
     * @return the material based on the condition evaluation
     */
    Material getMaterial();

    /**
     * Sets the material to return when the condition is true.
     *
     * @param material the material to return when true
     * @return this condition for chaining
     */
    MaterialCondition setTrueMaterial(Material material);

    /**
     * Sets the material to return when the condition is false.
     *
     * @param material the material to return when false
     * @return this condition for chaining
     */
    MaterialCondition setFalseMaterial(Material material);

    /**
     * Sets the default material to return if no condition is met.
     *
     * @param material the default material
     * @return this condition for chaining
     */
    MaterialCondition setDefaultMaterial(Material material);
}