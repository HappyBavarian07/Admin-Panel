package de.happybavarian07.adminpanel.placeholders;

import de.happybavarian07.adminpanel.utils.Utils;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "applayer";
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

        return switch (params) {
            case "balance" -> String.valueOf(Utils.getInstance().getEconomy().getBalance(player));
            case "world" -> player.getWorld().getName();
            case "displayname" -> player.getDisplayName();
            case "name" -> player.getName();
            case "x" -> String.valueOf((int) player.getLocation().getX());
            case "y" -> String.valueOf((int) player.getLocation().getY());
            case "z" -> String.valueOf((int) player.getLocation().getZ());
            default -> null;
        };
    }
}
