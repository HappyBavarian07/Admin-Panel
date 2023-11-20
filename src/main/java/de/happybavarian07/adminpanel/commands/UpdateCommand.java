package de.happybavarian07.adminpanel.commands;

import de.happybavarian07.adminpanel.utils.NewUpdater;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.language.LanguageManager;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UpdateCommand implements CommandExecutor, TabCompleter {
    private final NewUpdater updater;
    private final LanguageManager lgm;
    private final List<String> completerArgs = new ArrayList<>();
    //private final Map<String, Integer> versionArgs = new HashMap<>();

    public UpdateCommand() throws JSONException {
        AdminPanelMain plugin = AdminPanelMain.getPlugin();
        this.updater = plugin.getUpdater();
        this.lgm = plugin.getLanguageManager();
        /*JSONArray versionList = plugin.getUpdater()
                .getArrayFromWebsite("https://api.spiget.org/v2/resources/91800/versions?size=99999&fields=id%2Cname%2CreleaseDate");
        int index = 0;
        JSONObject jsonObj;

        while (index < versionList.length()) {
            jsonObj = versionList.getJSONObject(index);
            versionArgs.put(jsonObj.getString("name"), jsonObj.getInt("id"));
            index++;
        }*/
        //System.out.println("Insgesamt: " + versionArgs);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
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
                boolean check = updater.updateAvailable();
                if (args[0].equalsIgnoreCase("check")) {
                    if (check) {
                        updater.getMessages().sendUpdateMessage(sender);
                    } else {
                        updater.getMessages().sendNoUpdateMessage(sender);
                    }
                } else if (args[0].equalsIgnoreCase("download")) {
                    if (check) {
                        try {
                            updater.downloadLatestUpdate(false, true, true);
                            sender.sendMessage(Utils.chat(
                                    "&aNew Version now available in the downloaded-update Folder! (Further Actions required)"));
                        } catch (Exception e) {
                            e.printStackTrace();
                            sender.sendMessage(Utils.chat(AdminPanelMain.getPrefix() + " &cSomething went completely wrong!"));
                        }
                    } else {
                        updater.getMessages().sendNoUpdateMessage(sender);
                    }
                } else if (args[0].equalsIgnoreCase("forcedownload")) {
                    try {
                        updater.downloadLatestUpdate(false, true, true);
                        sender.sendMessage(Utils.chat(AdminPanelMain.getPrefix() + "&aForce Download finished!"));
                        sender.sendMessage(Utils.chat(AdminPanelMain.getPrefix() +
                                "&aNew Version now available in the downloaded-update Folder! (Further Actions required)"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        sender.sendMessage(Utils.chat(AdminPanelMain.getPrefix() + " &cSomething went completely wrong!"));
                    }
                } else if (args[0].equalsIgnoreCase("replace")) {
                    if (check) {
                        try {
                            updater.downloadLatestUpdate(true, true, true);
                            sender.sendMessage(Utils.chat(AdminPanelMain.getPrefix() + "&aNew Version now available to play! (No further Actions required)"));
                        } catch (Exception e) {
                            e.printStackTrace();
                            sender.sendMessage(Utils.chat(AdminPanelMain.getPrefix() + " &cSomething went completely wrong!"));
                        }
                    } else {
                        updater.getMessages().sendNoUpdateMessage(sender);
                    }
                } else if (args[0].equalsIgnoreCase("forcereplace")) {
                    try {
                        updater.downloadLatestUpdate(true, true, true);
                        sender.sendMessage(Utils.chat(AdminPanelMain.getPrefix() + "&aForce Replace finished!"));
                        sender.sendMessage(Utils.chat(AdminPanelMain.getPrefix() + "&aNew Version now available to play! (No further Actions required)"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        sender.sendMessage(Utils.chat(AdminPanelMain.getPrefix() + " &cSomething went completely wrong!"));
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
                        sender.sendMessage(Utils.chat(AdminPanelMain.getPrefix() + " &cSomething went completely wrong!"));
                    }
                } else {
                    sender.sendMessage(Utils.chat(AdminPanelMain.getPrefix() + "&c Usage: &6" + command.getUsage()));
                }
            }/* else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("download")) {
                    try {
                        if (!versionArgs.containsKey(args[1])) {
                            sender.sendMessage(lgm.getMessage("Player.General.NotAValidUpdateVersion", null));
                            return true;
                        }
                        System.out.println("Name: " + args[1] + " | ID: " + versionArgs.get(args[1]));
                        updater.downloadSpecificUpdate(false, true, true, args[1], versionArgs.get(args[1]).toString());
                        //sender.sendMessage(Utils.chat("&aNew Version now available in the downloaded-update Folder! (Further Actions required)"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        sender.sendMessage(Utils.chat(AdminPanelMain.getPrefix() + " &cSomething went completely wrong!"));
                    }
                }
            }*/ else {
                sender.sendMessage(Utils.chat(AdminPanelMain.getPrefix() + "&c Usage: &6" + command.getUsage()));
            }
            return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
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
            }/* else if (args.length == 2) {
                for (String a : versionArgs.keySet()) {
                    if (a.toLowerCase().startsWith(args[1].toLowerCase()))
                        result.add(a);
                }
                return result;
            }*/

            if (args.length > 1) {
                return null;
            }
            return completerArgs;
        }
        return null;
    }
}
