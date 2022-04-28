package de.happybavarian07.adminpanel.addonloader.api;/*
 * @Author HappyBavarian07
 * @Date 13.01.2022 | 19:25
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AddonTest extends Addon implements Listener {
    @Override
    public String getName() {
        return "Test";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getDescription() {
        return "Test Test";
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, AdminPanelMain.getPlugin());
        System.out.println("Enabled Example Addon!");
    }

    @Override
    public void onDisable() {
        System.out.println("Disabled Example Addon!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(Utils.format(event.getPlayer(), "%prefix% &9> &a+ &5" + event.getPlayer().getName() + "&a joined! (Example Addon)", "ExampleAddon"));
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        event.setQuitMessage(Utils.format(event.getPlayer(), "%prefix% &9> &a- 0&5" + event.getPlayer().getName() + "&a left! (Example Addon)", "ExampleAddon"));
    }
}
