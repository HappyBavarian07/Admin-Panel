package de.happybavarian07.main;

import me.clip.placeholderapi.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class UpdateChecker implements Listener {

    private final int RESOURCE_ID = 91800;
    private final Main plugin;
    private final String pluginVersion;
    private String spigotVersion;
    private boolean updateAvailable;

    public UpdateChecker(Main plugin) {
        this.plugin = plugin;
        pluginVersion = plugin.getDescription().getVersion();
    }

    public boolean hasUpdateAvailable() {
        return updateAvailable;
    }

    public String getSpigotVersion() {
        return spigotVersion;
    }

    public void fetch() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpsURLConnection con = (HttpsURLConnection) new URL(
                        "https://api.spigotmc.org/legacy/update.php?resource=" + RESOURCE_ID).openConnection();
                con.setRequestMethod("GET");
                spigotVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
            } catch (Exception ex) {
                plugin.getLogger().info("Failed to check for updates on spigot.");
                return;
            }

            if (spigotVersion == null || spigotVersion.isEmpty()) {
                return;
            }

            updateAvailable = spigotIsNewer();

            if (!updateAvailable) {
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getLogger().info("An update for AdminPanel (v" + getSpigotVersion() + ") is available at:");
                plugin.getLogger().info("https://www.spigotmc.org/resources/servermanager-an-mc-integrated-admin-panel-german." + RESOURCE_ID + "/");
                Bukkit.getPluginManager().registerEvents(this, plugin);
            });
        });
    }

    private boolean spigotIsNewer() {
        if (spigotVersion == null || spigotVersion.isEmpty()) {
            return false;
        }

        int pluginV = toReadable(pluginVersion);
        int spigotV = toReadable(spigotVersion);

        if (pluginV < spigotV) {
            return true;
        } else if ((pluginV < spigotV)) {
            return true;
        } else {
            return pluginV < spigotV;
        }
    }

    private int toReadable(String version) {
        return Integer.parseInt(version);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        if (e.getPlayer().hasPermission("AdminPanel.updatenotify")) {
            Msg.msg(e.getPlayer(),
                    "&bAn update for &5Admin&aPanel &e(&5Admin&aPanel &fv" + getSpigotVersion()
                            + "&e)"
                    , "&bis available at &ehttps://www.spigotmc.org/resources/servermanager-an-mc-integrated-admin-panel-german." + RESOURCE_ID
                            + "/");
        }
    }
}
