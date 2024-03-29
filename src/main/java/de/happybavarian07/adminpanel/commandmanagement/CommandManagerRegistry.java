package de.happybavarian07.adminpanel.commandmanagement;/*
 * @Author HappyBavarian07
 * @Date 09.11.2021 | 14:52
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.language.LanguageManager;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CommandManagerRegistry implements CommandExecutor, TabCompleter {
    private final LanguageManager lgm;
    private final Map<CommandManager, CommandData> commandManagers;

    public CommandManagerRegistry(AdminPanelMain plugin) {
        this.lgm = plugin.getLanguageManager();
        this.commandManagers = new HashMap<>();
    }

    private static Object getPrivateField(Object object, String field) {
        Object result;
        try {
            Field objectField = object.getClass().getDeclaredField(field);
            objectField.setAccessible(true);
            result = objectField.get(object);
            objectField.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    public static void unregisterCommand(PluginCommand cmd, JavaPlugin javaPlugin) {
        try {
            Object result = getPrivateField(Bukkit.getServer().getPluginManager(), "commandMap");
            SimpleCommandMap commandMap = (SimpleCommandMap) result;
            assert commandMap != null;
            Object map = getPrivateField(commandMap, "knownCommands");
            @SuppressWarnings("unchecked")
            HashMap<String, Command> knownCommands = (HashMap<String, Command>) map;
            assert knownCommands != null;
            knownCommands.remove(cmd.getName());
            for (String alias : cmd.getAliases()) {
                if (knownCommands.containsKey(alias) && knownCommands.get(alias).toString().contains(javaPlugin.getName())) {
                    knownCommands.remove(alias);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unregisterCommand(BukkitCommand cmd, JavaPlugin javaPlugin) {
        try {
            Object result = getPrivateField(Bukkit.getServer().getPluginManager(), "commandMap");
            SimpleCommandMap commandMap = (SimpleCommandMap) result;
            assert commandMap != null;
            Object map = getPrivateField(commandMap, "knownCommands");
            @SuppressWarnings("unchecked")
            HashMap<String, Command> knownCommands = (HashMap<String, Command>) map;
            assert knownCommands != null;
            knownCommands.remove(cmd.getName());
            for (String alias : cmd.getAliases()) {
                if (knownCommands.containsKey(alias) && knownCommands.get(alias).toString().contains(javaPlugin.getName())) {
                    knownCommands.remove(alias);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean register(CommandManager cm) {
        if (commandManagers.containsKey(cm) || cm == null) return false;

        // Checking if the Command Manager has CommandData

        CommandData data = cm.getClass().getAnnotation(CommandData.class);

        JavaPlugin javaPlugin = cm.getJavaPlugin();
        // Registering the Command on the Server
        if (javaPlugin.getCommand(cm.getCommandName()) != null) {
            Objects.requireNonNull(javaPlugin.getCommand(cm.getCommandName())).setExecutor(this);
            Objects.requireNonNull(javaPlugin.getCommand(cm.getCommandName())).setTabCompleter(this);
        } else {
            DCommand pluginCommand = new DCommand(cm.getCommandName(), javaPlugin);
            pluginCommand.setProperty("label", javaPlugin.getName().toLowerCase());
            pluginCommand.setProperty("aliases", cm.getCommandAliases());
            pluginCommand.setProperty("usage", cm.getCommandUsage());
            pluginCommand.setProperty("description", cm.getCommandInfo());
            pluginCommand.setProperty("permission", cm.getCommandPermissionAsString());
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(this);
            pluginCommand.register();
        }

        if(cm.autoRegisterPermission()) {
            if (cm.getCommandPermissionAsString().isEmpty()) {
                if (!Bukkit.getPluginManager().getPermissions().contains(cm.getCommandPermissionAsPermission())) {
                    Bukkit.getPluginManager().addPermission(cm.getCommandPermissionAsPermission());
                }
            } else {
                Permission tempPerm = new Permission(cm.getCommandPermissionAsString());
                if (!Bukkit.getPluginManager().getPermissions().contains(tempPerm)) {
                    Bukkit.getPluginManager().addPermission(tempPerm);
                }
            }
        }

        // Calling setup() for Adding Sub Commands
        cm.setup();

        for(SubCommand subCommand : cm.getSubCommands()) {
            if(subCommand.autoRegisterPermission()) {
                if (subCommand.permissionAsString().isEmpty()) {
                    if (!Bukkit.getPluginManager().getPermissions().contains(subCommand.permissionAsPermission())) {
                        Bukkit.getPluginManager().addPermission(subCommand.permissionAsPermission());
                    }
                } else {
                    Permission tempPerm = new Permission(subCommand.permissionAsString());
                    if (!Bukkit.getPluginManager().getPermissions().contains(tempPerm)) {
                        Bukkit.getPluginManager().addPermission(tempPerm);
                    }
                }
            }
        }

        if(cm.autoRegisterPermission()) {
            if (cm.getCommandPermissionAsString().isEmpty()) {
                if (Bukkit.getPluginManager().getPermissions().contains(cm.getCommandPermissionAsPermission())) {
                    Bukkit.getPluginManager().removePermission(cm.getCommandPermissionAsPermission());
                }
            } else {
                Permission tempPerm = new Permission(cm.getCommandPermissionAsString());
                if (Bukkit.getPluginManager().getPermissions().contains(tempPerm)) {
                    Bukkit.getPluginManager().removePermission(tempPerm);
                }
            }
        }

        for(SubCommand subCommand : cm.getSubCommands()) {
            if(subCommand.autoRegisterPermission()) {
                if (subCommand.permissionAsString().isEmpty()) {
                    if (Bukkit.getPluginManager().getPermissions().contains(subCommand.permissionAsPermission())) {
                        Bukkit.getPluginManager().removePermission(subCommand.permissionAsPermission());
                    }
                } else {
                    Permission tempPerm = new Permission(subCommand.permissionAsString());
                    if (Bukkit.getPluginManager().getPermissions().contains(tempPerm)) {
                        Bukkit.getPluginManager().removePermission(tempPerm);
                    }
                }
            }
        }

        commandManagers.put(cm, data);
        return true;
    }

    public void unregister(CommandManager cm) {
        if (!commandManagers.containsKey(cm) || cm == null) return;

        JavaPlugin javaPlugin = cm.getJavaPlugin();

        // Unregistering the Command on the Server
        if (javaPlugin.getCommand(cm.getCommandName()) != null) {
            unregisterCommand(javaPlugin.getCommand(cm.getCommandName()), javaPlugin);
        } else {
            DCommand pluginCommand = new DCommand(cm.getCommandName(), javaPlugin);
            pluginCommand.setProperty("label", javaPlugin.getName().toLowerCase());
            pluginCommand.setProperty("aliases", cm.getCommandAliases());
            pluginCommand.setProperty("usage", cm.getCommandUsage());
            pluginCommand.setProperty("description", cm.getCommandInfo());
            pluginCommand.setProperty("permission", cm.getCommandPermissionAsString());
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(this);
            unregisterCommand(pluginCommand, javaPlugin);
        }
        // Calling setup() for Adding Sub Commands
        cm.getSubCommands().clear();
        commandManagers.remove(cm);
    }

    public void unregisterAll() {
        for (CommandManager cm : commandManagers.keySet()) {
            unregister(cm);
        }
    }

    public Map<CommandManager, CommandData> getCommandManagers() {
        return commandManagers;
    }

    public CommandManager getCommandManager(String commandName) {
        for (CommandManager cm : commandManagers.keySet()) {
            if (cm.getCommandName().equals(commandName)) {
                return cm;
            } else if (cm.getCommandAliases().contains(commandName)) {
                return cm;
            }
        }
        return null;
    }

    public Boolean isPlayerRequired(CommandManager commandManager) {
        CommandData data = commandManagers.get(commandManager);
        if(data == null) return false;
        return data.playerRequired();
    }

    public Boolean isOpRequired(CommandManager commandManager) {
        CommandData data = commandManagers.get(commandManager);
        if(data == null) return false;
        return data.opRequired();
    }

    public Boolean allowOnlySubCommandArgsThatFitToSubArgs(CommandManager commandManager) {
        CommandData data = commandManagers.get(commandManager);
        if(data == null) return false;
        return data.allowOnlySubCommandArgsThatFitToSubArgs();
    }

    public List<SubCommand> getSubCommands(String commandName) {
        return getCommandManager(commandName).getSubCommands();
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
                    if (isPlayerRequired(cm)) {
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
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
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
