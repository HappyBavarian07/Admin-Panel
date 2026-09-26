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

@SubCommandInfo(name = "enable", info = "Allows a disabled item again.",
        permission = "AdminPanel.DisabledItems.Enable", autoRegisterPermission = false)
public class EnableCommand extends DisabledItemsSubCommand {
    public EnableCommand(String mainCommandName) {
        super(mainCommandName);
    }

    @Override
    public List<Argument> arguments() {
        return List.of(Argument.custom("material", sender -> AdminPanelUtils.getDisabledItems().stream().map(Material::name).toList()));
    }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        Material material = material(args, 0);
        if (material == null) return true;
        boolean changed = AdminPanelUtils.enableVanillaItem(new ItemStack(material));
        CommandText.send(lgm, sender, changed ? KEY + "Enabled" : KEY + "NotDisabled", "%material%", material.name());
        return true;
    }
}
