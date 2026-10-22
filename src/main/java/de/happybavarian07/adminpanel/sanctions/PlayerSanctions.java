package de.happybavarian07.adminpanel.sanctions;

import de.happybavarian07.adminpanel.journal.AdminJournal;
import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.data.PlayerWarning;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.warnings.WarningManager;
import de.happybavarian07.coolstufflib.languagemanager.LanguageManager;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.UUID;

/**
 * Bans and kicks players for the player menus. Both actions skip the players of {@code Pman.Actions.ExemptPlayers},
 * tell the target why, announce the action unless it is silent and write it to the warning history as a
 * {@code RECORD} when the warning system is enabled.
 */
public final class PlayerSanctions {
    private static final String BAN_PATH = "Player.PlayerManager.BanMenu.";
    private static final String KICK_PATH = "Player.PlayerManager.KickMenu.";

    private PlayerSanctions() {
    }

    public enum BanResult {
        BANNED,
        /** The name is banned, but the IP is not because the player is offline. */
        BANNED_IP_UNKNOWN,
        EXEMPT,
        ALREADY_BANNED,
        UNKNOWN_PLAYER
    }

    public enum KickResult {
        KICKED,
        EXEMPT,
        OFFLINE
    }

    /**
     * Reads the presets and ban lengths from {@code Pman.Actions.Ban}, invalid entries are logged and skipped.
     */
    public static BanSettings settings() {
        AdminPanelMain plugin = AdminPanelMain.getPlugin();
        return BanSettings.parse(plugin.getConfig().getMapList("Pman.Actions.Ban.Presets"),
                plugin.getConfig().getStringList("Pman.Actions.Ban.Durations"), plugin.getLogger()::warning);
    }

    /**
     * Whether the name is listed in {@code Pman.Actions.ExemptPlayers}.
     */
    public static boolean isExempt(String name) {
        if (name == null) return false;
        for (String exempt : AdminPanelMain.getPlugin().getConfig().getStringList("Pman.Actions.ExemptPlayers")) {
            if (exempt.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    /**
     * Bans a player by name and optionally by IP, kicks him if he is online and records the ban.
     *
     * @param staff          who bans, used as the source of the ban
     * @param target         the player to ban
     * @param reason         the ban reason, a default text if blank
     * @param durationMillis the ban length, {@code 0} for a permanent ban
     * @param ipBan          whether to ban the IP address too, only possible while the target is online
     * @param silent         whether to skip the broadcast
     */
    public static BanResult ban(CommandSender staff, OfflinePlayer target, String reason, long durationMillis,
                                boolean ipBan, boolean silent) {
        AdminPanelMain plugin = AdminPanelMain.getPlugin();
        LanguageManager lgm = plugin.getLanguageManager();
        String name = target.getName();
        if (name == null) return BanResult.UNKNOWN_PLAYER;
        if (isExempt(name)) return BanResult.EXEMPT;
        if (target.isBanned()) return BanResult.ALREADY_BANNED;
        String why = reason == null || reason.isBlank() ? CommandText.get(lgm, staff, BAN_PATH + "DefaultReason") : reason;
        Date expires = durationMillis > 0 ? new Date(System.currentTimeMillis() + durationMillis) : null;
        String duration = durationText(lgm, staff, durationMillis);
        Player online = target.getPlayer();
        boolean ipBanned = false;
        if (ipBan && online != null) {
            InetSocketAddress address = online.getAddress();
            if (address != null) {
                Bukkit.getBanList(BanList.Type.IP).addBan(address.getAddress().getHostAddress(), why, expires, staff.getName());
                ipBanned = true;
            }
        }
        Bukkit.getBanList(BanList.Type.NAME).addBan(name, why, expires, staff.getName());
        if (online != null) {
            online.kickPlayer(CommandText.get(lgm, online, BAN_PATH + "TargetBanMessage",
                    "%reason%", why, "%duration%", duration, "%staff%", staff.getName()));
        }
        recordBan(target.getUniqueId(), why, durationMillis, ipBanned, issuerOf(staff));
        AdminJournal.log(staff, ipBanned ? "BAN_IP" : "BAN", name, duration + " | " + why);
        if (!silent) {
            Bukkit.broadcastMessage(CommandText.get(lgm, null, BAN_PATH + "Broadcast",
                    "%staff%", staff.getName(), "%target%", name, "%duration%", duration, "%reason%", why));
        }
        return ipBan && !ipBanned ? BanResult.BANNED_IP_UNKNOWN : BanResult.BANNED;
    }

    /**
     * Kicks an online player and records the kick.
     *
     * @param staff  who kicks
     * @param target the player to kick
     * @param reason the kick reason, a default text if blank
     * @param silent whether to skip the broadcast
     */
    public static KickResult kick(CommandSender staff, OfflinePlayer target, String reason, boolean silent) {
        AdminPanelMain plugin = AdminPanelMain.getPlugin();
        LanguageManager lgm = plugin.getLanguageManager();
        if (isExempt(target.getName())) return KickResult.EXEMPT;
        Player online = target.getPlayer();
        if (online == null) return KickResult.OFFLINE;
        String why = reason == null || reason.isBlank() ? CommandText.get(lgm, staff, KICK_PATH + "DefaultReason") : reason;
        online.kickPlayer(CommandText.get(lgm, online, KICK_PATH + "TargetKickMessage", "%reason%", why, "%staff%", staff.getName()));
        AdminJournal.log(staff, "KICK", target.getName(), why);
        WarningManager history = plugin.getWarningManager();
        if (history != null) {
            String text = ChatColor.stripColor(CommandText.get(lgm, null, "Warnings.Record.Kick", "%reason%", why));
            history.record(target.getUniqueId(), text, issuerOf(staff));
        }
        if (!silent) {
            Bukkit.broadcastMessage(CommandText.get(lgm, null, KICK_PATH + "Broadcast",
                    "%staff%", staff.getName(), "%target%", online.getName(), "%reason%", why));
        }
        return KickResult.KICKED;
    }

    /**
     * Writes a ban to the warning history. Does nothing when the warning system is disabled.
     *
     * @param player         the banned player
     * @param reason         the ban reason
     * @param durationMillis the ban length, {@code 0} for a permanent ban
     * @param ipBan          whether the IP address was banned too
     * @param issuer         a UUID text, {@link PlayerWarning#CONSOLE} or {@link PlayerWarning#SYSTEM}
     */
    public static void recordBan(UUID player, String reason, long durationMillis, boolean ipBan, String issuer) {
        AdminPanelMain plugin = AdminPanelMain.getPlugin();
        WarningManager history = plugin.getWarningManager();
        if (history == null) return;
        LanguageManager lgm = plugin.getLanguageManager();
        String text = ChatColor.stripColor(CommandText.get(lgm, null, "Warnings.Record." + (ipBan ? "BanIp" : "Ban"),
                "%reason%", reason, "%duration%", durationText(lgm, null, durationMillis)));
        history.record(player, text, issuer);
    }

    private static String durationText(LanguageManager lgm, CommandSender viewer, long durationMillis) {
        return durationMillis > 0 ? Durations.format(durationMillis) : CommandText.get(lgm, viewer, "Warnings.Format.Permanent");
    }

    private static String issuerOf(CommandSender staff) {
        return staff instanceof Player player ? player.getUniqueId().toString() : PlayerWarning.CONSOLE;
    }
}
