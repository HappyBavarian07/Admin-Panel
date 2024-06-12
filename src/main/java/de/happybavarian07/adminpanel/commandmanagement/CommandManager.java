package de.happybavarian07.adminpanel.commandmanagement;/*
 * @Author HappyBavarian07
 * @Date 09.11.2021 | 14:43
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.language.LanguageManager;
import de.happybavarian07.adminpanel.language.Placeholder;
import de.happybavarian07.adminpanel.language.PlaceholderType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

@CommandData
public abstract class CommandManager {
    protected final ArrayList<SubCommand> commands = new ArrayList<>();
    protected final AdminPanelMain adminpanel = AdminPanelMain.getPlugin();
    protected final LanguageManager lgm = adminpanel.getLanguageManager();
    protected List<String> commandArgs = new ArrayList<>();
    protected List<String> commandSubArgs = new ArrayList<>();

    public abstract String getCommandName();

    public abstract String getCommandUsage();

    public abstract String getCommandInfo();

    public abstract JavaPlugin getJavaPlugin();

    public abstract List<String> getCommandAliases();

    public Permission getCommandPermissionAsPermission() {
        return new Permission(getCommandPermissionAsString());
    }

    public abstract String getCommandPermissionAsString();

    public abstract boolean autoRegisterPermission();

    public boolean onPlayerCommand(Player player, String[] args) {
        SubCommand target = this.getSub(args[0]);

        if (target == null) {
            player.sendMessage(lgm.getMessage("Player.Commands.InvalidSubCommand", player, true));
            return true;
        }

        if (!hasPermission(player, target)) {
            //if (!player.hasPermission(target.permission()) || (target.isOpRequired() && !player.isOp())) {
            player.sendMessage(format(lgm.getMessage("Player.General.NoPermissions", player, true), target));
            return true;
        }
        String[] updatedArgs = removeFirstArgument(args);
        if (!target.subArgs().isEmpty() && target.allowOnlySubCommandArgsThatFitToSubArgs()) {
            int count2 = 1;
            Map<Integer, String> invalidArgs = new HashMap<>();
            for (String arg : updatedArgs) {
                if (!Arrays.asList(target.subArgs().get(count2)).contains(arg)) {
                    invalidArgs.put(count2, arg);
                }
                count2++;
            }
            if (!invalidArgs.isEmpty()) {
                lgm.addPlaceholder(PlaceholderType.MESSAGE, "%invalidArgs%", invalidArgs.toString(), false);
                player.sendMessage(format(lgm.getMessage("Player.Commands.CommandContainsInvalidArgs", player, true), target));
                return false;
            }
        }
        try {
            boolean callResult = target.onPlayerCommand(player, updatedArgs);
            if (!callResult) {
                player.sendMessage(format(lgm.getMessage("Player.Commands.UsageMessage", player, true), target));
            }
        } catch (Exception e) {
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%error%", e + ": " + e.getMessage(), false);
            player.sendMessage(format(lgm.getMessage("Player.Commands.ErrorPerformingSubCommand", player, true), target));
            e.printStackTrace();
        }
        return true;
    }

    public boolean onConsoleCommand(ConsoleCommandSender sender, String[] args) {
        SubCommand target = this.getSub(args[0]);

        if (target == null) {
            sender.sendMessage(lgm.getMessage("Player.Commands.InvalidSubCommand", null, true));
            return true;
        }

        if (!hasPermission(sender, target)) {
            //if (!sender.hasPermission(target.permission()) || (target.isOpRequired() && !sender.isOp())) {
            sender.sendMessage(format(lgm.getMessage("Player.General.NoPermissions", null, true), target));
            return true;
        }

        if (target.isPlayerRequired()) {
            sender.sendMessage(lgm.getMessage("Console.ExecutesPlayerCommand", null, true));
            return true;
        }
        String[] updatedArgs = removeFirstArgument(args);
        if (!target.subArgs().isEmpty() && target.allowOnlySubCommandArgsThatFitToSubArgs()) {
            int count2 = 1;
            Map<Integer, String> invalidArgs = new HashMap<>();
            for (String arg : updatedArgs) {
                if (!Arrays.asList(target.subArgs().get(count2)).contains(arg)) {
                    invalidArgs.put(count2, arg);
                }
                count2++;
            }
            if (!invalidArgs.isEmpty()) {
                lgm.addPlaceholder(PlaceholderType.MESSAGE, "%invalidArgs%", invalidArgs.toString(), false);
                sender.sendMessage(format(lgm.getMessage("Player.Commands.CommandContainsInvalidArgs", null, true), target));
                return false;
            }
        }
        try {
            boolean callResult = target.onConsoleCommand(sender, updatedArgs);
            if (!callResult) {
                sender.sendMessage(format(lgm.getMessage("Player.Commands.UsageMessage", null, true), target));
            }
        } catch (Exception e) {
            lgm.addPlaceholder(PlaceholderType.MESSAGE, "%error%", e + ": " + e.getMessage(), false);
            sender.sendMessage(format(lgm.getMessage("Player.Commands.ErrorPerformingSubCommand", null, true), target));
            e.printStackTrace();
        }
        return true;
    }

    public String[] removeFirstArgument(String[] args) {
        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(args));
        arrayList.remove(0);
        String[] updatedArgs = new String[arrayList.size()];
        int count = 0;
        for (String s : arrayList) {
            updatedArgs[count] = s;
            count++;
        }
        return updatedArgs;
    }

    public boolean hasPermission(CommandSender sender, SubCommand target) {
        return sender.hasPermission(target.permissionAsPermission()) || (target.isOpRequired() && sender.isOp());
    }

    public boolean handleSubCommand(CommandSender sender, SubCommand target, String[] args) {
        if (sender instanceof Player) {
            return target.onPlayerCommand((Player) sender, args);
        }
        return target.onConsoleCommand((ConsoleCommandSender) sender, args);
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        //System.out.println("Test 2");
        /*commandArgs.clear();
        commandSubArgs.clear();
        for (SubCommand sub : this.getSubCommands()) {
            String[] aliases;
            int length = (aliases = sub.aliases()).length;
            if (sender.hasPermission(sub.permission()) || (sub.isOpRequired() && sender.isOp())) {
                commandArgs.add(sub.name());
                commandArgs.addAll(Arrays.asList(aliases).subList(0, length));
            }
        }

        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            for (String a : commandArgs) {
                if (a.toLowerCase().startsWith(args[0].toLowerCase()))
                    result.add(a);
            }
            return result;
        }
        if (args.length > 1) {
            //System.out.println("Test SubArgs 1");
            int count = 0;
            for (SubCommand sub : this.getSubCommands()) {
                if (sub.subArgs() == null || sub.aliases() == null || sub.subArgs().get(args.length - 1) == null) {
                    if (count >= this.getSubCommands().size()) {
                        return Utils.emptyList();
                    } else {
                        continue;
                    }
                }
                if (sub.subArgs().get(args.length - 1) == null) {
                    if (count >= this.getSubCommands().size()) {
                        return Utils.emptyList();
                    } else {
                        continue;
                    }
                }
                String[] subArgs;
                int length = (subArgs = sub.subArgs().get(args.length - 1)).length;
                if (length == 0) {
                    if (count >= this.getSubCommands().size()) {
                        return Utils.emptyList();
                    } else {
                        continue;
                    }
                }
                if (!sub.name().equals(args[0])) {
                    for (String s : sub.aliases()) {
                        if (s.equals(args[0])) {
                            if (sender.hasPermission(sub.permission()) || (sub.isOpRequired() && sender.isOp())) {
                                if (sender.hasPermission(sub.permission()) || (sub.isOpRequired() && sender.isOp())) {
                                    commandSubArgs.addAll(Arrays.asList(subArgs).subList(0, length));
                                }
                                //System.out.println("Test SubArgs 2 (args)");
                                List<String> result1 = new ArrayList<>();
                                for (String a : commandSubArgs) {
                                    if (a.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                                        result1.add(a);
                                }
                                return result1;
                            }
                        }
                        if (count >= this.getSubCommands().size()) {
                            return Utils.emptyList();
                        }
                    }
                } else {
                    if (sender.hasPermission(sub.permission()) || (sub.isOpRequired() && sender.isOp())) {
                        if (sender.hasPermission(sub.permission()) || (sub.isOpRequired() && sender.isOp())) {
                            commandSubArgs.addAll(Arrays.asList(subArgs).subList(0, length));
                        }
                        //System.out.println("Test SubArgs 2 (name)");
                        List<String> result1 = new ArrayList<>();
                        for (String a : commandSubArgs) {
                            if (a.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                                result1.add(a);
                        }
                        return result1;
                    }
                }
                count++;
            }
            return Utils.emptyList();
        }
        return Utils.emptyList();*/
        List<String> result = new ArrayList<>();
        List<String> subCommandOptions = new ArrayList<>();
        List<String> subCommandArgOptions = new ArrayList<>();

        for (SubCommand sub : this.getSubCommands()) {
            if (!sender.hasPermission(sub.permissionAsPermission()) && !(sub.isOpRequired() && sender.isOp())) {
                continue;
            }

            subCommandOptions.add(sub.name());
            subCommandOptions.addAll(Arrays.asList(sub.aliases()));

            if (args.length == 1) {
                for (String option : subCommandOptions) {
                    if (option.toLowerCase().startsWith(args[0].toLowerCase())) {
                        result.add(option);
                    }
                }
            } else if (args.length > 1) {
                if (sub.name().equals(args[0]) || Arrays.asList(sub.aliases()).contains(args[0])) {
                    Map<Integer, String[]> subArgs = sub.subArgs();
                    if (subArgs != null && subArgs.containsKey(args.length - 1)) {
                        subCommandArgOptions.addAll(Arrays.asList(subArgs.get(args.length - 1)));
                    }

                    for (String option : subCommandArgOptions) {
                        if (option.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                            result.add(option);
                        }
                    }

                    return result;
                }
            }
        }

        return result;
    }

    public abstract void setup();

    public List<SubCommand> getSubCommands() {
        return commands;
    }

    protected SubCommand getSub(String name) {
        for (SubCommand sub : getSubCommands()) {
            if (sub.name().equalsIgnoreCase(name)) {
                return sub;
            }

            String[] aliases;
            int length = (aliases = sub.aliases()).length;

            for (int i = 0; i < length; i++) {
                String alias = aliases[i];
                if (name.equalsIgnoreCase(alias)) {
                    return sub;
                }
            }
        }
        return null;
    }

    private String format(String in, SubCommand cmd) {
        Map<String, Placeholder> placeholders = new HashMap<>();
        placeholders.put("%usage%", new Placeholder("%usage%", cmd.syntax(), PlaceholderType.ALL));
        placeholders.put("%description%", new Placeholder("%description%", cmd.info(), PlaceholderType.ALL));
        placeholders.put("%name%", new Placeholder("%name%", cmd.name(), PlaceholderType.ALL));
        placeholders.put("%permission%", new Placeholder("%permission%", cmd.permissionAsString(), PlaceholderType.ALL));
        placeholders.put("%aliases%", new Placeholder("%aliases%", cmd.aliases(), PlaceholderType.ALL));
        placeholders.put("%subArgs%", new Placeholder("%subArgs%", cmd.subArgs().toString(), PlaceholderType.ALL));

        return lgm.replacePlaceholders(in, placeholders);
    }
}
