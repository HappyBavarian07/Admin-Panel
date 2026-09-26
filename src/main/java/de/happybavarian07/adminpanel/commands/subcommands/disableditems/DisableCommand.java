package de.happybavarian07.adminpanel.commands.subcommands.disableditems;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.coolstufflib.commandmanagement.Argument;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommandInfo;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@SubCommandInfo(name = "disable", info = "Blocks an item for players without AdminPanel.Bypass.ItemDisable.",
        permission = "AdminPanel.DisabledItems.Disable", autoRegisterPermission = false)
public class DisableCommand extends DisabledItemsSubCommand {
    public DisableCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public List<Argument> arguments() {
        return List.of(Argument.custom("material", sender -> ITEM_NAMES));
    }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        Material material = material(args, 0);
        if (material == null) return true;
        boolean changed = AdminPanelUtils.disableVanillaItem(new ItemStack(material));
        CommandText.send(lgm, sender, changed ? KEY + "Disabled" : KEY + "AlreadyDisabled", "%material%", material.name());
        return true;
    }
}
