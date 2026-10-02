package de.happybavarian07.adminpanel.commands;

import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.coolstufflib.languagemanager.LanguageManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Sends language messages with placeholders replaced in the text itself, so a command that fails half way
 * leaves no values behind in the language manager's shared placeholder map.
 */
public final class CommandText {
    private CommandText() {
    }

    /** {@code placeholders} are pairs: key, value, key, value, ... Values may use {@code &} color codes. */
    public static String get(LanguageManager lgm, CommandSender sender, String key, Object... placeholders) {
        String text = lgm.getMessage(key, sender instanceof Player player ? player : null, false)
                .replace("\\r\\n", "\n").replace("\\n", "\n");
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            text = text.replace(String.valueOf(placeholders[i]), AdminPanelUtils.chat(String.valueOf(placeholders[i + 1])));
        }
        return text;
    }

    public static void send(LanguageManager lgm, CommandSender sender, String key, Object... placeholders) {
        sender.sendMessage(get(lgm, sender, key, placeholders));
    }
}
