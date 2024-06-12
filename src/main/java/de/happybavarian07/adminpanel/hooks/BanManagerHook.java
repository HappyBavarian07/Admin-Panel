package de.happybavarian07.adminpanel.hooks;/*
 * @Author HappyBavarian07
 * @Date 16.02.2024 | 14:15
 */

import dte.hooksystem.hook.AbstractPluginHook;
import me.confuser.banmanager.common.api.BmAPI;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.UUID;

public class BanManagerHook extends AbstractPluginHook {
    private AdvancedBanMethodInterface advancedBanMethodInterface;

    public BanManagerHook(String pluginName) {
        super(pluginName);
    }

    @Override
    public void init() throws Exception {
        BmAPI banManagerAPI = queryProvider(BmAPI.class);
    }

    public void banPlayer(UUID bannedPlayer, String reason, UUID actor, boolean silent, long duration) {
        try {
            PlayerData bannedPlayerData = BmAPI.getPlayer(bannedPlayer);
            PlayerData actorData = BmAPI.getPlayer(actor);
            BmAPI.ban(new PlayerBanData(bannedPlayerData, actorData, reason, silent, duration, System.currentTimeMillis()));
            // Kick if Online
            if(Bukkit.getPlayer(bannedPlayer) != null && Bukkit.getPlayer(bannedPlayer).isOnline()) {
                Bukkit.getPlayer(bannedPlayer).kickPlayer(reason);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void unbanPlayer(UUID bannedPlayer, UUID actor) {
        try {
            PlayerData bannedPlayerData = BmAPI.getPlayer(bannedPlayer);
            PlayerData actorData = BmAPI.getPlayer(actor);
            BmAPI.unban(new PlayerBanData(bannedPlayerData, actorData, "", false, 0, 0), actorData);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void banIP(IPAddress ip, UUID bannedPlayer, String reason, UUID actor, boolean silent, long duration) {
        try {
            PlayerData actorData = BmAPI.getPlayer(actor);
            BmAPI.ban(new IpBanData(ip, actorData, reason, silent, duration, System.currentTimeMillis()));
            // Kick if Online
            if(Bukkit.getPlayer(bannedPlayer) != null && Bukkit.getPlayer(bannedPlayer).isOnline()) {
                Bukkit.getPlayer(bannedPlayer).kickPlayer(reason);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void unbanIP(IPAddress ip, UUID actor) {
        try {
            PlayerData actorData = BmAPI.getPlayer(actor);
            BmAPI.unban(new IpBanData(ip, actorData, "", false, 0, 0), actorData);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void mutePlayer(UUID mutedPlayer, String reason, UUID actor, boolean silent, boolean soft, long duration) {
        try {
            PlayerData mutedPlayerData = BmAPI.getPlayer(mutedPlayer);
            PlayerData actorData = BmAPI.getPlayer(actor);
            BmAPI.mute(new PlayerMuteData(mutedPlayerData, actorData, reason, silent, soft, duration));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void unmutePlayer(UUID mutedPlayer, UUID actor) {
        try {
            PlayerData mutedPlayerData = BmAPI.getPlayer(mutedPlayer);
            PlayerData actorData = BmAPI.getPlayer(actor);
            BmAPI.unmute(new PlayerMuteData(mutedPlayerData, actorData, "", false, false, 0), actorData);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void muteIP(IPAddress ip, String reason, UUID actor, boolean silent, boolean soft, long duration) {
        try {
            PlayerData actorData = BmAPI.getPlayer(actor);
            BmAPI.mute(new IpMuteData(ip, actorData, reason, silent, soft, duration));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void unmuteIP(IPAddress ip, UUID actor) {
        try {
            PlayerData actorData = BmAPI.getPlayer(actor);
            BmAPI.unmute(new IpMuteData(ip, actorData, "", false, false, 0), actorData);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
