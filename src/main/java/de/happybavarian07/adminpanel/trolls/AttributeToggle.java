package de.happybavarian07.adminpanel.trolls;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A toggle that scales one attribute of the target with a modifier of its own. Stopping removes only that modifier.
 * Modifiers are saved with the player, so one that survived a crash is removed when the player joins.
 */
abstract class AttributeToggle implements Troll, Listener {
    private final String id;
    private final Attribute attribute;
    private final UUID modifierId;
    private final String modifierName;
    private final double amount;
    private final Set<UUID> active = new HashSet<>();

    /**
     * @param attributeNames the attribute under its names in the different versions, see {@link TrollSupport#attribute}
     * @param amount         added to the multiplier of the attribute, -1 removes the whole value
     */
    AttributeToggle(String id, double amount, String... attributeNames) {
        this.id = id;
        this.amount = amount;
        this.attribute = TrollSupport.attribute(attributeNames);
        this.modifierName = "AdminPanel-" + id;
        this.modifierId = UUID.nameUUIDFromBytes(modifierName.getBytes(StandardCharsets.UTF_8));
    }

    /** @return {@code false} if the running server has no such attribute, then the troll must not be registered */
    boolean isSupported() {
        return attribute != null;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Kind kind() {
        return Kind.TOGGLE;
    }

    @Override
    public boolean isActive(UUID target) {
        return active.contains(target);
    }

    @Override
    public void start(Player staff, Player target) {
        AttributeInstance instance = target.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(modifier());
        instance.addModifier(modifier());
        active.add(target.getUniqueId());
    }

    @Override
    public void stop(Player target) {
        active.remove(target.getUniqueId());
        AttributeInstance instance = target.getAttribute(attribute);
        if (instance != null) instance.removeModifier(modifier());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (active.contains(player.getUniqueId())) return;
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) instance.removeModifier(modifier());
    }

    private AttributeModifier modifier() {
        return new AttributeModifier(modifierId, modifierName, amount, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
    }
}
