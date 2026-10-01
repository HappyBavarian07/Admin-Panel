package de.happybavarian07.adminpanel.warnings;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.UUID;

public final class BukkitEscalationExecutor implements EscalationExecutor {
    private final AdminPanelMain plugin;

    public BukkitEscalationExecutor(AdminPanelMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run(EscalationRule rule, UUID player) {
        if (Bukkit.isPrimaryThread()) {
            execute(rule, player);
        } else if (plugin.isEnabled()) {
            Bukkit.getScheduler().runTask(plugin, () -> execute(rule, player));
        }
    }

    private void execute(EscalationRule rule, UUID player) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(player);
        String name = target.getName();
        if (name == null) {
            plugin.getLogger().warning("Escalation rule " + rule + " skipped, the name of " + player + " is unknown");
            return;
        }
        boolean exempt = plugin.getConfig().getStringList("Pman.Actions.ExemptPlayers").contains(name);
        String reason = rule.reason().isEmpty()
                ? plugin.getLanguageManager().getMessage("Warnings.Escalation.DefaultReason", null, false)
                : rule.reason();
        switch (rule.action()) {
            case KICK -> {
                Player online = target.getPlayer();
                if (!exempt && online != null) online.kickPlayer(reason);
            }
            case TEMPBAN -> {
                if (exempt) return;
                Bukkit.getBanList(BanList.Type.NAME).addBan(name, reason,
                        new Date(System.currentTimeMillis() + rule.durationMillis()), plugin.getName());
                Player online = target.getPlayer();
                if (online != null) online.kickPlayer(reason);
            }
            case COMMAND -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), rule.command().replace("%player%", name));
        }
    }
}
