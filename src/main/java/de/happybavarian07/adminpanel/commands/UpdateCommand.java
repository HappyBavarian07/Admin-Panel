package de.happybavarian07.adminpanel.commands;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.AdminPanelUtils;
import de.happybavarian07.adminpanel.utils.NewUpdater;
import de.happybavarian07.coolstufflib.languagemanager.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UpdateCommand implements CommandExecutor, TabCompleter {
    private final NewUpdater updater;
    private final LanguageManager lgm;
    private final List<String> completerArgs = new ArrayList<>();

    public UpdateCommand() throws JSONException {
        AdminPanelMain plugin = AdminPanelMain.getPlugin();
        this.updater = plugin.getUpdater();
        this.lgm = plugin.getLanguageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("update")) {
            if (!sender.hasPermission("AdminPanel.update")) {
                if (sender instanceof Player) {
                    sender.sendMessage(lgm.getMessage("Player.General.NoPermissions", (Player) sender, true));
                } else {
                    sender.sendMessage(lgm.getMessage("Player.General.NoPermissions", null, true));
                }
                return true;
            }
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("check")) {
                    updater.checkForUpdatesAsync(false, (check, latest) -> {
                        Bukkit.getScheduler().runTask(AdminPanelMain.getPlugin(), () -> {
                            if (check) updater.getMessages().sendUpdateMessage(sender);
                            else updater.getMessages().sendNoUpdateMessage(sender);
                        });
                    });
                } else if (args[0].equalsIgnoreCase("download")) {
                    updater.checkForUpdatesAsync(false, (check, latest) -> {
                        if (check) {
                            updater.downloadLatestUpdateAsync(false, true, true, r -> sender.sendMessage(AdminPanelUtils.chat(
                                    "&aNew Version now available in the downloaded-update Folder! (Further Actions required)")));
                        } else {
                            Bukkit.getScheduler().runTask(AdminPanelMain.getPlugin(), () -> updater.getMessages().sendNoUpdateMessage(sender));
                        }
                    });
                } else if (args[0].equalsIgnoreCase("forcedownload")) {
                    updater.downloadLatestUpdateAsync(false, true, true, r -> sender.sendMessage(AdminPanelUtils.chat(AdminPanelMain.getPrefix() + "&aForce Download finished!\n" +
                            "&aNew Version now available in the downloaded-update Folder! (Further Actions required)")));
                } else if (args[0].equalsIgnoreCase("replace")) {
                    updater.checkForUpdatesAsync(false, (check, latest) -> {
                        if (check) {
                            updater.downloadLatestUpdateAsync(true, true, true, r -> sender.sendMessage(AdminPanelUtils.chat(AdminPanelMain.getPrefix() + "&aNew Version now available to play! (No further Actions required)")));
                        } else {
                            Bukkit.getScheduler().runTask(AdminPanelMain.getPlugin(), () -> updater.getMessages().sendNoUpdateMessage(sender));
                        }
                    });
                } else if (args[0].equalsIgnoreCase("forcereplace")) {
                    updater.downloadLatestUpdateAsync(true, true, true, r -> sender.sendMessage(AdminPanelUtils.chat(AdminPanelMain.getPrefix() + "&aForce Replace finished!\n" +
                            AdminPanelMain.getPrefix() + "&aNew Version now available to play! (No further Actions required)")));
                } else if (args[0].equalsIgnoreCase("getlatest")) {
                    Bukkit.getScheduler().runTaskAsynchronously(AdminPanelMain.getPlugin(), () -> {
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
                            Bukkit.getScheduler().runTask(AdminPanelMain.getPlugin(), () -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    "&bCurrent Version: &c" + currentVersion + "&r\n" +
                                            "&bNew Version Name: &c" + versionName + "&r\n" +
                                            "&bNew Version ID: &c" + versionID + "&r\n" +
                                            "&bNew Version Title: &c" + versionTitle + "&r\n" +
                                            "&bNew Version Description: &c" + versionDescriptionDecoded + "&r\n" +
                                            "&bNew Version Likes: &c" + versionLikes + "&r\n" +
                                            "&bNew Version Date: &c" + versionDate)));
                        } catch (Exception e) {
                            Bukkit.getScheduler().runTask(AdminPanelMain.getPlugin(), () -> sender.sendMessage(AdminPanelUtils.chat(AdminPanelMain.getPrefix() + " &cSomething went completely wrong!")));
                        }
                    });
                } else {
                    sender.sendMessage(AdminPanelUtils.chat(AdminPanelMain.getPrefix() + "&c Usage: &6" + command.getUsage()));
                }
            } else {
                sender.sendMessage(AdminPanelUtils.chat(AdminPanelMain.getPrefix() + "&c Usage: &6" + command.getUsage()));
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
