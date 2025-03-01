package de.happybavarian07.adminpanel.menusystem.menu.playermanager;

import de.happybavarian07.adminpanel.events.troll.*;
import de.happybavarian07.adminpanel.menusystem.Menu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
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
        Player player = (Player) e.getWhoClicked();
        Player target = playerMenuUtility.getTarget();
        ItemStack item = e.getCurrentItem();

        if (target == null || !target.isOnline()) return;

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player, true);
        /*
        Troll Permissions:
                  AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.*:
                    default: op
                    children:
                      AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.Open: true
                      AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.FakeOp: true
                      AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.FakeDeop: true
                      AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.FakeTNT: true
                      AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.BuildPrevent: true
                      AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.DropPlayersInv: true
                      AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.VillagerSounds: true
                      AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.MuteChat: true
                      AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.HurtingWater: true
                      AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.DupeMobs: true
                      AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.KickForWhitelist: true
                      AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.KickForServerstop: true
                      AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.KickForError: true
                      AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.KickForConnectionReset: true
                      AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.FreezePlayer: true
         */
        // Items
        PlayerKickBecauseErrorEvent kickBecauseErrorEvent;
        if (item.equals(lgm.getItem("PlayerManager.TrollMenu.Kick.Serverstop", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.KickForServerstop")) {
                player.sendMessage(noPerms);
                return;
            }
            kickBecauseErrorEvent = new PlayerKickBecauseErrorEvent(player, target, "Server closed");
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(kickBecauseErrorEvent);
                if (!kickBecauseErrorEvent.isCancelled()) {
                    kickBecauseErrorEvent.getTarget().kickPlayer(kickBecauseErrorEvent.getErrorMessage());
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem("PlayerManager.TrollMenu.Kick.ConnectionReset", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.KickForConnectionReset")) {
                player.sendMessage(noPerms);
                return;
            }
            kickBecauseErrorEvent = new PlayerKickBecauseErrorEvent(player, target,
                    "Internal exception: java.net.SocketException: Connection reset.");
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(kickBecauseErrorEvent);
                if (!kickBecauseErrorEvent.isCancelled()) {
                    kickBecauseErrorEvent.getTarget().kickPlayer(kickBecauseErrorEvent.getErrorMessage());
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem("PlayerManager.TrollMenu.Kick.Whitelist", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.KickForWhitelist")) {
                player.sendMessage(noPerms);
                return;
            }
            kickBecauseErrorEvent = new PlayerKickBecauseErrorEvent(player, target, "You are not whitelisted on this server!");
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(kickBecauseErrorEvent);
                if (!kickBecauseErrorEvent.isCancelled()) {
                    kickBecauseErrorEvent.getTarget().kickPlayer(kickBecauseErrorEvent.getErrorMessage());
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem("PlayerManager.TrollMenu.Kick.ServerStoppedError", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.KickForError")) {
                player.sendMessage(noPerms);
                return;
            }
            kickBecauseErrorEvent = new PlayerKickBecauseErrorEvent(player, target,
                    "io.netty.channel.AbstractChannel$AnnotatedConnectException: Connection refused: no further informations:");
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(kickBecauseErrorEvent);
                if (!kickBecauseErrorEvent.isCancelled()) {
                    kickBecauseErrorEvent.getTarget().kickPlayer(kickBecauseErrorEvent.getErrorMessage());
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem("PlayerManager.TrollMenu.DropPlayerInv", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.DropPlayersInv")) {
                player.sendMessage(noPerms);
                return;
            }
            PlayerDropInvEvent dropInvEvent = new PlayerDropInvEvent(player, target);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(dropInvEvent);
                if (!dropInvEvent.isCancelled()) {
                    List<ItemStack> items = new ArrayList<>();

                    for (int i = 0; i < target.getInventory().getSize(); i++) {
                        if (target.getInventory().getItem(i) != null) {
                            items.add(target.getInventory().getItem(i));
                        }
                    }

                    target.getInventory().clear();

                    for (ItemStack itemDrop : items) {
                        target.getWorld().dropItem(target.getLocation(), itemDrop);
                    }

                    items.clear();
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem("PlayerManager.TrollMenu.FakeOp", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.FakeOp")) {
                player.sendMessage(noPerms);
                return;
            }
            PlayerFakeOpEvent fakeOpEvent = new PlayerFakeOpEvent(player, target,
                    ChatColor.GRAY + "[Server: Made " + target.getName() + " a server operator]");
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(fakeOpEvent);
                if (!fakeOpEvent.isCancelled()) {
                    target.sendMessage(fakeOpEvent.getMessage());
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem("PlayerManager.TrollMenu.FakeTNT", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.FakeTNT")) {
                player.sendMessage(noPerms);
                return;
            }
            TrollTNTSpawnEvent fakeTNTEvent = new TrollTNTSpawnEvent(player, target);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(fakeTNTEvent);
                if (!fakeTNTEvent.isCancelled()) {
                    Location loc = target.getLocation();
                    TNTPrimed tnt = target.getWorld().spawn(loc, TNTPrimed.class);
                    tnt.setFuseTicks(20);
                    tnt.setYield(0);
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem("PlayerManager.TrollMenu.FakeDeop", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.FakeDeop")) {
                player.sendMessage(noPerms);
                return;
            }
            PlayerFakeDeopEvent fakeDeopEvent = new PlayerFakeDeopEvent(player, target,
                    ChatColor.GRAY + "[Server: Made " + target.getName() + " no longer a server operator]");
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(fakeDeopEvent);
                if (!fakeDeopEvent.isCancelled()) {
                    target.sendMessage(fakeDeopEvent.getMessage());
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem("General.Close", target, false))) {
            if (!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new PlayerActionsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(player)).open();
        }
        // Enable/Disable Optionen
        else if (item.equals(lgm.getItem("PlayerManager.TrollMenu.HurtingWater.false", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.HurtingWater")) {
                player.sendMessage(noPerms);
                return;
            }
            inventory.setItem(e.getSlot(), lgm.getItem("PlayerManager.TrollMenu.HurtingWater.true", target, false));
            if (!plugin.getPluginStateManager().getHurtingWaterMap().containsKey(playerMenuUtility.getTargetUUID()))
                plugin.getPluginStateManager().getHurtingWaterMap().put(playerMenuUtility.getTargetUUID(), true);
        } else if (item.equals(lgm.getItem("PlayerManager.TrollMenu.HurtingWater.true", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.HurtingWater")) {
                player.sendMessage(noPerms);
                return;
            }
            inventory.setItem(e.getSlot(), lgm.getItem("PlayerManager.TrollMenu.HurtingWater.false", target, false));
            plugin.getPluginStateManager().getHurtingWaterMap().remove(playerMenuUtility.getTargetUUID());
        } else if (item.equals(lgm.getItem("PlayerManager.TrollMenu.BreakPlacePrevent.false", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.BlockPrevent")) {
                player.sendMessage(noPerms);
                return;
            }
            BlockBreakPreventEvent preventEvent = new BlockBreakPreventEvent(player, target, true);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(preventEvent);
                if (!preventEvent.isCancelled()) {
                    inventory.setItem(e.getSlot(), lgm.getItem("PlayerManager.TrollMenu.BreakPlacePrevent.true", target, false));
                    if (!plugin.getPluginStateManager().getBlockBreakPreventMap().containsKey(playerMenuUtility.getTargetUUID()))
                        plugin.getPluginStateManager().getBlockBreakPreventMap().put(playerMenuUtility.getTargetUUID(), true);
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem("PlayerManager.TrollMenu.BreakPlacePrevent.true", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.BlockPrevent")) {
                player.sendMessage(noPerms);
                return;
            }
            BlockBreakPreventEvent preventEvent = new BlockBreakPreventEvent(player, target, false);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(preventEvent);
                if (!preventEvent.isCancelled()) {
                    inventory.setItem(e.getSlot(), lgm.getItem("PlayerManager.TrollMenu.BreakPlacePrevent.false", target, false));
                    plugin.getPluginStateManager().getBlockBreakPreventMap().remove(playerMenuUtility.getTargetUUID());
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem("PlayerManager.TrollMenu.VillagerSounds.false", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.VillagerSounds")) {
                player.sendMessage(noPerms);
                return;
            }
            VillagerSoundsToggleEvent soundsToggleEvent = new VillagerSoundsToggleEvent(player, target, true);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(soundsToggleEvent);
                if (!soundsToggleEvent.isCancelled()) {
                    inventory.setItem(e.getSlot(), lgm.getItem("PlayerManager.TrollMenu.VillagerSounds.true", target, false));
                    if (!plugin.getPluginStateManager().getVillagerSoundsMap().containsKey(playerMenuUtility.getTargetUUID()))
                        plugin.getPluginStateManager().getVillagerSoundsMap().put(playerMenuUtility.getTargetUUID(), true);
                    Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                        if (plugin.getPluginStateManager().getVillagerSoundsMap().containsKey(playerMenuUtility.getTargetUUID())) {
                            target.playSound(target.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 50, (float) 1.0);
                        }
                    }, 0L, 10L);
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem("PlayerManager.TrollMenu.VillagerSounds.true", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.VillagerSounds")) {
                player.sendMessage(noPerms);
                return;
            }
            VillagerSoundsToggleEvent soundsToggleEvent = new VillagerSoundsToggleEvent(player, target, true);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(soundsToggleEvent);
                if (!soundsToggleEvent.isCancelled()) {
                    inventory.setItem(e.getSlot(), lgm.getItem("PlayerManager.TrollMenu.VillagerSounds.false", target, false));
                    plugin.getPluginStateManager().getVillagerSoundsMap().remove(playerMenuUtility.getTargetUUID());
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem("PlayerManager.TrollMenu.DupeMobsOnKill.false", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.DupeMobs")) {
                player.sendMessage(noPerms);
                return;
            }
            inventory.setItem(e.getSlot(), lgm.getItem("PlayerManager.TrollMenu.DupeMobsOnKill.true", target, false));
            if (!plugin.getPluginStateManager().getDupeMobsOnKillMap().containsKey(playerMenuUtility.getTargetUUID()))
                plugin.getPluginStateManager().getDupeMobsOnKillMap().put(playerMenuUtility.getTargetUUID(), true);
        } else if (item.equals(lgm.getItem("PlayerManager.TrollMenu.DupeMobsOnKill.true", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.DupeMobs")) {
                player.sendMessage(noPerms);
                return;
            }
            inventory.setItem(e.getSlot(), lgm.getItem("PlayerManager.TrollMenu.DupeMobsOnKill.false", target, false));
            plugin.getPluginStateManager().getDupeMobsOnKillMap().remove(playerMenuUtility.getTargetUUID());
        } else if (item.equals(lgm.getItem("PlayerManager.TrollMenu.ChatMute.false", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.MuteChat")) {
                player.sendMessage(noPerms);
                return;
            }
            PlayerChatMuteEvent muteEvent = new PlayerChatMuteEvent(player, target);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(muteEvent);
                if (!muteEvent.isCancelled()) {
                    inventory.setItem(e.getSlot(), lgm.getItem("PlayerManager.TrollMenu.ChatMute.true", target, false));
                    if (!plugin.getPluginStateManager().getChatMuteMap().containsKey(playerMenuUtility.getTargetUUID()))
                        plugin.getPluginStateManager().getChatMuteMap().put(playerMenuUtility.getTargetUUID(), true);
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem("PlayerManager.TrollMenu.ChatMute.true", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.MuteChat")) {
                player.sendMessage(noPerms);
                return;
            }
            PlayerChatUnMuteEvent unMuteEvent = new PlayerChatUnMuteEvent(player, target);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(unMuteEvent);
                if (!unMuteEvent.isCancelled()) {
                    inventory.setItem(e.getSlot(), lgm.getItem("PlayerManager.TrollMenu.ChatMute.false", target, false));
                    plugin.getPluginStateManager().getChatMuteMap().remove(playerMenuUtility.getTargetUUID());
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }
        } else if (item.equals(lgm.getItem("PlayerManager.TrollMenu.FreezePlayer.true", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.FreezePlayer")) {
                player.sendMessage(noPerms);
                return;
            }
            inventory.setItem(e.getSlot(), lgm.getItem("PlayerManager.TrollMenu.FreezePlayer.false", target, false));
            plugin.getPluginStateManager().getFreezePlayersMap().remove(playerMenuUtility.getTargetUUID());
        } else if (item.equals(lgm.getItem("PlayerManager.TrollMenu.FreezePlayer.false", target, false))) {
            if (!player.hasPermission("AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.FreezePlayer")) {
                player.sendMessage(noPerms);
                return;
            }
            inventory.setItem(e.getSlot(), lgm.getItem("PlayerManager.TrollMenu.FreezePlayer.true", target, false));
            plugin.getPluginStateManager().getFreezePlayersMap().put(playerMenuUtility.getTargetUUID(), true);
        }
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {

    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {

    }

    @Override
    public void setMenuItems() {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, super.FILLER);
        }
        String path = "PlayerManager.TrollMenu.";
        Player target = playerMenuUtility.getTarget();

        // Items
        if (plugin.getPluginStateManager().getHurtingWaterMap().containsKey(playerMenuUtility.getTargetUUID())) {
            inventory.setItem(getSlot(path + "HurtingWater.true", 4), lgm.getItem("PlayerManager.TrollMenu.HurtingWater.true", target, false));
        } else {
            inventory.setItem(getSlot(path + "HurtingWater.false", 4), lgm.getItem(path + "HurtingWater.false", target, false));
        }

        if (plugin.getPluginStateManager().getBlockBreakPreventMap().containsKey(playerMenuUtility.getTargetUUID())) {
            inventory.setItem(getSlot(path + "BreakPlacePrevent.true", 12), lgm.getItem(path + "BreakPlacePrevent.true", target, false));
        } else {
            inventory.setItem(getSlot(path + "BreakPlacePrevent.true", 12), lgm.getItem(path + "BreakPlacePrevent.false", target, false));
        }

        if (plugin.getPluginStateManager().getVillagerSoundsMap().containsKey(playerMenuUtility.getTargetUUID())) {
            inventory.setItem(getSlot(path + "VillagerSounds.true", 13), lgm.getItem(path + "VillagerSounds.true", target, false));
        } else {
            inventory.setItem(getSlot(path + "VillagerSounds.true", 13), lgm.getItem(path + "VillagerSounds.false", target, false));
        }

        if (plugin.getPluginStateManager().getDupeMobsOnKillMap().containsKey(playerMenuUtility.getTargetUUID())) {
            inventory.setItem(getSlot(path + "DupeMobsOnKill.true", 20), lgm.getItem(path + "DupeMobsOnKill.true", target, false));
        } else {
            inventory.setItem(getSlot(path + "DupeMobsOnKill.true", 20), lgm.getItem(path + "DupeMobsOnKill.false", target, false));
        }

        if (plugin.getPluginStateManager().getChatMuteMap().containsKey(playerMenuUtility.getTargetUUID())) {
            inventory.setItem(getSlot(path + "ChatMute.true", 24), lgm.getItem(path + "ChatMute.true", target, false));
        } else {
            inventory.setItem(getSlot(path + "ChatMute.true", 24), lgm.getItem(path + "ChatMute.false", target, false));
        }

        if (plugin.getPluginStateManager().getFreezePlayersMap().containsKey(playerMenuUtility.getTargetUUID())) {
            inventory.setItem(getSlot(path + "FreezePlayer.true", 31), lgm.getItem(path + "FreezePlayer.true", target, false));
        } else {
            inventory.setItem(getSlot(path + "FreezePlayer.true", 31), lgm.getItem(path + "FreezePlayer.false", target, false));
        }

        // Kicks
        inventory.setItem(getSlot(path + "Kick.Serverstop", 29), lgm.getItem(path + "Kick.Serverstop", target, false));
        inventory.setItem(getSlot(path + "Kick.ConnectionReset", 30), lgm.getItem(path + "Kick.ConnectionReset", target, false));
        inventory.setItem(getSlot(path + "Kick.Whitelist", 32), lgm.getItem(path + "Kick.Whitelist", target, false));
        inventory.setItem(getSlot(path + "Kick.ServerStoppedError", 33), lgm.getItem(path + "Kick.ServerStoppedError", target, false));
        // Items
        inventory.setItem(getSlot(path + "DropPlayerInv", 14), lgm.getItem(path + "DropPlayerInv", target, false));
        inventory.setItem(getSlot(path + "FakeOp", 21), lgm.getItem(path + "FakeOp", target, false));
        inventory.setItem(getSlot(path + "FakeTNT", 22), lgm.getItem(path + "FakeTNT", target, false));
        inventory.setItem(getSlot(path + "FakeDeop", 23), lgm.getItem(path + "FakeDeop", target, false));

        // Close Item
        inventory.setItem(53, lgm.getItem("General.Close", target, false));
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
            if(hurtingWaterRunnable != null) {
                hurtingWaterRunnable.cancel();
                hurtingWaterRunnable = null;
            }
        }
        if(plugin.getPluginStateManager().getFreezePlayersMap().containsKey(e.getPlayer().getUniqueId())) {
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
