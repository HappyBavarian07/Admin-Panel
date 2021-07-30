package de.happybavarian07.placeholders;

import de.happybavarian07.main.Main;
import de.happybavarian07.main.Utils;
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
        return "3.2";
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
            return Main.getPrefix();
        }
        if (params.startsWith("message-")) {
            if (Main.getMessages().contains(params.substring(8))) {
                return Utils.getInstance().replacePlaceHolders(player, Main.getMessages().getString(params.substring(8)), Main.getPrefix());
            } else {
                return "";
            }
        }
        return null;
    }
}
