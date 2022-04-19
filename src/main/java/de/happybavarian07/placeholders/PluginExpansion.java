package de.happybavarian07.placeholders;

import de.happybavarian07.main.AdminPanelMain;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PluginExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "applugin";
    }

    @Override
    public @NotNull String getAuthor() {
        return "HappyBavarian07";
    }

    @Override
    public @NotNull String getVersion() {
        return AdminPanelMain.getPlugin().getDescription().getVersion();
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return null;
        }
        if (params.equals("prefix")) {
            return AdminPanelMain.getPrefix();
        }
        if (params.startsWith("message-")) {
            return AdminPanelMain.getPlugin().getLanguageManager().getMessage(params.substring(8), player, true);
        }
        return null;
    }
}
