package de.happybavarian07.adminpanel.commands.subcommands.addoncommands;

import de.happybavarian07.adminpanel.addonloader.api.Addon;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.coolstufflib.commandmanagement.Argument;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommandInfo;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/*
 * @Author HappyBavarian07
 * @Date September 08, 2024 | 13:59
 */
@SubCommandInfo(name = "info", info = "Shows information about an Addon.", permission = "AdminPanel.AddonManageCMD.Info")
public class AddonInfoCommand extends AddonSubCommand {
    public AddonInfoCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public List<Argument> arguments() {
        return List.of(addonArgument(addon -> true));
    }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        Addon addon = addon(args, 0, "Info");
        if (addon == null) return true;
        Player player = sender instanceof Player p ? p : null;
        List<String> lines = lgm.getCustomObject("AddonCommand.Info.AddonInfoMessage", player, new ArrayList<>(), false);
        for (String line : lines) {
            String text = lgm.replacePlaceholders(PlaceholderType.MESSAGE, format(line, addon));
            sender.sendMessage(AdminPanelUtils.format(player, text, AdminPanelMain.getPrefix()));
        }
        return true;
    }
}
