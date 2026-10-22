package de.happybavarian07.adminpanel.commands.managers;

import de.happybavarian07.adminpanel.commands.subcommands.perms.PermsCommands;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.coolstufflib.commandmanagement.CommandData;
import de.happybavarian07.coolstufflib.commandmanagement.CommandManager;
import de.happybavarian07.coolstufflib.commandmanagement.HelpCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/** /perms: users, groups, checks, search, export and import of the permission manager. */
@CommandData
public class PermsManager extends CommandManager {
    public static final String PERMISSION = "AdminPanel.Permissions.Command";

    @Override
    public String getCommandName() {
        return "perms";
    }

    @Override
    public String getCommandUsage() {
        return "/perms <user|group|groups|check|explain|search|audit|export|import|reload|menu>";
    }

    @Override
    public String getCommandInfo() {
        return "Manages groups and permissions of players";
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return AdminPanelMain.getPlugin();
    }

    @Override
    public List<String> getCommandAliases() {
        return List.of("apperms", "permissions");
    }

    @Override
    public String getCommandPermissionAsString() {
        return PERMISSION;
    }

    @Override
    public boolean autoRegisterPermission() {
        return false;
    }

    @Override
    public void setup() {
        registerSubCommand(new HelpCommand(getCommandName()));
        PermsCommands.all(getCommandName()).forEach(this::registerSubCommand);
    }
}
