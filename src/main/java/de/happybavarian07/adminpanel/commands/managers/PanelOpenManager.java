package de.happybavarian07.adminpanel.commands.managers;/*
 * @Author HappyBavarian07
 * @Date 27.04.2022 | 17:07
 */

import de.happybavarian07.adminpanel.commands.subcommands.panels.MenuOpenCommand;
import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.adminpanel.menusystem.menu.TempLanguageSelectMenu;
import de.happybavarian07.adminpanel.menusystem.menu.playermanager.BannedPlayersMenu;
import de.happybavarian07.adminpanel.menusystem.menu.playermanager.PlayerSelectMenu;
import de.happybavarian07.adminpanel.menusystem.menu.pluginmanager.PluginAutoUpdaterMenu;
import de.happybavarian07.adminpanel.menusystem.menu.pluginmanager.PluginInstallMenu;
import de.happybavarian07.adminpanel.menusystem.menu.pluginmanager.PluginSelectMenu;
import de.happybavarian07.adminpanel.menusystem.menu.servermanager.ChatManagerMenu;
import de.happybavarian07.adminpanel.menusystem.menu.servermanager.ServerManagerMenu;
import de.happybavarian07.adminpanel.menusystem.menu.servermanager.WhitelistManagerMenu;
import de.happybavarian07.adminpanel.menusystem.menu.servermanager.WhitelistedPlayersMenu;
import de.happybavarian07.adminpanel.menusystem.menu.worldmanager.WorldCreateMenu;
import de.happybavarian07.adminpanel.menusystem.menu.worldmanager.WorldSelectMenu;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.coolstufflib.commandmanagement.CommandData;
import de.happybavarian07.coolstufflib.commandmanagement.CommandManager;
import de.happybavarian07.coolstufflib.commandmanagement.HelpCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

@CommandData
public class PanelOpenManager extends CommandManager {
    @Override
    public String getCommandName() {
        return "openpanel";
    }

    @Override
    public String getCommandUsage() {
        /*
         */
        return "/openpanel <SubCommand> (/openpanel help <Page>)";
    }

    @Override
    public String getCommandInfo() {
        return "Open specific Panels for you or other Players";
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return AdminPanelMain.getPlugin();
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getCommandPermissionAsString() {
        return "";
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
        registerSubCommand(new MenuOpenCommand(getCommandName(), "BannedPlayersMenu", pmu -> new BannedPlayersMenu(pmu, null)));
        registerSubCommand(new MenuOpenCommand(getCommandName(), "PlayerSelectMenu", pmu -> new PlayerSelectMenu(pmu, null)));
        registerSubCommand(new MenuOpenCommand(getCommandName(), "PluginAutoUpdaterMenu", PluginAutoUpdaterMenu::new));
        registerSubCommand(new MenuOpenCommand(getCommandName(), "PluginInstallMenu", PluginInstallMenu::new));
        registerSubCommand(new MenuOpenCommand(getCommandName(), "PluginSelectMenu", pmu -> new PluginSelectMenu(pmu, null)));
        registerSubCommand(new MenuOpenCommand(getCommandName(), "ChatManagerMenu", ChatManagerMenu::new));
        registerSubCommand(new MenuOpenCommand(getCommandName(), "ServerManagerMenu", ServerManagerMenu::new));
        registerSubCommand(new MenuOpenCommand(getCommandName(), "WhitelistedPlayersMenu", pmu -> new WhitelistedPlayersMenu(pmu, null)));
        registerSubCommand(new MenuOpenCommand(getCommandName(), "WhitelistManagerMenu", WhitelistManagerMenu::new));
        registerSubCommand(new MenuOpenCommand(getCommandName(), "WorldCreateMenu", WorldCreateMenu::new));
        registerSubCommand(new MenuOpenCommand(getCommandName(), "WorldSelectMenu", pmu -> new WorldSelectMenu(pmu, null)));
        registerSubCommand(new MenuOpenCommand(getCommandName(), "AdminPanelStartMenu", AdminPanelStartMenu::new));
        registerSubCommand(new MenuOpenCommand(getCommandName(), "TempLanguageSelectMenu", pmu -> new TempLanguageSelectMenu(pmu, null)));
    }
}
