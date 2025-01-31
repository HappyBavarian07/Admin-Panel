package de.happybavarian07.adminpanel.commands.subcommands.addoncommands;

import de.happybavarian07.adminpanel.addonloader.api.Addon;
import de.happybavarian07.adminpanel.addonloader.loadingutils.AddonLoader;
import de.happybavarian07.adminpanel.commandmanagement.CommandData;
import de.happybavarian07.adminpanel.commandmanagement.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.*;

/*
 * @Author HappyBavarian07
 * @Date September 08, 2024 | 13:59
 */
@CommandData
public class AddonEnableCommand extends SubCommand {
    public AddonEnableCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Player playerOrNull, String[] args) {
        if(args.length != 1) {
            return false;
        }
        String addonName = args[0];
        if(plugin.getAddonLoader().getAddonMainClassByName(addonName) == null) {
            sender.sendMessage(lgm.getMessage("AddonCommand.Enable.AddonNotFound", playerOrNull, false).replace("%addon_name%", addonName));
            return false;
        }
        Addon addon = plugin.getAddonLoader().getAddonMainClassByName(addonName);
        AddonLoader.EnableResult result;
        try {
            result = plugin.getAddonLoader().enableAddon(addon.getFile(), new HashSet<>());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        switch (result) {
            case SUCCESS:
                sender.sendMessage(replaceAddonPlaceholders(lgm.getMessage("AddonCommand.Enable.EnabledMessage", playerOrNull, false), addon));
                break;
            case DEPENDENCY_MISSING:
                sender.sendMessage(replaceAddonPlaceholders(lgm.getMessage("AddonCommand.Enable.DependencyError", playerOrNull, false), addon));
                break;
            case ALREADY_ENABLED:
                sender.sendMessage(replaceAddonPlaceholders(lgm.getMessage("AddonCommand.Enable.AddonAlreadyEnabled", playerOrNull, false), addon));
                break;
            case ERROR:
                sender.sendMessage(replaceAddonPlaceholders(lgm.getMessage("AddonCommand.Enable.ErrorEnablingAddon", playerOrNull, false), addon));
                break;
            case NULL_ADDON:
                sender.sendMessage(lgm.getMessage("AddonCommand.Enable.AddonNotFound", playerOrNull, false).replace("%addon_name%", addonName));
                break;
            case CIRCULAR_DEPENDENCY:
                sender.sendMessage(replaceAddonPlaceholders(lgm.getMessage("AddonCommand.Enable.CircularDependency", playerOrNull, false), addon));
                break;
        }
        return true;
    }

    @Override
    public String name() {
        return "enable";
    }

    @Override
    public String info() {
        return "Enables an Addon.";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public Map<Integer, String[]> subArgs(CommandSender sender, int isPlayer, String[] args) {
        Map<Integer, String[]> subArgs = new HashMap<>();
        List<String> addonList = new ArrayList<>();
        plugin.getAddonLoader().getLoadedAddons().forEach((addon) -> {
            if (!addon.isEnabled()) {
                addonList.add(addon.getName());
            }
        });
        subArgs.put(1, addonList.toArray(new String[0]));
        return subArgs;
    }

    @Override
    public String syntax() {
        return "/" + mainCommandName + " " + name() + " <Addon>";
    }

    @Override
    public String permissionAsString() {
        return "AdminPanel.AddonManageCMD.Enable";
    }

    @Override
    public boolean autoRegisterPermission() {
        return true;
    }

    public String replaceAddonPlaceholders(String message, Addon addon) {
        // %addon_name%, %addon_version%, %addon_description%, %addon_dependencies%
        return message.replace("%addon_name%", addon.getName())
                .replace("%addon_version%", addon.getVersion())
                .replace("%addon_description%", addon.getDescription())
                .replace("%addon_dependencies%", addon.getDependencies().toString());
    }
}
