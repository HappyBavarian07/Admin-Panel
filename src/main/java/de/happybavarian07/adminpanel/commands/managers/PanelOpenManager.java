package de.happybavarian07.adminpanel.commands.managers;/*
 * @Author HappyBavarian07
 * @Date 27.04.2022 | 17:07
 */

import de.happybavarian07.adminpanel.menusystem.menu.JournalMenu;
import de.happybavarian07.adminpanel.menusystem.menu.StartupStatusMenu;
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
        registerSubCommand(new MenuOpenCommand(getCommandName(), "BannedPlayersMenu", "AdminPanel.PlayerManager.BannedPlayers",
                pmu -> new BannedPlayersMenu(pmu, null)));
        registerSubCommand(new MenuOpenCommand(getCommandName(), "PlayerSelectMenu", "AdminPanel.PlayerManager.open",
                pmu -> new PlayerSelectMenu(pmu, null)));
        registerSubCommand(new MenuOpenCommand(getCommandName(), "PluginAutoUpdaterMenu", "AdminPanel.PluginManager.AutoUpdateMenu",
                PluginAutoUpdaterMenu::new));
        registerSubCommand(new MenuOpenCommand(getCommandName(), "PluginInstallMenu", "AdminPanel.PluginManager.InstallPlugins",
                PluginInstallMenu::new));
        registerSubCommand(new MenuOpenCommand(getCommandName(), "PluginSelectMenu", "AdminPanel.PluginManager.Open",
                pmu -> new PluginSelectMenu(pmu, null)));
        registerSubCommand(new MenuOpenCommand(getCommandName(), "ChatManagerMenu", "AdminPanel.ServerManagment.ChatManager.Open",
                ChatManagerMenu::new));
        registerSubCommand(new MenuOpenCommand(getCommandName(), "ServerManagerMenu", "AdminPanel.ServerManagment.Open",
                ServerManagerMenu::new));
        registerSubCommand(new MenuOpenCommand(getCommandName(), "WhitelistedPlayersMenu", "AdminPanel.ServerManagment.ManageWhitelist",
                pmu -> new WhitelistedPlayersMenu(pmu, null)));
        registerSubCommand(new MenuOpenCommand(getCommandName(), "WhitelistManagerMenu", "AdminPanel.ServerManagment.ManageWhitelist",
                WhitelistManagerMenu::new));
        registerSubCommand(new MenuOpenCommand(getCommandName(), "WorldCreateMenu", "AdminPanel.WorldManager.Create",
                WorldCreateMenu::new));
        registerSubCommand(new MenuOpenCommand(getCommandName(), "WorldSelectMenu", "AdminPanel.WorldManager.open",
                pmu -> new WorldSelectMenu(pmu, null)));
        registerSubCommand(new MenuOpenCommand(getCommandName(), "AdminPanelStartMenu", "AdminPanel.open",
                AdminPanelStartMenu::new));
        registerSubCommand(new MenuOpenCommand(getCommandName(), "StartupStatusMenu", StartupStatusMenu.PERMISSION,
                StartupStatusMenu::new));
        registerSubCommand(new MenuOpenCommand(getCommandName(), "JournalMenu", JournalMenu.PERMISSION,
                JournalMenu::new));
        registerSubCommand(new MenuOpenCommand(getCommandName(), "TempLanguageSelectMenu", "AdminPanel.SwitchLanguage",
                pmu -> new TempLanguageSelectMenu(pmu, null)));
    }
}
