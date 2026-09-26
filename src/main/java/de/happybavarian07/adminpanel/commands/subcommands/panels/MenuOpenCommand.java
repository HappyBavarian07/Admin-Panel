package de.happybavarian07.adminpanel.commands.subcommands.panels;

import de.happybavarian07.coolstufflib.CoolStuffLib;
import de.happybavarian07.coolstufflib.commandmanagement.Argument;
import de.happybavarian07.coolstufflib.commandmanagement.CommandArgs;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommand;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Function;

/** Opens one menu for the sender or, with {@code AdminPanel.open.other}, for another online player. */
public class MenuOpenCommand extends SubCommand {
    public static final String OPEN_OTHER_PERMISSION = "AdminPanel.open.other";

    private final String name;
    private final String permission;
    private final Function<PlayerMenuUtility, ? extends Menu> factory;

    public MenuOpenCommand(String mainCommandName, String name, String permission, Function<PlayerMenuUtility, ? extends Menu> factory) {
        super(mainCommandName);
        this.name = name;
        this.permission = permission;
        this.factory = factory;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String info() {
        return "Opens the " + name;
    }

    @Override
    public String permissionAsString() {
        return permission;
    }

    @Override
    public boolean autoRegisterPermission() {
        return false;
    }

    @Override
    public List<Argument> arguments() {
        return List.of(Argument.player("player").optional());
    }

    @Override
    public boolean execute(CommandSender sender, CommandArgs args) {
        Player self = sender instanceof Player player ? player : null;
        Player target = args.has(0) ? args.onlinePlayer(0) : args.player();
        boolean forOther = !target.equals(self);
        if (forOther && !sender.hasPermission(OPEN_OTHER_PERMISSION)) {
            sender.sendMessage(lgm.getMessage("Player.General.NoPermissions", self, true));
            return true;
        }
        factory.apply(CoolStuffLib.getLib().getPlayerMenuUtility(target.getUniqueId())).open();
        lgm.addPlaceholder(PlaceholderType.MESSAGE, "%target%", target.getName(), false);
        sender.sendMessage(lgm.getMessage(forOther ? "Player.General.OpeningMessageSelfForOther" : "Player.General.OpeningMessageSelf", self, true));
        return true;
    }
}
