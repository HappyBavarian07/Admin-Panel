package de.happybavarian07.adminpanel.trolls;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.coolstufflib.languagemanager.LanguageManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Random;

/** Teleports the target to a safe spot within 5 blocks. The staff member is told when there is none. */
public final class RandomHop implements Troll {
    private static final int RADIUS = 5;

    private final LanguageManager lgm;
    private final Random random = new Random();

    public RandomHop(LanguageManager lgm) {
        this.lgm = lgm;
    }

    @Override
    public String id() {
        return "RandomHop";
    }

    @Override
    public Kind kind() {
        return Kind.ONE_SHOT;
    }

    @Override
    public void start(Player staff, Player target) {
        Location from = target.getLocation();
        World world = from.getWorld();
        Optional<SafeSpot.Spot> spot = SafeSpot.find(from.getBlockX(), from.getBlockY(), from.getBlockZ(), RADIUS, random, new WorldProbe(world));
        if (spot.isEmpty()) {
            if (staff != null) CommandText.send(lgm, staff, "Trolls.NoSafeSpot", "%target%", target.getName());
            return;
        }
        SafeSpot.Spot to = spot.get();
        target.teleport(new Location(world, to.x() + 0.5, to.y(), to.z() + 0.5, from.getYaw(), from.getPitch()));
    }

    private record WorldProbe(World world) implements SafeSpot.Probe {
        @Override
        public boolean isFloor(int x, int y, int z) {
            Block block = world.getBlockAt(x, y, z);
            Material type = block.getType();
            return type.isSolid() && type != Material.MAGMA_BLOCK && type != Material.CACTUS
                    && type != Material.CAMPFIRE && type != Material.SOUL_CAMPFIRE && type != Material.SWEET_BERRY_BUSH;
        }

        @Override
        public boolean isFree(int x, int y, int z) {
            Block block = world.getBlockAt(x, y, z);
            Material type = block.getType();
            return block.isPassable() && !block.isLiquid() && type != Material.FIRE && type != Material.SOUL_FIRE
                    && type != Material.COBWEB && type != Material.SWEET_BERRY_BUSH && type != Material.WITHER_ROSE;
        }
    }
}
