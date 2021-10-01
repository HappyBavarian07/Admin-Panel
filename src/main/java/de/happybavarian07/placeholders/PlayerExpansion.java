package de.happybavarian07.placeholders;

import de.happybavarian07.main.AdminPanelMain;
import de.happybavarian07.utils.Utils;
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

        if (params.equals("balance")) {
            return String.valueOf(Utils.getInstance().getEconomy().getBalance(player));
        }
        if (params.equals("world")) {
            return player.getWorld().getName();
        }
        if (params.equals("displayname")) {
            return player.getDisplayName();
        }
        if (params.equals("name")) {
            return player.getName();
        }
        if (params.equals("x")) {
            return String.valueOf((int) player.getLocation().getX());
        }
        if (params.equals("y")) {
            return String.valueOf((int) player.getLocation().getY());
        }
        if (params.equals("z")) {
            return String.valueOf((int) player.getLocation().getZ());
        }
        if (params.equals("isbanned")) {
            return String.valueOf(player.isBanned() && AdminPanelMain.getPlugin().getBanConfig().getBoolean(player.getUniqueId().toString()));
        }
        return null;
    }
}
