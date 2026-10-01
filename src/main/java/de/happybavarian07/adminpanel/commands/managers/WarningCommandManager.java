package de.happybavarian07.adminpanel.commands.managers;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.coolstufflib.commandmanagement.CommandManager;
import de.happybavarian07.coolstufflib.commandmanagement.SubCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

abstract class WarningCommandManager extends CommandManager {
    abstract SubCommand rootCommand();

    @Override
    public String getCommandInfo() {
        return lgm == null ? "" : lgm.getMessageOrDefault("Commands." + getCommandName() + "." + getCommandName() + ".Info", null, "", false);
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return AdminPanelMain.getPlugin();
    }

    @Override
    public List<String> getCommandAliases() {
        return List.of();
    }

    @Override
    public boolean autoRegisterPermission() {
        return false;
    }

    @Override
    public void setup() {
        setRootCommand(rootCommand());
    }
}
