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
@SubCommandInfo(name = "reload", info = "Disables and enables an Addon again.", permission = "AdminPanel.AddonManageCMD.Reload")
public class AddonReloadCommand extends AddonSubCommand {
    public AddonReloadCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public List<Argument> arguments() {
        return List.of(addonArgument(addon -> true));
    }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        Addon addon = addon(args, 0, "Reload");
        if (addon == null) return true;
        AddonLoader loader = loader();
        loader.disableAddon(addon.getFile());
        AddonLoader.EnableResult result;
        try {
            result = loader.enableAddon(addon.getFile(), new HashSet<>(), true);
        } catch (IOException | ClassNotFoundException e) {
            AdminPanelMain.getPlugin().getFileLogger().writeToLog(Level.SEVERE, "Could not reload addon " + addon.getName() + ": " + e, LogPrefixExtension.ADMINPANEL_MAIN, true);
            result = AddonLoader.EnableResult.ERROR;
        }
        send(sender, switch (result) {
            case SUCCESS -> "AddonCommand.Reload.ReloadedMessage";
            case ALREADY_ENABLED -> "AddonCommand.Reload.AddonAlreadyEnabled";
            case NULL_ADDON -> "AddonCommand.Reload.AddonNotFound";
            default -> "AddonCommand.Reload.ErrorReloadingAddon";
        }, addon);
        return true;
    }
}
