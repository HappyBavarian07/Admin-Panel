package de.happybavarian07.adminpanel.menusystem.menu.playermanager;

import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.troll.*;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.menusystem.actions.PermissionAction;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class PlayerTrollMenu extends Menu implements Listener {
    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();

    public PlayerTrollMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.Open");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.TrollMenu", playerMenuUtility.getTarget());
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PlayerTrollMenu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        // Logic is now handled by registered MenuActions in setMenuItems
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {

    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {

    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        String path = "PlayerManager.TrollMenu.";
        Player target = playerMenuUtility.getTarget();
        if (target == null || !target.isOnline()) return;

        // Toggles
        boolean hurtingWaterState = plugin.getPluginStateManager().getHurtingWaterMap().containsKey(playerMenuUtility.getTargetUUID());
        registerButton(getSlot(path + "HurtingWater." + hurtingWaterState, 4), lgm.getItem(path + "HurtingWater." + hurtingWaterState, target, false),
                new PermissionAction("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.HurtingWater", (p, e) -> {
                    if (plugin.getPluginStateManager().getHurtingWaterMap().containsKey(playerMenuUtility.getTargetUUID())) {
                        inventory.setItem(getSlot(path + "HurtingWater.false", 4), lgm.getItem(path + "HurtingWater.false", target, false));
                        plugin.getPluginStateManager().getHurtingWaterMap().remove(playerMenuUtility.getTargetUUID());
                    } else {
                        inventory.setItem(getSlot(path + "HurtingWater.true", 4), lgm.getItem(path + "HurtingWater.true", target, false));
                        plugin.getPluginStateManager().getHurtingWaterMap().put(playerMenuUtility.getTargetUUID(), true);
                    }
                }, lgm), null);

        boolean blockPreventState = plugin.getPluginStateManager().getBlockBreakPreventMap().containsKey(playerMenuUtility.getTargetUUID());
        registerButton(getSlot(path + "BreakPlacePrevent." + blockPreventState, 12), lgm.getItem(path + "BreakPlacePrevent." + blockPreventState, target, false),
                new PermissionAction("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.BlockPrevent", (p, e) -> {
                    boolean newState = !plugin.getPluginStateManager().getBlockBreakPreventMap().containsKey(playerMenuUtility.getTargetUUID());
                    BlockBreakPreventEvent preventEvent = new BlockBreakPreventEvent(p, target, newState);
                    try {
                        AdminPanelMain.getAPI().callAdminPanelEvent(preventEvent);
                        if (!preventEvent.isCancelled()) {
                            if (newState) {
                                inventory.setItem(getSlot(path + "BreakPlacePrevent.true", 12), lgm.getItem(path + "BreakPlacePrevent.true", target, false));
                                plugin.getPluginStateManager().getBlockBreakPreventMap().put(playerMenuUtility.getTargetUUID(), true);
                            } else {
                                inventory.setItem(getSlot(path + "BreakPlacePrevent.false", 12), lgm.getItem(path + "BreakPlacePrevent.false", target, false));
                                plugin.getPluginStateManager().getBlockBreakPreventMap().remove(playerMenuUtility.getTargetUUID());
                            }
                        }
                    } catch (NotAPanelEventException ex) {
                        ex.printStackTrace();
                    }
                }, lgm), null);

        boolean villagerSoundsState = plugin.getPluginStateManager().getVillagerSoundsMap().containsKey(playerMenuUtility.getTargetUUID());
        registerButton(getSlot(path + "VillagerSounds." + villagerSoundsState, 13), lgm.getItem(path + "VillagerSounds." + villagerSoundsState, target, false),
                new PermissionAction("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.VillagerSounds", (p, e) -> {
                    boolean newState = !plugin.getPluginStateManager().getVillagerSoundsMap().containsKey(playerMenuUtility.getTargetUUID());
                    VillagerSoundsToggleEvent soundsToggleEvent = new VillagerSoundsToggleEvent(p, target, newState);
                    try {
                        AdminPanelMain.getAPI().callAdminPanelEvent(soundsToggleEvent);
                        if (!soundsToggleEvent.isCancelled()) {
                            if (newState) {
                                inventory.setItem(getSlot(path + "VillagerSounds.true", 13), lgm.getItem(path + "VillagerSounds.true", target, false));
                                plugin.getPluginStateManager().getVillagerSoundsMap().put(playerMenuUtility.getTargetUUID(), true);
                                Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                                    if (plugin.getPluginStateManager().getVillagerSoundsMap().containsKey(playerMenuUtility.getTargetUUID())) {
                                        target.playSound(target.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 50, (float) 1.0);
                                    }
                                }, 0L, 10L);
                            } else {
                                inventory.setItem(getSlot(path + "VillagerSounds.false", 13), lgm.getItem(path + "VillagerSounds.false", target, false));
                                plugin.getPluginStateManager().getVillagerSoundsMap().remove(playerMenuUtility.getTargetUUID());
                            }
                        }
                    } catch (NotAPanelEventException ex) {
                        ex.printStackTrace();
                    }
                }, lgm), null);

        boolean dupeMobsState = plugin.getPluginStateManager().getDupeMobsOnKillMap().containsKey(playerMenuUtility.getTargetUUID());
        registerButton(getSlot(path + "DupeMobsOnKill." + dupeMobsState, 20), lgm.getItem(path + "DupeMobsOnKill." + dupeMobsState, target, false),
                new PermissionAction("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.DupeMobs", (p, e) -> {
                    if (plugin.getPluginStateManager().getDupeMobsOnKillMap().containsKey(playerMenuUtility.getTargetUUID())) {
                        inventory.setItem(getSlot(path + "DupeMobsOnKill.false", 20), lgm.getItem(path + "DupeMobsOnKill.false", target, false));
                        plugin.getPluginStateManager().getDupeMobsOnKillMap().remove(playerMenuUtility.getTargetUUID());
                    } else {
                        inventory.setItem(getSlot(path + "DupeMobsOnKill.true", 20), lgm.getItem(path + "DupeMobsOnKill.true", target, false));
                        plugin.getPluginStateManager().getDupeMobsOnKillMap().put(playerMenuUtility.getTargetUUID(), true);
                    }
                }, lgm), null);

        boolean chatMuteState = plugin.getPluginStateManager().getChatMuteMap().containsKey(playerMenuUtility.getTargetUUID());
        registerButton(getSlot(path + "ChatMute." + chatMuteState, 24), lgm.getItem(path + "ChatMute." + chatMuteState, target, false),
                new PermissionAction("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.MuteChat", (p, e) -> {
                    boolean newState = !plugin.getPluginStateManager().getChatMuteMap().containsKey(playerMenuUtility.getTargetUUID());
                    try {
                        if (newState) {
                            PlayerChatMuteEvent muteEvent = new PlayerChatMuteEvent(p, target);
                            AdminPanelMain.getAPI().callAdminPanelEvent(muteEvent);
                            if (!muteEvent.isCancelled()) {
                                inventory.setItem(getSlot(path + "ChatMute.true", 24), lgm.getItem(path + "ChatMute.true", target, false));
                                plugin.getPluginStateManager().getChatMuteMap().put(playerMenuUtility.getTargetUUID(), true);
                            }
                        } else {
                            PlayerChatUnMuteEvent unMuteEvent = new PlayerChatUnMuteEvent(p, target);
                            AdminPanelMain.getAPI().callAdminPanelEvent(unMuteEvent);
                            if (!unMuteEvent.isCancelled()) {
                                inventory.setItem(getSlot(path + "ChatMute.false", 24), lgm.getItem(path + "ChatMute.false", target, false));
                                plugin.getPluginStateManager().getChatMuteMap().remove(playerMenuUtility.getTargetUUID());
                            }
                        }
                    } catch (NotAPanelEventException ex) {
                        ex.printStackTrace();
                    }
                }, lgm), null);

        boolean freezeState = plugin.getPluginStateManager().getFreezePlayersMap().containsKey(playerMenuUtility.getTargetUUID());
        registerButton(getSlot(path + "FreezePlayer." + freezeState, 31), lgm.getItem(path + "FreezePlayer." + freezeState, target, false),
                new PermissionAction("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.FreezePlayer", (p, e) -> {
                    if (plugin.getPluginStateManager().getFreezePlayersMap().containsKey(playerMenuUtility.getTargetUUID())) {
                        inventory.setItem(getSlot(path + "FreezePlayer.false", 31), lgm.getItem(path + "FreezePlayer.false", target, false));
                        plugin.getPluginStateManager().getFreezePlayersMap().remove(playerMenuUtility.getTargetUUID());
                    } else {
                        inventory.setItem(getSlot(path + "FreezePlayer.true", 31), lgm.getItem(path + "FreezePlayer.true", target, false));
                        plugin.getPluginStateManager().getFreezePlayersMap().put(playerMenuUtility.getTargetUUID(), true);
                    }
                }, lgm), null);

        // Kicks
        registerButton(getSlot(path + "Kick.Serverstop", 29), lgm.getItem(path + "Kick.Serverstop", target, false),
                new PermissionAction("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.KickForServerstop", (p, e) -> {
                    try {
                        PlayerKickBecauseErrorEvent event = new PlayerKickBecauseErrorEvent(p, target, "Server closed");
                        AdminPanelMain.getAPI().callAdminPanelEvent(event);
                        if (!event.isCancelled()) event.getTarget().kickPlayer(event.getErrorMessage());
                    } catch (NotAPanelEventException ex) {
                        ex.printStackTrace();
                    }
                }, lgm), null);
        registerButton(getSlot(path + "Kick.ConnectionReset", 30), lgm.getItem(path + "Kick.ConnectionReset", target, false),
                new PermissionAction("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.KickForConnectionReset", (p, e) -> {
                    try {
                        PlayerKickBecauseErrorEvent event = new PlayerKickBecauseErrorEvent(p, target, "Internal exception: java.net.SocketException: Connection reset.");
                        AdminPanelMain.getAPI().callAdminPanelEvent(event);
                        if (!event.isCancelled()) event.getTarget().kickPlayer(event.getErrorMessage());
                    } catch (NotAPanelEventException ex) {
                        ex.printStackTrace();
                    }
                }, lgm), null);
        registerButton(getSlot(path + "Kick.Whitelist", 32), lgm.getItem(path + "Kick.Whitelist", target, false),
                new PermissionAction("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.KickForWhitelist", (p, e) -> {
                    try {
                        PlayerKickBecauseErrorEvent event = new PlayerKickBecauseErrorEvent(p, target, "You are not whitelisted on this server!");
                        AdminPanelMain.getAPI().callAdminPanelEvent(event);
                        if (!event.isCancelled()) event.getTarget().kickPlayer(event.getErrorMessage());
                    } catch (NotAPanelEventException ex) {
                        ex.printStackTrace();
                    }
                }, lgm), null);
        registerButton(getSlot(path + "Kick.ServerStoppedError", 33), lgm.getItem(path + "Kick.ServerStoppedError", target, false),
                new PermissionAction("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.KickForError", (p, e) -> {
                    try {
                        PlayerKickBecauseErrorEvent event = new PlayerKickBecauseErrorEvent(p, target, "io.netty.channel.AbstractChannel$AnnotatedConnectException: Connection refused: no further informations:");
                        AdminPanelMain.getAPI().callAdminPanelEvent(event);
                        if (!event.isCancelled()) event.getTarget().kickPlayer(event.getErrorMessage());
                    } catch (NotAPanelEventException ex) {
                        ex.printStackTrace();
                    }
                }, lgm), null);

        // Action items
        registerButton(getSlot(path + "DropPlayerInv", 14), lgm.getItem(path + "DropPlayerInv", target, false),
                new PermissionAction("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.DropPlayersInv", (p, e) -> {
                    try {
                        PlayerDropInvEvent dropInvEvent = new PlayerDropInvEvent(p, target);
                        AdminPanelMain.getAPI().callAdminPanelEvent(dropInvEvent);
                        if (!dropInvEvent.isCancelled()) {
                            List<ItemStack> items = new ArrayList<>();
                            for (int i = 0; i < target.getInventory().getSize(); i++) {
                                if (target.getInventory().getItem(i) != null)
                                    items.add(target.getInventory().getItem(i));
                            }
                            target.getInventory().clear();
                            for (ItemStack itemDrop : items) {
                                target.getWorld().dropItem(target.getLocation(), itemDrop);
                            }
                        }
                    } catch (NotAPanelEventException ex) {
                        ex.printStackTrace();
                    }
                }, lgm), null);
        registerButton(getSlot(path + "FakeOp", 21), lgm.getItem(path + "FakeOp", target, false),
                new PermissionAction("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.FakeOp", (p, e) -> {
                    try {
                        PlayerFakeOpEvent event = new PlayerFakeOpEvent(p, target, ChatColor.GRAY + "[Server: Made " + target.getName() + " a server operator]");
                        AdminPanelMain.getAPI().callAdminPanelEvent(event);
                        if (!event.isCancelled()) target.sendMessage(event.getMessage());
                    } catch (NotAPanelEventException ex) {
                        ex.printStackTrace();
                    }
                }, lgm), null);
        registerButton(getSlot(path + "FakeTNT", 22), lgm.getItem(path + "FakeTNT", target, false),
                new PermissionAction("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.FakeTNT", (p, e) -> {
                    try {
                        TrollTNTSpawnEvent event = new TrollTNTSpawnEvent(p, target);
                        AdminPanelMain.getAPI().callAdminPanelEvent(event);
                        if (!event.isCancelled()) {
                            Location loc = target.getLocation();
                            TNTPrimed tnt = target.getWorld().spawn(loc, TNTPrimed.class);
                            tnt.setFuseTicks(20);
                            tnt.setYield(0);
                        }
                    } catch (NotAPanelEventException ex) {
                        ex.printStackTrace();
                    }
                }, lgm), null);
        registerButton(getSlot(path + "FakeDeop", 23), lgm.getItem(path + "FakeDeop", target, false),
                new PermissionAction("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.FakeDeop", (p, e) -> {
                    try {
                        PlayerFakeDeopEvent event = new PlayerFakeDeopEvent(p, target, ChatColor.GRAY + "[Server: Made " + target.getName() + " no longer a server operator]");
                        AdminPanelMain.getAPI().callAdminPanelEvent(event);
                        if (!event.isCancelled()) target.sendMessage(event.getMessage());
                    } catch (NotAPanelEventException ex) {
                        ex.printStackTrace();
                    }
                }, lgm), null);

        registerButton(getSlot("General.Close", 53), lgm.getItem("General.Close", target, false),
                new PermissionAction("AdminPanel.Button.Close", (p, e) -> {
                    new PlayerActionsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p)).open();
                }, lgm), null);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (plugin.getPluginStateManager().getBlockBreakPreventMap().containsKey(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (plugin.getPluginStateManager().getChatMuteMap().containsKey(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (plugin.getPluginStateManager().getBlockBreakPreventMap().containsKey(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    BukkitRunnable hurtingWaterRunnable;

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (plugin.getPluginStateManager().getHurtingWaterMap().containsKey(e.getPlayer().getUniqueId())) {
            if (hurtingWaterRunnable == null || hurtingWaterRunnable.isCancelled() || !Bukkit.getScheduler().isCurrentlyRunning(hurtingWaterRunnable.getTaskId())) {
                hurtingWaterRunnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!plugin.getPluginStateManager().getHurtingWaterMap().containsKey(e.getPlayer().getUniqueId())) {
                            cancel();
                            hurtingWaterRunnable = null;
                            return;
                        }
                        if (e.getPlayer().getLocation().getBlock().getType() == Material.WATER) {
                            if (e.getPlayer().getHealth() > 0.2) {
                                e.getPlayer().setHealth(e.getPlayer().getHealth() - 0.09);
                                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_GENERIC_HURT, 100, 1.0f);
                            }
                        }
                    }
                };
                hurtingWaterRunnable.runTaskTimer(plugin, 20, 100);
            }
        } else {
            if (hurtingWaterRunnable != null) {
                hurtingWaterRunnable.cancel();
                hurtingWaterRunnable = null;
            }
        }
        if (plugin.getPluginStateManager().getFreezePlayersMap().containsKey(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onKill(EntityDeathEvent e) {
        if (e.getEntity().getKiller() != null) {
            e.getEntity();
            if (plugin.getPluginStateManager().getDupeMobsOnKillMap().containsKey(e.getEntity().getKiller().getUniqueId())) {
                e.getDrops().clear();
                e.setDroppedExp(0);
                for (int i = 0; i < plugin.getConfig().getInt("Pman.Troll.MobDupe"); i++) {
                    e.getEntity().getKiller().getWorld().spawnEntity(e.getEntity().getLocation(), e.getEntityType());
                }
                e.getEntity().getKiller().getWorld().spawnEntity(e.getEntity().getLocation(), e.getEntityType());
            }
        }
    }
}
