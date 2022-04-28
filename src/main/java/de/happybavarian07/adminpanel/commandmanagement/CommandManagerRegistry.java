package de.happybavarian07.adminpanel.commandmanagement;/*
 * @Author HappyBavarian07
 * @Date 09.11.2021 | 14:52
 */

import de.happybavarian07.adminpanel.commands.DCommand;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.main.LanguageManager;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManagerRegistry implements CommandExecutor, TabCompleter {
    private final AdminPanelMain plugin;
    private final LanguageManager lgm;
    private final Map<CommandManager, CommandData> commandManagers;

    public CommandManagerRegistry(AdminPanelMain plugin) {
        this.plugin = plugin;
        this.lgm = plugin.getLanguageManager();
        this.commandManagers = new HashMap<>();
    }

    public void register(CommandManager cm) {
        if (commandManagers.containsKey(cm)) return;

        // Checking if the Command Manager has CommandData

        CommandData data = cm.getClass().getAnnotation(CommandData.class);

        // Registering the Command on the Server
        if(plugin.getCommand(cm.getCommandName()) != null) {
            plugin.getCommand(cm.getCommandName()).setExecutor(this);
            plugin.getCommand(cm.getCommandName()).setTabCompleter(this);
        } else {
            DCommand pluginCommand = new DCommand(cm.getCommandName(), plugin);
            pluginCommand.setProperty("label", this.plugin.getName().toLowerCase());
            pluginCommand.setProperty("aliases", cm.getCommandAliases());
            pluginCommand.setProperty("usage", cm.getCommandUsage());
            pluginCommand.setProperty("description", cm.getCommandInfo());
            pluginCommand.setProperty("permission", cm.getCommandPermission());
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(this);
            pluginCommand.register();
        }
        // Calling setup() for Adding Sub Commands
        cm.setup();
        commandManagers.put(cm, data);
    }

    public void unregister(CommandManager cm) {
        if (!commandManagers.containsKey(cm)) return;

        // Unregistering the Command on the Server
        plugin.getCommand(cm.getCommandName()).setExecutor(null);
        plugin.getCommand(cm.getCommandName()).setTabCompleter(null);
        // Calling setup() for Adding Sub Commands
        cm.getSubCommands().clear();
        commandManagers.remove(cm);
    }

    public void unregisterAll() {
        for (CommandManager cm : commandManagers.keySet()) {
            // Unregistering the Command on the Server
            plugin.getCommand(cm.getCommandName()).setExecutor(null);
            plugin.getCommand(cm.getCommandName()).setTabCompleter(null);
            // Calling setup() for Adding Sub Commands
            cm.getSubCommands().clear();
            commandManagers.remove(cm);
        }
    }

    public Map<CommandManager, CommandData> getCommandManagers() {
        return commandManagers;
    }

    public CommandManager getCommandManager(String commandName) {
        for (CommandManager cm : commandManagers.keySet()) {
            if (cm.getCommandName().equals(commandName)) {
                return cm;
            }
        }
        return null;
    }

    public Boolean isPlayerRequired(CommandManager commandManager) {
        CommandData data = commandManagers.get(commandManager);
        return data.playerRequired();
    }

    public List<SubCommand> getSubCommands(String commandName) {
        for (CommandManager cm : commandManagers.keySet()) {
            if (cm.getCommandName().equals(commandName)) {
                return cm.getSubCommands();
            }
        }
        return null;
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        for (CommandManager cm : commandManagers.keySet()) {
            if (cm.getCommandName().equalsIgnoreCase(cmd.getName())) {
                if (args.length == 0) {
                    sender.sendMessage(lgm.getMessage("Player.Commands.TooFewArguments", (sender instanceof Player) ? (Player) sender : null, true));
                    return true;
                }
                if (sender instanceof Player) {
                    return cm.onCommand((Player) sender, args);
                } else {
                    if(isPlayerRequired(cm)) {
                        sender.sendMessage(lgm.getMessage("Console.ExecutesPlayerCommand", null, true));
                        return true;
                    }
                    return cm.onCommand((ConsoleCommandSender) sender, args);
                }
            }
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        for (CommandManager cm : commandManagers.keySet()) {
            if (cm.getCommandName().equalsIgnoreCase(cmd.getName())) {
                try {
                    if (!(sender instanceof Player) && isPlayerRequired(cm)) {
                        return Utils.emptyList();
                    }
                    if (args.length == 0) {
                        return Utils.emptyList();
                    }
                    //System.out.println("Test 1");
                    return cm.onTabComplete(sender, cmd, label, args);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    return Utils.emptyList();
                }
            }
        }
        return null;
    }
}
