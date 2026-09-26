package de.happybavarian07.adminpanel.commands.subcommands.addoncommands;

import de.happybavarian07.adminpanel.addonloader.api.Addon;
import de.happybavarian07.adminpanel.addonloader.loadingutils.AddonLoader;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.coolstufflib.commandmanagement.Argument;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgumentException;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/** Shared lookup and messages for the /addon sub commands. */
abstract class AddonSubCommand extends SubCommand {
    AddonSubCommand(String mainCommandName) {
        super(mainCommandName);
    }

    static AddonLoader loader() {
        AddonLoader loader = AdminPanelMain.getPlugin().getAddonLoader();
        if (loader == null) {
            throw new CommandArgumentException("AddonCommand.SystemDisabled", "%prefix% &9> &cThe Addon System is disabled!", Map.of());
        }
        return loader;
    }

    static Argument addonArgument(Predicate<Addon> filter) {
        return Argument.custom("addon", sender -> {
            AddonLoader loader = AdminPanelMain.getPlugin().getAddonLoader();
            if (loader == null) return List.of();
            return loader.getLoadedAddons().stream().filter(filter).map(Addon::getName).toList();
        });
    }

    /** The addon named by argument {@code index}, or sends {@code AddonCommand.<action>.AddonNotFound}. */
    Addon addon(CommandArgs args, int index, String action) {
        String name = args.string(index);
        Addon addon = loader().getAddonMainClassByName(name);
        if (addon == null) {
            args.sender().sendMessage(message(args.sender(), "AddonCommand." + action + ".AddonNotFound").replace("%addon_name%", name));
        }
        return addon;
    }

    void send(CommandSender sender, String key, Addon addon) {
        sender.sendMessage(format(message(sender, key), addon));
    }

    String message(CommandSender sender, String key) {
        return lgm.getMessage(key, sender instanceof Player player ? player : null, false);
    }

    static String format(String message, Addon addon) {
        return message.replace("%addon_name%", addon.getName())
                .replace("%addon_version%", addon.getVersion())
                .replace("%addon_description%", addon.getDescription())
                .replace("%addon_dependencies%", addon.getDependencies().toString())
                .replace("%addon_enabled%", String.valueOf(addon.isEnabled()));
    }
}
