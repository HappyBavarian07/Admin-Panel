package de.happybavarian07.adminpanel.permissions.vault;

import de.happybavarian07.adminpanel.permissions.PermissionService;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

/** Registers the permission manager as the Vault permission and chat provider. This class is only loaded when Vault is installed. */
public final class VaultHook {
    private VaultHook() {
    }

    public static void register(Plugin plugin, PermissionService service) {
        VaultPermissionProvider permission = new VaultPermissionProvider(plugin, service);
        Bukkit.getServicesManager().register(Permission.class, permission, plugin, ServicePriority.Highest);
        Bukkit.getServicesManager().register(Chat.class, new VaultChatProvider(plugin, service, permission), plugin, ServicePriority.Highest);
    }

    public static void unregister(Plugin plugin) {
        Bukkit.getServicesManager().unregisterAll(plugin);
    }
}
