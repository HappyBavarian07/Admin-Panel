package de.happybavarian07.adminpanel.language.expressionparser.conditions;

import de.happybavarian07.adminpanel.language.expressionparser.interfaces.MaterialCondition;
import de.happybavarian07.adminpanel.main.Head;
import org.bukkit.Material;

/**
 * A MaterialCondition implementation that represents a player head.
 * <p>
 * This class can represent both player name heads and texture value heads.
 * </p>
 */
public class HeadMaterialCondition implements MaterialCondition {
    private final String headValue;
    private final Head head;
    private final boolean isTexture;
    private Material trueMaterial = Material.GREEN_WOOL;
    private Material falseMaterial = Material.RED_WOOL;
    private Material defaultMaterial = Material.STONE;

    /**
     * Creates a new HeadMaterialCondition.
     *
     * @param headValue The player name or texture value
     * @param isTexture True if the headValue is a texture, false if it's a player name
     */
    public HeadMaterialCondition(String headValue, boolean isTexture) {
        this.headValue = headValue;
        this.isTexture = isTexture;
        this.head = null;
    }

    public HeadMaterialCondition(Head head) {
        this.headValue = null;
        this.isTexture = false;
        this.head = head;
    }

    @Override
    public Material getMaterial() {
        // This returns just the basic material, the actual head creation happens in LanguageManager
        return Material.PLAYER_HEAD;
    }

    @Override
    public boolean isTrue() {
        // Head conditions are always "true" for material selection
        return true;
    }

    @Override
    public String getName() {
        if (head != null) {
            return "HEAD_OBJECT:" + head.name();
        }
        if (headValue == null) {
            return "HEAD:UNKNOWN";
        }
        return isTexture ? "HEAD_TEXTURE:" + headValue : "HEAD:" + headValue;
    }

    /**
     * Gets the head value (player name or texture).
     *
     * @return The head value
     */
    public String getHeadValue() {
        return headValue;
    }

    /**
     * Gets the head object if available.
     *
     * @return The head object, or null if not available
     */
    public Head getHead() {
        return head;
    }

    /**
     * Checks if this head condition represents a texture head.
     *
     * @return True if this is a texture head, false if it's a player name head
     */
    public boolean isTexture() {
        return isTexture;
    }

    public boolean isHead() {
        return head != null;
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
