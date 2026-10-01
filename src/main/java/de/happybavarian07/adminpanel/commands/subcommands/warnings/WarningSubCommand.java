package de.happybavarian07.adminpanel.commands.subcommands.warnings;

import de.happybavarian07.adminpanel.menusystem.menu.FeatureGate;
import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.warnings.WarningManager;
import de.happybavarian07.coolstufflib.commandmanagement.Argument;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

abstract class WarningSubCommand extends SubCommand {
    static final String KEY = "Warnings.";

    WarningSubCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    protected String defaultSyntax() {
        StringBuilder syntax = new StringBuilder("/").append(mainCommandName);
        for (Argument argument : arguments()) syntax.append(' ').append(argument.usage());
        return syntax.toString();
    }

    static Argument playerArgument() {
        return Argument.custom("player", sender -> Arrays.stream(Bukkit.getOfflinePlayers())
                .map(OfflinePlayer::getName).filter(Objects::nonNull).distinct().toList());
    }

    WarningManager manager(CommandSender sender) {
        WarningManager manager = AdminPanelMain.getPlugin().getWarningManager();
        if (manager == null) {
            if (FeatureGate.broken("warnings")) FeatureGate.sendUnavailable(sender, "warnings");
            else CommandText.send(lgm, sender, KEY + "Disabled");
        }
        return manager;
    }

    OfflinePlayer target(CommandSender sender, String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) return online;
        for (OfflinePlayer known : Bukkit.getOfflinePlayers()) {
            if (name.equalsIgnoreCase(known.getName())) return known;
        }
        CommandText.send(lgm, sender, KEY + "PlayerNotFound", "%target%", name);
        return null;
    }

    static UUID issuer(CommandSender sender) {
        return sender instanceof Player player ? player.getUniqueId() : null;
    }

    Function<Throwable, Void> failed(CommandSender sender) {
        return error -> {
            CommandText.send(lgm, sender, KEY + "Error");
            AdminPanelMain.logError("A warning command failed", error);
            return null;
        };
    }
}
