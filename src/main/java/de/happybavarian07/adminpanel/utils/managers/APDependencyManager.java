package de.happybavarian07.adminpanel.utils.managers;

import com.saicone.ezlib.Ezlib;
import de.happybavarian07.adminpanel.hooks.BanManagerHook;
import de.happybavarian07.adminpanel.hooks.LiteBansHook;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import dte.hooksystem.api.HookSystemAPI;
import dte.hooksystem.service.HookService;
import org.bukkit.ChatColor;

import java.io.File;
import java.util.List;

/*
 * @Author HappyBavarian07
 * @Date September 11, 2024 | 19:05
 */
public class APDependencyManager {
    private final AdminPanelMain plugin;
    private final Ezlib ezlib;

    public APDependencyManager(AdminPanelMain plugin) {
        this.plugin = plugin;
        ezlib = new Ezlib(new File(plugin.getDataFolder() + "/libs"));
    }

    public void loadDependenciesOverDependencyManager() {
        plugin.getStartUpLogger().coloredSpacer(ChatColor.RED).message("&e&lLoading Dependencies&r");
        ezlib.init();
        plugin.getStartUpLogger().message("&e&lLoading Lucene Core Dependency&r");
        ezlib.dependency("org.apache.lucene:lucene-core:9.8.0").parent(true).load();
        plugin.getStartUpLogger().message("&e&lLoading Lucene QueryParser Dependency&r");
        ezlib.dependency("org.apache.lucene:lucene-queryparser:9.8.0").parent(true).load();
        plugin.getStartUpLogger().message("&e&lLoading Commons Codec Dependency&r");
        ezlib.dependency("commons-codec:commons-codec:1.16.0").parent(true).load();

        // Needed Dependencies for MySQL
        ezlib.dependency("org.xerial:sqlite-jdbc:3.36.0.3").parent(true).load();
        // MariaDB Driver
        ezlib.dependency("org.mariadb.jdbc:mariadb-java-client:3.3.3").parent(true).load();


        plugin.getStartUpLogger().message("&a&lDone&r").coloredSpacer(ChatColor.RED);
        plugin.getStartUpLogger().emptySpacer().emptySpacer();
    }

    public void loadDepepdenciesFromList(List<String> dependencies, boolean logging) {
        plugin.getStartUpLogger().coloredSpacer(ChatColor.RED).message("&e&lLoading Dependencies From List&r");
        for (String dependency : dependencies) {
            if (logging)
                plugin.getStartUpLogger().coloredSpacer(ChatColor.RED).message("&e&lLoading " + dependency + " Dependency&r");
            ezlib.dependency(dependency).parent(true).load();
        }
        plugin.getStartUpLogger().message("&a&lDone&r").coloredSpacer(ChatColor.RED);
        plugin.getStartUpLogger().emptySpacer().emptySpacer();
    }

    public void useHookSystem() {
        HookService hookService = HookSystemAPI.getService(plugin);

        hookService.register(new BanManagerHook("BanManager")).orElse((handler) -> {
            plugin.getLogger().warning("Failed to hook into BanManager! If you are using LiteBans as the Ban Plugin, please ignore this message. \n" +
                    "Else please check if Ban Manager is installed and running or send me your Ban Plugin if it is publically available over the /admin-panel:report feature, \n" +
                    "so i can implement it!");
        });
        hookService.register(new LiteBansHook("LiteBans")).orElse((handler) -> {
            plugin.getLogger().warning("Failed to hook into LiteBans! If you are using Ban Manager as the Ban Plugin, please ignore this message. \n" +
                    "Else please check if LiteBans is installed and running or send me your Ban Plugin if it is publically available over the /admin-panel:report feature, \n" +
                    "so i can implement it!");
        });
    }
}
