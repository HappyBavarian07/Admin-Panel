package de.happybavarian07.adminpanel.commands.subcommands.addoncommands;

import de.happybavarian07.adminpanel.addonloader.api.Addon;
import de.happybavarian07.adminpanel.addonloader.loadingutils.AddonLoader;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.LogPrefixExtension;
import de.happybavarian07.coolstufflib.commandmanagement.Argument;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommandInfo;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

/*
 * @Author HappyBavarian07
 * @Date September 08, 2024 | 13:59
 */
@SubCommandInfo(name = "enable", info = "Enables an Addon.", permission = "AdminPanel.AddonManageCMD.Enable")
public class AddonEnableCommand extends AddonSubCommand {
    public AddonEnableCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public List<Argument> arguments() {
        return List.of(addonArgument(addon -> !addon.isEnabled()));
    }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        Addon addon = addon(args, 0, "Enable");
        if (addon == null) return true;
        AddonLoader.EnableResult result;
        try {
            result = loader().enableAddon(addon.getFile(), new HashSet<>(), true);
        } catch (IOException | ClassNotFoundException e) {
            AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.SEVERE, "Could not enable addon " + addon.getName() + ": " + e, LogPrefixExtension.ADMINPANEL_MAIN, true);
            result = AddonLoader.EnableResult.ERROR;
        }
        send(sender, switch (result) {
            case SUCCESS -> "AddonCommand.Enable.EnabledMessage";
            case DEPENDENCY_MISSING -> "AddonCommand.Enable.DependencyError";
            case ALREADY_ENABLED -> "AddonCommand.Enable.AddonAlreadyEnabled";
            case CIRCULAR_DEPENDENCY -> "AddonCommand.Enable.CircularDependencyError";
            case NULL_ADDON -> "AddonCommand.Enable.AddonNotFound";
            case ERROR -> "AddonCommand.Enable.ErrorEnablingAddon";
        }, addon);
        return true;
    }
}
