package de.happybavarian07.adminpanel.commands.managers;/*
 * @Author HappyBavarian07
 * @Date 17.06.2022 | 21:38
 */

import de.happybavarian07.adminpanel.commands.subcommands.adminpaneladmin.*;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.PluginUtils;
import de.happybavarian07.coolstufflib.commandmanagement.CommandData;
import de.happybavarian07.coolstufflib.commandmanagement.CommandManager;
import de.happybavarian07.coolstufflib.commandmanagement.HelpCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

@CommandData
public class AdminPanelAdminManager extends CommandManager {
    @Override
    public String getCommandName() {
        return "adminpaneladmin";
    }

    @Override
    public String getCommandUsage() {
        return "/adminpaneladmin <reload|reloadconfig|reloadlang|reloaddata>";
    }

    @Override
    public String getCommandInfo() {
        return "The Admin Command to reload Stuff";
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return AdminPanelMain.getPlugin();
    }

    @Override
    public List<String> getCommandAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add("apadmin");
        aliases.add("adminpadmin");
        aliases.add("apaneladmin");
        return aliases;
    }

    @Override
    public String getCommandPermissionAsString() {
        return "AdminPanel.AdminPanelAdmin";
    }

    @Override
    public boolean autoRegisterPermission() {
        return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        return super.onCommand(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return super.onTabComplete(sender, command, label, args);
    }

    @Override
    public void setup() {
        registerSubCommand(new HelpCommand(getCommandName()));

        AdminPanelMain plugin = AdminPanelMain.getPlugin();
        String perm = "AdminPanel.AdminPanelAdminCommands.";
        registerSubCommand(new AdminActionCommand(getCommandName(), "reloadconfig", new String[0], perm + "ReloadConfig",
                "ReloadConfig", false, sender -> plugin.reloadConfig()));
        registerSubCommand(new AdminActionCommand(getCommandName(), "reloaddata", new String[0], perm + "ReloadData",
                "ReloadData", false, sender -> {
                    if (!plugin.reloadDataYML()) throw new IllegalStateException("data.yml could not be read");
                }));
        registerSubCommand(new AdminActionCommand(getCommandName(), "reloadlang", new String[0], perm + "ReloadLang",
                "ReloadLang", false, sender -> lgm.reloadLanguages(sender, true)));
        registerSubCommand(new AdminActionCommand(getCommandName(), "reload", new String[]{"reloadplugin"}, perm + "ReloadPlugin",
                "ReloadPlugin", true, sender -> new PluginUtils().reload(plugin)));
        registerSubCommand(new AdminActionCommand(getCommandName(), "reloadcommandmanagers", new String[]{"reloadcm"}, perm + "ReloadCommandManagers",
                "ReloadCommandManagers", false, sender -> {
                    plugin.getCommandManagerRegistry().unregisterAll();
                    plugin.getInitMethods().initCommandManagers();
                }));
        registerSubCommand(new AdminActionCommand(getCommandName(), "updateconfig", new String[0], perm + "UpdateConfig",
                "UpdateConfig", false, sender -> plugin.updateConfig()));
        registerSubCommand(new AdminActionCommand(getCommandName(), "updatelangfiles", new String[]{"updatelang"}, perm + "UpdateLangFiles",
                "UpdateLangFiles", false, sender -> {
                    lgm.updateLangFiles();
                    lgm.reloadLanguages(sender, true);
                }));
        registerSubCommand(new StartUpLoggerControlCommand(getCommandName()));
        registerSubCommand(new BackupManagerCommand(getCommandName()));
        registerSubCommand(new PermissionMenuHelpCommand(getCommandName()));
        registerSubCommand(new StaffChatCommand(getCommandName()));
        registerSubCommand(new StartupStatusCommand(getCommandName()));
    }
}
