package de.happybavarian07.adminpanel.commands.subcommands.disableditems;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommand;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

abstract class DisabledItemsSubCommand extends SubCommand {
    static final String KEY = "DisabledItems.";
    static final List<String> ITEM_NAMES = Arrays.stream(Material.values())
            .filter(material -> material.isItem() && !material.isAir() && !material.name().startsWith("LEGACY_"))
            .map(Material::name)
            .toList();

    DisabledItemsSubCommand(String mainCommandName) {
        super(mainCommandName);
    }

    /** The item named by argument {@code index}, or sends {@code DisabledItems.InvalidMaterial}. */
    Material material(CommandArgs args, int index) {
        String name = args.string(index);
        Material material = Material.matchMaterial(name);
        if (material == null || !material.isItem() || material.isAir()) {
            CommandText.send(lgm, args.sender(), KEY + "InvalidMaterial", "%material%", name);
            return null;
        }
        return material;
    }
}
