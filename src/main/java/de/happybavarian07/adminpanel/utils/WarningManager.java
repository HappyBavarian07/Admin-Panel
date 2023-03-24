package de.happybavarian07.adminpanel.utils;/*
 * @Author HappyBavarian07
 * @Date 13.11.2022 | 12:09
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class WarningManager {
    // Jeder UUID wird eine Liste an Warnings zugewiesen
    private final Map<UUID, List<Warning>> warnings;
    private final AdminPanelMain plugin;
    private final File warningFile;
    private final FileConfiguration warningConfig;

    public WarningManager(AdminPanelMain plugin, File warningFile) {
        this.warnings = new HashMap<>();
        this.plugin = plugin;
        this.warningFile = warningFile;
        if (!warningFile.exists()) {
            try {
                warningFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.warningConfig = YamlConfiguration.loadConfiguration(warningFile);
        if(!warningConfig.isConfigurationSection("WarningCount")) warningConfig.createSection("WarningCount");
        if(!warningConfig.isConfigurationSection("Warnings")) warningConfig.createSection("Warnings");
    }

    public boolean hasWarningCount(UUID player, int count) {
        int warningCount = getWarningCount(player, true);
        if (warningCount == 0 || count < 0) return false;
        return count >= warningCount;
    }

    public int getWarningCount(UUID player, boolean returnZeroIfMissing) {
        if (!warnings.containsKey(player)) {
            if(!returnZeroIfMissing) return 1;
            return 0;
        }
        return warnings.get(player).size();
    }

    public Warning getWarning(UUID player, int position) {
        if (!hasWarningCount(player, position)) return null;
        return warnings.get(player).get(position - 1);
    }

    public List<Warning> getWarnings(UUID player) {
        if (!warnings.containsKey(player)) return new ArrayList<>();
        return warnings.get(player);
    }

    public void removeWarning(UUID player, int position, boolean saveEdit) {
        if (!hasWarningCount(player, position)) return;
        if (!warnings.containsKey(player)) return;
        if (saveEdit) {
            Warning warningFromList = getWarning(player, position);
            removeWarningFromConfig(player, warningFromList);
        }
        warnings.get(player).remove(position - 1);
    }

    public void addWarning(UUID player, Warning warning, boolean saveEdit) {
        if (!warnings.containsKey(player)) warnings.put(player, new ArrayList<>());
        if (hasWarningCount(player, warning.getWarningCount())) warning.setWarningCount(getWarningCount(player, true) + 1);

        warnings.get(player).add(warning);
        if (saveEdit) {
            saveWarningToConfig(player, warning);
        }
    }

    private void saveWarningToConfig(UUID player, Warning warning) {
        warningConfig.set("WarningCount." + player.toString(), getWarningCount(player, true));
        String path = "Warnings." + player + "." + warning.getWarningCount() + ".";
        warningConfig.set(path + "playerUUID", player.toString());
        warningConfig.set(path + "warningCount", warning.getWarningCount());
        warningConfig.set(path + "creationDate", warning.getCreationDate());
        warningConfig.set(path + "expirationDate", warning.getExpirationDate());
        warningConfig.set(path + "reason", warning.getReason());
        try {
            warningConfig.save(warningFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO Edit Methods (setReason, setExpirationDate, etc.)

    private void removeWarningFromConfig(UUID player, Warning warning) {
        if (warningConfig.contains("WarningCount." + player.toString()) &&
                warningConfig.isConfigurationSection("Warnings." + player + "." + warning.getWarningCount())) {
            warningConfig.set("WarningCount." + player, warningConfig.getInt("WarningCount." + player) - 1);
            warningConfig.set("Warnings." + player + "." + warning.getWarningCount(), null);
            try {
                warningConfig.save(warningFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadWarnings() {
        if (!warningConfig.isConfigurationSection("Warnings")) warningConfig.createSection("Warnings");

        warningConfig.getConfigurationSection("Warnings").getKeys(false).forEach(player -> {
            String path = "Warnings." + player;
            if (warningConfig.isConfigurationSection(path)) {
                for (String count : warningConfig.getConfigurationSection(path).getKeys(false)) {
                    String tempPath = path + "." + count;
                    Warning warning = new Warning(
                            UUID.fromString(player),
                            warningConfig.getString(tempPath + "reason"),
                            warningConfig.getLong(tempPath + "expirationDate"),
                            warningConfig.getLong(tempPath + "creationDate"),
                            warningConfig.getInt(tempPath + "warningCount")
                    );
                    addWarning(UUID.fromString(player), warning, false);
                }
                warningConfig.set("WarningCount." + player, getWarningCount(UUID.fromString(player), true));
            }
        });
        try {
            warningConfig.save(warningFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveWarnings() {
        for(UUID player : warnings.keySet()) {
            if(warnings.get(player).isEmpty()) continue;
            for(Warning warning : warnings.get(player)) {
                saveWarningToConfig(player, warning);
            }
        }
    }

    public Map<UUID, List<Warning>> getWarnings() {
        return warnings;
    }

    public AdminPanelMain getPlugin() {
        return plugin;
    }

    public File getWarningFile() {
        return warningFile;
    }

    public FileConfiguration getWarningConfig() {
        return warningConfig;
    }
}
