package de.happybavarian07.adminpanel.data;

import de.happybavarian07.adminpanel.warnings.WarningStatus;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class LegacyWarningImporter {
    private LegacyWarningImporter() {
    }

    /**
     * Reads every warning of the file, oldest first. Warnings keep their dates, and a warning whose expiration date
     * is not after {@code now} becomes {@code EXPIRED}. The old file did not store an issuer, so the console is used.
     *
     * @param warningFile the old warnings.yml
     * @param now         the current time in epoch milliseconds
     */
    public static List<PlayerWarning> read(File warningFile, long now) {
        List<PlayerWarning> warnings = new ArrayList<>();
        ConfigurationSection players = YamlConfiguration.loadConfiguration(warningFile).getConfigurationSection("Warnings");
        if (players == null) return warnings;
        for (String playerKey : players.getKeys(false)) {
            UUID player;
            try {
                player = UUID.fromString(playerKey);
            } catch (IllegalArgumentException notAUuid) {
                continue;
            }
            ConfigurationSection entries = players.getConfigurationSection(playerKey);
            if (entries == null) continue;
            for (String entryKey : entries.getKeys(false)) {
                ConfigurationSection entry = entries.getConfigurationSection(entryKey);
                if (entry == null) continue;
                long expires = Math.max(0, entry.getLong("expirationDate"));
                boolean expired = expires > 0 && expires <= now;
                warnings.add(new PlayerWarning(player.toString(), entry.getString("reason", ""), PlayerWarning.CONSOLE,
                        entry.getLong("creationDate"), expires,
                        expired ? WarningStatus.EXPIRED : WarningStatus.ACTIVE,
                        expired ? expires : 0, expired ? PlayerWarning.SYSTEM : ""));
            }
        }
        warnings.sort(Comparator.comparingLong(PlayerWarning::getCreatedAt));
        return warnings;
    }
}
