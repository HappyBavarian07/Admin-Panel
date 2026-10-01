package de.happybavarian07.adminpanel.trolls;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/** Plays the creeper fuse sound behind the target, then nothing happens. */
public final class CreeperHiss implements Troll {
    @Override
    public String id() {
        return "CreeperHiss";
    }

    @Override
    public Kind kind() {
        return Kind.ONE_SHOT;
    }

    @Override
    public void start(Player staff, Player target) {
        Location behind = TrollSupport.ahead(target.getLocation(), -1.5);
        target.playSound(behind, Sound.ENTITY_CREEPER_PRIMED, 1.0f, 1.0f);
    }
}
