package de.happybavarian07.adminpanel.warnings;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.data.PlayerWarning;
import de.happybavarian07.coolstufflib.languagemanager.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public final class WarningText {
    private WarningText() {
    }

    public static String date(LanguageManager lgm, CommandSender viewer, long millis) {
        return new SimpleDateFormat(CommandText.get(lgm, viewer, "Warnings.Format.Date")).format(new Date(millis));
    }

    public static String expires(LanguageManager lgm, CommandSender viewer, PlayerWarning warning) {
        return warning.getExpiresAt() <= 0
                ? CommandText.get(lgm, viewer, "Warnings.Format.Never")
                : date(lgm, viewer, warning.getExpiresAt());
    }

    public static String status(LanguageManager lgm, CommandSender viewer, PlayerWarning warning) {
        return CommandText.get(lgm, viewer, "Warnings.Status." + warning.getStatus().name());
    }

    public static String actor(LanguageManager lgm, CommandSender viewer, String value) {
        if (value == null || value.isEmpty()) return CommandText.get(lgm, viewer, "Warnings.Format.Unknown");
        if (value.equals(PlayerWarning.CONSOLE)) return CommandText.get(lgm, viewer, "Warnings.Format.Console");
        if (value.equals(PlayerWarning.SYSTEM)) return CommandText.get(lgm, viewer, "Warnings.Format.System");
        try {
            String name = Bukkit.getOfflinePlayer(UUID.fromString(value)).getName();
            return name != null ? name : value;
        } catch (IllegalArgumentException notAUuid) {
            return value;
        }
    }
}
