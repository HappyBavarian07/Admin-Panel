# AdminPanel Menu Example (Production Pattern)

This guide provides a complete pattern for a production-grade admin menu using:
- `MultiPaginatedMenu`
- Three independent pagination zones (top/middle/bottom)
- Mixed transition strategies per zone
- Composed `MenuAction` wrappers (permission, cooldown, sound, open menu)
- Smart filler (`setFillerGlass()`) without manual frame loops

## Goals

- Keep static frame + controls stable.
- Paginate separate data sets independently.
- Keep click handling explicit and predictable.

---

## 1) Example Class

```java
package your.plugin.menus;

import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.MultiPaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.menusystem.actions.CompositeAction;
import de.happybavarian07.coolstufflib.menusystem.actions.CooldownAction;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import de.happybavarian07.coolstufflib.menusystem.actions.OpenMenuAction;
import de.happybavarian07.coolstufflib.menusystem.actions.PermissionAction;
import de.happybavarian07.coolstufflib.menusystem.actions.SoundAction;
import de.happybavarian07.coolstufflib.menusystem.pagination.ClearThenInstantPageTransition;
import de.happybavarian07.coolstufflib.menusystem.pagination.PageControlLayout;
import de.happybavarian07.coolstufflib.menusystem.pagination.PageDirection;
import de.happybavarian07.coolstufflib.menusystem.pagination.PaginationZone;
import de.happybavarian07.coolstufflib.menusystem.pagination.SequentialPageTransition;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminPanelMenu extends MultiPaginatedMenu {
    private List<String> onlinePlayerIds;
    private List<String> worldNames;
    private List<String> loadedFeatureFlags;

    public AdminPanelMenu(PlayerMenuUtility util, Menu previous) {
        super(util, previous);
    }

    @Override
    public String getMenuName() {
        return "Admin Panel";
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "AdminPanel";
    }

    @Override
    public int getSlots() {
        return 54; // 6 rows
    }

    @Override
    protected void preSetMenuItems() {
        // Smart filler: auto-skips all defined zone slots and zone controls.
        // Define zones/controls first so exclusion is correct.
        // (Calling it before defineZone is also fine if you only want a fully filled background.)
        registerGlobalButtons();

        // data snapshots
        onlinePlayerIds = Bukkit.getOnlinePlayers().stream().map(p -> p.getUniqueId().toString()).toList();
        worldNames = Bukkit.getWorlds().stream().map(w -> w.getName()).toList();
        loadedFeatureFlags = loadFeatureFlagsFromYourSystem();

        // define 3 independent zones
        defineZone("players-top", new PaginationZone(10, 16), onlinePlayerIds, this::renderPlayerItem);
        defineZone("worlds-mid", new PaginationZone(19, 25), worldNames, this::renderWorldItem);
        defineZone("flags-bottom", new PaginationZone(28, 34), loadedFeatureFlags, this::renderFlagItem);

        // per-zone controls (prev, close, next, refresh)
        setZoneControls("players-top", new PageControlLayout(9, 8, 17, 18));
        setZoneControls("worlds-mid", new PageControlLayout(27, 26, 35, 36));
        setZoneControls("flags-bottom", new PageControlLayout(45, 49, 53, 50));

        // now fill everything else
        setFillerGlass();

        // per-zone direction
        setZonePageDirection("players-top", PageDirection.NORMAL);
        setZonePageDirection("worlds-mid", PageDirection.REVERSED);
        setZonePageDirection("flags-bottom", PageDirection.NORMAL);

        // per-zone transitions
        setZoneTransition("players-top", new SequentialPageTransition<>());
        setZoneTransition("worlds-mid", new ClearThenInstantPageTransition<>());
        setZoneTransition("flags-bottom", new SequentialPageTransition<>());

        // global animation cadence for sequential transitions
        setAnimationTickDelay(2);
    }

    @Override
    protected void postSetMenuItems() {
        // optional footer/status items
        inventory.setItem(48, named(Material.BOOK, "§eAdmin Tips"));
    }

    @Override
    protected void handleZoneItemClick(String zoneId, Object data, int dataIndex, int slot, ItemStack item, InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        switch (zoneId) {
            case "players-top" -> {
                String playerId = (String) data;
                player.sendMessage("Selected player: " + playerId);
                // open a dedicated player management menu, etc.
            }
            case "worlds-mid" -> {
                String worldName = (String) data;
                player.sendMessage("Selected world: " + worldName);
            }
            case "flags-bottom" -> {
                String flag = (String) data;
                toggleFlag(flag);
                open(); // refresh
            }
            default -> player.sendMessage("Unknown zone: " + zoneId);
        }
    }

    @Override
    protected void handleOutsideZoneClick(int slot, ItemStack item, InventoryClickEvent event) {
        // optional behavior for non-zone clicks
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {}

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {}

    private void registerGlobalButtons() {
        MenuAction openAuditLog = new CompositeAction(
                new PermissionAction("admin.panel.audit",
                        new CooldownAction(750,
                                new SoundAction(Sound.UI_BUTTON_CLICK, 1.0f, 1.0f,
                                        new OpenMenuAction(new AuditLogMenu(playerMenuUtility, this))
                                ),
                                "§cPlease wait before clicking again."
                        )
                )
        );
        registerButton(4, named(Material.WRITABLE_BOOK, "§bAudit Log"), openAuditLog, null);
    }

    private ItemStack renderPlayerItem(String playerId) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§aPlayer " + shortId(playerId));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack renderWorldItem(String worldName) {
        return named(Material.GRASS_BLOCK, "§a" + worldName);
    }

    private ItemStack renderFlagItem(String flag) {
        boolean enabled = isFlagEnabled(flag);
        return named(enabled ? Material.LIME_DYE : Material.GRAY_DYE, (enabled ? "§a" : "§7") + flag);
    }

    private ItemStack named(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private String shortId(String fullId) {
        return fullId.length() > 8 ? fullId.substring(0, 8) : fullId;
    }

    private List<String> loadFeatureFlagsFromYourSystem() {
        return new ArrayList<>(List.of("anti-xray", "chat-filter", "combat-log", "world-lock"));
    }

    private boolean isFlagEnabled(String flag) {
        // replace with your backing config/service
        return flag.hashCode() % 2 == 0;
    }

    private void toggleFlag(String flag) {
        // replace with your write/update logic
    }

    // Example submenu target used by OpenMenuAction
    static class AuditLogMenu extends Menu {
        public AuditLogMenu(PlayerMenuUtility util, Menu previous) {
            super(util, previous);
        }
        @Override public String getMenuName() { return "Audit Log"; }
        @Override public String getConfigMenuAddonFeatureName() { return "AuditLog"; }
        @Override public int getSlots() { return 27; }
        @Override public void handleMenu(InventoryClickEvent e) {}
        @Override public void handleOpenMenu(InventoryOpenEvent e) {}
        @Override public void handleCloseMenu(InventoryCloseEvent e) {}
        @Override public void setMenuItems() { setFillerGlass(); }
    }
}
```

---

## 2) Why This Pattern Works

- `MultiPaginatedMenu` keeps each zone independent, so paging one list does not affect others.
- Per-zone transitions let you animate only where it helps.
- Composed actions keep click behavior reusable and testable.
- Smart filler removes manual frame-loop boilerplate and keeps zone slots untouched automatically.

---

## 3) Common Variations

- Replace string datasets with typed DTOs.
- Use `ConfirmationAction` before destructive admin operations.
- Give each zone different page-control slots depending on design.
- Use `ClearThenInstantPageTransition` in high-frequency menus for predictable redraws.

---

## 4) Implementation Checklist

1. Keep static frame slots outside your zones.
2. Define each zone with explicit slot ownership.
3. Assign controls per zone (`PageControlLayout`).
4. Add transitions only where necessary.
5. Route zone clicks by `zoneId` in one switch.
6. Use composed actions for permission/cooldown/sound/open flows.

This is a strong baseline for `AdminPanel`, `PlayerSelect`, and `WorldSelect` style menus.
