package de.happybavarian07.commands;

import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.main.Main;
import de.happybavarian07.utils.Updater;
import de.happybavarian07.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UpdateCommand implements CommandExecutor, TabCompleter {
    private final Main plugin;
    private final Updater updater;
    private final LanguageManager lgm;
    List<String> completerArgs = new ArrayList<>();

    public UpdateCommand() {
        this.plugin = Main.getPlugin();
        this.updater = plugin.getUpdater();
        this.lgm = plugin.getLanguageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("update")) {
            if (!sender.hasPermission("AdminPanel.update")) {
                if (sender instanceof Player) {
                    sender.sendMessage(lgm.getMessage("Player.General.NoPermissions", (Player) sender));
                } else {
                    sender.sendMessage(lgm.getMessage("Player.General.NoPermissions", null));
                }
                return true;
            }
            if (args.length == 1) {
                boolean check = updater.updateAvailable();
                if (args[0].equalsIgnoreCase("check")) {
                    if (check) {
                        if (sender instanceof Player) {
                            updater.sendUpdateMessage((Player) sender);
                        } else {
                            updater.sendUpdateMessage((ConsoleCommandSender) sender);
                        }
                    } else {
                        if (sender instanceof Player) {
                            updater.sendNoUpdateMessage((Player) sender);
                        } else {
                            updater.sendNoUpdateMessage((ConsoleCommandSender) sender);
                        }
                    }
                } else if (args[0].equalsIgnoreCase("download")) {
                    if (check) {
                        try {
                            updater.downloadPlugin(false, true, false);
                            sender.sendMessage(Utils.getInstance().chat(
                                    "&aNew Version now available in the downloaded-update Folder! (Further Actions required)"));
                        } catch (Exception e) {
                            e.printStackTrace();
                            sender.sendMessage(Utils.getInstance().chat(Main.getPrefix() + " &cSomething went completely wrong!"));
                        }
                    } else {
                        if (sender instanceof Player) {
                            updater.sendNoUpdateMessage((Player) sender);
                        } else {
                            updater.sendNoUpdateMessage((ConsoleCommandSender) sender);
                        }
                    }
                } else if (args[0].equalsIgnoreCase("forcedownload")) {
                    try {
                        updater.downloadPlugin(false, true, false);
                        sender.sendMessage(Utils.getInstance().chat(Main.getPrefix() + "&aForce Download finished!"));
                        sender.sendMessage(Utils.getInstance().chat(Main.getPrefix() +
                                "&aNew Version now available in the downloaded-update Folder! (Further Actions required)"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        sender.sendMessage(Utils.getInstance().chat(Main.getPrefix() + " &cSomething went completely wrong!"));
                    }
                } else if (args[0].equalsIgnoreCase("replace")) {
                    if (check) {
                        try {
                            updater.downloadPlugin(true, true, false);
                            sender.sendMessage(Utils.getInstance().chat(Main.getPrefix() + "&aNew Version now available to play! (No further Actions required)"));
                        } catch (Exception e) {
                            e.printStackTrace();
                            sender.sendMessage(Utils.getInstance().chat(Main.getPrefix() + " &cSomething went completely wrong!"));
                        }
                    } else {
                        if (sender instanceof Player) {
                            updater.sendNoUpdateMessage((Player) sender);
                        } else {
                            updater.sendNoUpdateMessage((ConsoleCommandSender) sender);
                        }
                    }
                } else if (args[0].equalsIgnoreCase("forcereplace")) {
                    try {
                        updater.downloadPlugin(true, true, false);
                        sender.sendMessage(Utils.getInstance().chat(Main.getPrefix() + "&aForce Replace finished!"));
                        sender.sendMessage(Utils.getInstance().chat(Main.getPrefix() + "&aNew Version now available to play! (No further Actions required)"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        sender.sendMessage(Utils.getInstance().chat(Main.getPrefix() + " &cSomething went completely wrong!"));
                    }
                } else if (args[0].equalsIgnoreCase("getlatest")) {
                    try {
                        JSONObject websiteData = updater.getObjectFromWebsite("https://api.spiget.org/v2/resources/" + updater.getResourceID() + "/updates/latest");
                        String currentVersion = updater.getPluginVersion();
                        String versionName = updater.getLatestVersionName();
                        String versionID = String.valueOf(websiteData.getInt("id"));
                        String versionTitle = websiteData.getString("title");
                        String versionDescriptionEncoded = websiteData.getString("description");
                        String versionDescriptionDecoded = updater.html2text(new String(java.util.Base64.getDecoder().decode(versionDescriptionEncoded)));
                        String versionLikes = String.valueOf(websiteData.getInt("likes"));
                        String versionDate = String.valueOf(websiteData.getInt("date"));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                "&bCurrent Version: &c" + currentVersion + "&r\n" +
                                        "&bNew Version Name: &c" + versionName + "&r\n" +
                                        "&bNew Version ID: &c" + versionID + "&r\n" +
                                        "&bNew Version Title: &c" + versionTitle + "&r\n" +
                                        "&bNew Version Description: &c" + versionDescriptionDecoded + "&r\n" +
                                        "&bNew Version Likes: &c" + versionLikes + "&r\n" +
                                        "&bNew Version Date: &c" + versionDate));
                    } catch (Exception e) {
                        e.printStackTrace();
                        sender.sendMessage(Utils.getInstance().chat(Main.getPrefix() + " &cSomething went completely wrong!"));
                    }
                } else {
                    sender.sendMessage(Utils.getInstance().chat(Main.getPrefix() + "&c Usage: &6" + command.getUsage()));
                }
            } else {
                sender.sendMessage(Utils.getInstance().chat(Main.getPrefix() + "&c Usage: &6" + command.getUsage()));
            }
            return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("update")) {
            if (!sender.hasPermission("AdminPanel.update")) return null;
            if (completerArgs.isEmpty()) {
                completerArgs.add("check");
                completerArgs.add("download");
                completerArgs.add("forcedownload");
                completerArgs.add("replace");
                completerArgs.add("forcereplace");
                completerArgs.add("getlatest");
            }

            List<String> result = new ArrayList<>();
            if (args.length == 1) {
                for (String a : completerArgs) {
                    if (a.toLowerCase().startsWith(args[0].toLowerCase()))
                        result.add(a);
                }
                return result;
            }
            if (args.length > 1) {
                return null;
            }
            return completerArgs;
        }
        return null;
    }
}
