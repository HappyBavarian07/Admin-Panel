package de.happybavarian07.adminpanel.trolls;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The trolls from before the registry. They keep their old permissions, item texts and the state in the maps of the
 * {@code PluginStateManager}, which are saved with the plugin state and synced between servers. Their enforcing
 * listeners are registered once with the plugin, so they work while the troll menu is closed.
 */
final class LegacyTrolls {
    private LegacyTrolls() {
    }

    static void registerAll(TrollRegistry registry, Plugin plugin) {
        registry.register(new HurtingWater(plugin));
        registry.register(new BlockPrevent());
        registry.register(new VillagerSounds(plugin));
        registry.register(new DupeMobs(plugin));
        registry.register(new ChatMute());
        registry.register(new Freeze());
        registry.register(new Kick("KickServerstop", "Kick.Serverstop", "KickForServerstop", "Server closed"));
        registry.register(new Kick("KickConnectionReset", "Kick.ConnectionReset", "KickForConnectionReset",
                "Internal exception: java.net.SocketException: Connection reset."));
        registry.register(new Kick("KickWhitelist", "Kick.Whitelist", "KickForWhitelist", "You are not whitelisted on this server!"));
        registry.register(new Kick("KickError", "Kick.ServerStoppedError", "KickForError",
                "io.netty.channel.AbstractChannel$AnnotatedConnectException: Connection refused: no further informations:"));
        registry.register(new DropInventory());
        registry.register(new FakeServerMessage("FakeOp", "FakeOp", "[Server: Made %s a server operator]"));
        registry.register(new FakeServerMessage("FakeDeop", "FakeDeop", "[Server: Made %s no longer a server operator]"));
    }

    private static Map<UUID, Boolean> hurtingWaterMap() {
        return AdminPanelMain.getPlugin().getPluginStateManager().getHurtingWaterMap();
    }

    /** A toggle whose state is one of the maps of the plugin state. */
    private abstract static class MapToggle implements Troll {
        private final String id;
        private final String itemKey;
        private final String permissionName;

        MapToggle(String id, String itemKey, String permissionName) {
            this.id = id;
            this.itemKey = itemKey;
            this.permissionName = permissionName;
        }

        abstract Map<UUID, Boolean> state();

        @Override
        public String id() {
            return id;
        }

        @Override
        public String itemKey() {
            return itemKey;
        }

        @Override
        public String permission() {
            return PERMISSION_PREFIX + permissionName;
        }

        @Override
        public Kind kind() {
            return Kind.TOGGLE;
        }

        @Override
        public boolean isActive(UUID target) {
            return state().containsKey(target);
        }

        @Override
        public void start(Player staff, Player target) {
            state().put(target.getUniqueId(), true);
        }

        @Override
        public void stop(Player target) {
            state().remove(target.getUniqueId());
        }
    }

    /** A toggle that plays something on a timer while it runs. */
    private abstract static class TimedToggle extends MapToggle implements Listener {
        private final Plugin plugin;
        private final long period;
        private final Map<UUID, BukkitTask> tasks = new HashMap<>();

        TimedToggle(Plugin plugin, long period, String id, String itemKey, String permissionName) {
            super(id, itemKey, permissionName);
            this.plugin = plugin;
            this.period = period;
        }

        abstract void tick(Player target);

        @Override
        public void start(Player staff, Player target) {
            super.start(staff, target);
            schedule(target.getUniqueId());
        }

        @Override
        public void stop(Player target) {
            super.stop(target);
            BukkitTask task = tasks.remove(target.getUniqueId());
            if (task != null) task.cancel();
        }

        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            if (isActive(event.getPlayer().getUniqueId())) schedule(event.getPlayer().getUniqueId());
        }

        private void schedule(UUID id) {
            if (tasks.containsKey(id)) return;
            tasks.put(id, Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                Player target = Bukkit.getPlayer(id);
                if (target != null && isActive(id)) tick(target);
            }, 20L, period));
        }
    }

    private static final class HurtingWater extends TimedToggle {
        HurtingWater(Plugin plugin) {
            super(plugin, 100L, "HurtingWater", "HurtingWater", "HurtingWater");
        }

        @Override
        Map<UUID, Boolean> state() {
            return hurtingWaterMap();
        }

        @Override
        void tick(Player target) {
            if (target.getLocation().getBlock().getType() == Material.WATER && target.getHealth() > 0.2) {
                target.setHealth(Math.max(0.2, target.getHealth() - 0.09));
                target.playSound(target.getLocation(), Sound.ENTITY_GENERIC_HURT, 100, 1.0f);
            }
        }
    }

    private static final class VillagerSounds extends TimedToggle {
        VillagerSounds(Plugin plugin) {
            super(plugin, 10L, "VillagerSounds", "VillagerSounds", "VillagerSounds");
        }

        @Override
        Map<UUID, Boolean> state() {
            return AdminPanelMain.getPlugin().getPluginStateManager().getVillagerSoundsMap();
        }

        @Override
        void tick(Player target) {
            target.playSound(target.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 50, 1.0f);
        }
    }

    private static final class BlockPrevent extends MapToggle implements Listener {
        BlockPrevent() {
            super("BlockPrevent", "BreakPlacePrevent", "BlockPrevent");
        }

        @Override
        Map<UUID, Boolean> state() {
            return AdminPanelMain.getPlugin().getPluginStateManager().getBlockBreakPreventMap();
        }

        @EventHandler
        public void onBreak(BlockBreakEvent event) {
            if (isActive(event.getPlayer().getUniqueId())) event.setCancelled(true);
        }

        @EventHandler
        public void onPlace(BlockPlaceEvent event) {
            if (isActive(event.getPlayer().getUniqueId())) event.setCancelled(true);
        }
    }

    private static final class DupeMobs extends MapToggle implements Listener {
        private final Plugin plugin;

        DupeMobs(Plugin plugin) {
            super("DupeMobs", "DupeMobsOnKill", "DupeMobs");
            this.plugin = plugin;
        }

        @Override
        Map<UUID, Boolean> state() {
            return AdminPanelMain.getPlugin().getPluginStateManager().getDupeMobsOnKillMap();
        }

        @EventHandler
        public void onKill(EntityDeathEvent event) {
            Player killer = event.getEntity().getKiller();
            if (killer == null || !isActive(killer.getUniqueId())) return;
            event.getDrops().clear();
            event.setDroppedExp(0);
            int copies = plugin.getConfig().getInt("Pman.Troll.MobDupe") + 1;
            for (int i = 0; i < copies; i++) {
                killer.getWorld().spawnEntity(event.getEntity().getLocation(), event.getEntityType());
            }
        }
    }

    private static final class ChatMute extends MapToggle implements Listener {
        ChatMute() {
            super("ChatMute", "ChatMute", "MuteChat");
        }

        @Override
        Map<UUID, Boolean> state() {
            return AdminPanelMain.getPlugin().getPluginStateManager().getChatMuteMap();
        }

        @EventHandler
        public void onChat(AsyncPlayerChatEvent event) {
            if (isActive(event.getPlayer().getUniqueId())) event.setCancelled(true);
        }
    }

    private static final class Freeze extends MapToggle implements Listener {
        Freeze() {
            super("Freeze", "FreezePlayer", "FreezePlayer");
        }

        @Override
        Map<UUID, Boolean> state() {
            return AdminPanelMain.getPlugin().getPluginStateManager().getFreezePlayersMap();
        }

        @EventHandler
        public void onMove(PlayerMoveEvent event) {
            Location to = event.getTo();
            Location from = event.getFrom();
            if (to == null || !isActive(event.getPlayer().getUniqueId())) return;
            if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) event.setCancelled(true);
        }
    }

    /** One shots that need no state. */
    private abstract static class Instant implements Troll {
        private final String id;
        private final String itemKey;
        private final String permissionName;

        Instant(String id, String itemKey, String permissionName) {
            this.id = id;
            this.itemKey = itemKey;
            this.permissionName = permissionName;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public String itemKey() {
            return itemKey;
        }

        @Override
        public String permission() {
            return PERMISSION_PREFIX + permissionName;
        }

        @Override
        public Kind kind() {
            return Kind.ONE_SHOT;
        }
    }

    private static final class Kick extends Instant {
        private final String message;

        Kick(String id, String itemKey, String permissionName, String message) {
            super(id, itemKey, permissionName);
            this.message = message;
        }

        @Override
        public void start(Player staff, Player target) {
            target.kickPlayer(message);
        }
    }

    private static final class DropInventory extends Instant {
        DropInventory() {
            super("DropInventory", "DropPlayerInv", "DropPlayersInv");
        }

        @Override
        public void start(Player staff, Player target) {
            List<ItemStack> items = new ArrayList<>();
            for (ItemStack item : target.getInventory().getContents()) {
                if (item != null) items.add(item);
            }
            target.getInventory().clear();
            for (ItemStack item : items) {
                target.getWorld().dropItem(target.getLocation(), item);
            }
        }
    }

    private static final class FakeServerMessage extends Instant {
        private final String format;

        FakeServerMessage(String id, String permissionName, String format) {
            super(id, id, permissionName);
            this.format = format;
        }

        @Override
        public void start(Player staff, Player target) {
            target.sendMessage(ChatColor.GRAY + String.format(format, target.getName()));
        }
    }
}
