package de.happybavarian07.adminpanel.commands.subcommands.addoncommands;

import de.happybavarian07.adminpanel.addonloader.api.Addon;
import de.happybavarian07.coolstufflib.commandmanagement.Argument;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommandInfo;
import org.bukkit.command.CommandSender;

import java.util.List;

/*
 * @Author HappyBavarian07
 * @Date September 08, 2024 | 13:59
 */
@SubCommandInfo(name = "disable", info = "Disables an Addon.", permission = "AdminPanel.AddonManageCMD.Disable")
public class AddonDisableCommand extends AddonSubCommand {
    public AddonDisableCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public List<Argument> arguments() {
        return List.of(addonArgument(Addon::isEnabled));
    }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        Addon addon = addon(args, 0, "Disable");
        if (addon == null) return true;
        send(sender, switch (loader().disableAddon(addon.getFile())) {
            case SUCCESS -> "AddonCommand.Disable.DisabledMessage";
            case ALREADY_ENABLED -> "AddonCommand.Disable.AddonAlreadyDisabled";
            case NULL_ADDON -> "AddonCommand.Disable.AddonNotFound";
            default -> "AddonCommand.Disable.ErrorDisablingAddon";
        }, addon);
        return true;
    }
}
