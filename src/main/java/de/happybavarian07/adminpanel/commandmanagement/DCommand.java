package de.happybavarian07.adminpanel.commandmanagement;/*
 * @Author HappyBavarian07
 * @Date 28.04.2022 | 19:41
 */

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class DCommand extends BukkitCommand {
    private CommandExecutor commandExecutor;
    private TabCompleter tabCompleter;
    private JavaPlugin plugin;

    protected DCommand(@NotNull String name, CommandExecutor executor, TabCompleter tabCompleter) {
        super(name);
        setExecutor(executor);
        setTabCompleter(tabCompleter);
    }

    public DCommand(@NotNull String name, JavaPlugin plugin) {
        super(name);
        this.plugin = plugin;
    }

    public void setExecutor(CommandExecutor executor) {
        commandExecutor = executor;
    }

    public void setTabCompleter(TabCompleter completer) {
        tabCompleter = completer;
    }

    @Override
    public @Nullable List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) {
        return tabCompleter != null ? tabCompleter.onTabComplete(sender, this, alias, args) : null;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        return commandExecutor.onCommand(sender, this, commandLabel, args);
    }

    public void setProperties(Map<String, Object> properties) {
        for(Map.Entry<String, Object> c : properties.entrySet()) {
            setProperty(c.getKey(), c.getValue());
        }
    }

    public void setProperty(String name, Object value) {
        switch (name) {
            case "aliases":
                @SuppressWarnings("unchecked") List<String> aliases = (List<String>) value;
                this.setAliases(aliases);
                break;
            case "usage":
                this.setUsage((String) value);
                break;
            case "description":
                this.setDescription((String) value);
                break;
            case "permission":
                this.setPermission((String) value);
                break;
            case "label":
                this.setLabel((String) value);
                break;
        }
    }

    public void register() {
        if(commandExecutor == null) {
            throw new CommandNotPreparedException();
        }
        try {
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

            commandMap.register(plugin.getName(), this);
        }catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("serial")
    public static class CommandNotPreparedException extends RuntimeException{
        public CommandNotPreparedException() {
            super("no CommandExecutor was found");
        }
    }
}
