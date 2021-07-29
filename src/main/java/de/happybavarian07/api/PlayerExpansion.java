package de.happybavarian07.api;

import de.happybavarian07.main.Main;
import de.happybavarian07.main.Utils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "ap";
    }

    @Override
    public @NotNull String getAuthor() {
        return "HappyBavarian07";
    }

    @Override
    public @NotNull String getVersion() { return "3.2"; }

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
        if(player == null) {
            return null;
        }

        if(params.equals("player-balance")) {
            return String.valueOf(Utils.getInstance().getEconomy().getBalance(player));
        }
        if(params.equals("player-world")) {
            return String.valueOf(Utils.getInstance().getEconomy().getBalance(player));
        }
        if(params.equals("player-x")) {
            return String.valueOf(Utils.getInstance().getEconomy().getBalance(player));
        }
        if(params.equals("player-y")) {
            return String.valueOf(Utils.getInstance().getEconomy().getBalance(player));
        }
        if(params.equals("player-z")) {
            return String.valueOf(Utils.getInstance().getEconomy().getBalance(player));
        }
        return null;
    }
}
