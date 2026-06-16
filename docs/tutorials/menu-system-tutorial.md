# CoolStuffLib Menu System Tutorial

This tutorial covers the current menu architecture, including constrained pagination zones, multi-zone pagination, pluggable page transitions, and reusable menu actions.

## Contents
1. Quick Start
2. Core Concepts
3. Build a Basic Menu
4. Build a Paginated Menu (Single Data Source)
5. Pagination Zones (Constrained Layouts)
6. MultiPaginatedMenu (Multiple Independent Lists)
7. Transition Strategies and Animation
8. Reusable Actions
9. Smart Filler API (No Manual Frame Loops)
10. PlayerMenuUtility
11. Migration Notes
12. Troubleshooting

---

## 1) Quick Start

Initialize via `CoolStuffLibBuilder`:

```java
CoolStuffLib lib = new CoolStuffLibBuilder(plugin)
        .withMenuSystem()
        .build()
        .createCoolStuffLib();
```

Get player utility:

```java
PlayerMenuUtility util = lib.getPlayerMenuUtility(player.getUniqueId());
```

---

## 2) Core Concepts

- `Menu`: Base class for all inventory menus.
- `PaginatedMenu<T>`: One paginated data source, now with optional constrained zones and pluggable transitions.
- `MultiPaginatedMenu`: Multiple independent paginated zones in one inventory.
- `MenuListener`: Routes inventory events to your menu instances.
- `MenuAction`: Reusable click action interface and wrappers/decorators.

---

## 3) Build a Basic Menu

```java
public class AdminMenu extends Menu {
    public AdminMenu(PlayerMenuUtility util) {
        super(util);
    }

    @Override
    public String getMenuName() { return "Admin Panel"; }

    @Override
    public String getConfigMenuAddonFeatureName() { return "AdminPanel"; }

    @Override
    public int getSlots() { return 54; }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        // place/register buttons here
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {}

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {}

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {}
}
```

Open it:

```java
new AdminMenu(util).open();
```

---

## 4) Build a Paginated Menu (Single Data Source)

```java
public class WorldSelectMenu extends PaginatedMenu<World> {
    public WorldSelectMenu(PlayerMenuUtility util, Menu previous) {
        super(util, previous);
    }

    @Override
    public String getMenuName() { return "Select World"; }

    @Override
    public String getConfigMenuAddonFeatureName() { return "WorldSelect"; }

    @Override
    public int getSlots() { return 54; }

    @Override
    public void preSetMenuItems() {}

    @Override
    public void postSetMenuItems() {}

    @Override
    protected void handlePageItemClick(int slot, ItemStack item, InventoryClickEvent event) {
        World world = getPaginatedDataForSlot(slot, item);
        if (world == null) return;
        event.getWhoClicked().sendMessage("Selected: " + world.getName());
    }

    @Override
    protected void handleCustomItemClick(int slot, ItemStack item, InventoryClickEvent event) {}

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {}

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {}
}
```

Set data:

```java
menu.setPaginatedData(Bukkit.getWorlds(), world -> {
    ItemStack item = new ItemStack(Material.GRASS_BLOCK);
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName("§a" + world.getName());
    item.setItemMeta(meta);
    return item;
});
```

If you do nothing else, `PaginatedMenu` keeps its default behavior.

---

## 5) Pagination Zones (Constrained Layouts)

Use zones when the content area should not occupy the full default region.

```java
import de.happybavarian07.coolstufflib.menusystem.pagination.PaginationZone;

// contiguous zone
menu.setPaginationZone(new PaginationZone(10, 16));

// or explicit slots
menu.setPaginationZone(new PaginationZone(10, 11, 12, 19, 20, 21, 28, 29, 30));
```

This enables frame/header/sidebar layouts while preserving page-click mapping.

---

## 6) MultiPaginatedMenu (Multiple Independent Lists)

Use when one inventory needs multiple paginated data blocks with separate page states.

```java
public class DashboardMenu extends MultiPaginatedMenu {
    public DashboardMenu(PlayerMenuUtility util, Menu previous) {
        super(util, previous);
    }

    @Override
    public String getMenuName() { return "Dashboard"; }

    @Override
    public String getConfigMenuAddonFeatureName() { return "Dashboard"; }

    @Override
    public int getSlots() { return 54; }

    @Override
    protected void preSetMenuItems() {}

    @Override
    protected void postSetMenuItems() {}

    @Override
    protected void handleZoneItemClick(String zoneId, Object data, int dataIndex, int slot, ItemStack item, InventoryClickEvent event) {
        event.getWhoClicked().sendMessage(zoneId + " -> " + data);
    }

    @Override
    protected void handleOutsideZoneClick(int slot, ItemStack item, InventoryClickEvent event) {}

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {}

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {}
}
```

Define zones:

```java
dashboard.defineZone("top", new PaginationZone(10, 16), topData, topRenderer);
dashboard.defineZone("middle", new PaginationZone(19, 25), middleData, middleRenderer);
dashboard.defineZone("bottom", new PaginationZone(28, 34), bottomData, bottomRenderer);
```

Each zone can have its own controls, direction, and transition strategy.

---

## 7) Transition Strategies and Animation

Available built-ins (`menusystem.pagination`):
- `InstantPageTransition`
- `ClearThenInstantPageTransition`
- `SequentialPageTransition`

`PaginatedMenu` setup:

```java
menu.setPageTransition(new SequentialPageTransition<>());
menu.setAnimationTickDelay(2);
```

Directional controls:

```java
menu.setNavigationDirection(NavigationDirection.VERTICAL); // uses Up/Down if present
menu.setPageDirection(PageDirection.REVERSED);            // invert logical page direction
menu.setControlLayout(new PageControlLayout(45, 49, 53, 50));
```

You can implement your own transition by implementing `PageTransition<T>`.

---

## 8) Reusable Actions

Available action helpers in `menusystem.actions`:
- `PermissionAction`
- `ConfirmationAction`
- `CompositeAction`
- `CooldownAction`
- `SoundAction`
- `OpenMenuAction`

Example composition:

```java
MenuAction action = new CompositeAction(
        new PermissionAction("admin.menu.use",
                new CooldownAction(750,
                        new SoundAction(Sound.UI_BUTTON_CLICK, 1f, 1f,
                                new OpenMenuAction(targetMenu)
                        ),
                        "Please wait before clicking again."
                )
        )
);
```

Register on a slot:

```java
registerButton(13, icon, action, null);
```

---

## 9) Smart Filler API (No Manual Frame Loops)

`setFillerGlass()` is now zone-aware:

- In `PaginatedMenu`, it skips:
  - the active pagination slots
  - pagination control slots
- In `MultiPaginatedMenu`, it skips:
  - all defined zone slots
  - all zone control slots

So in most paginated menus, this is enough:

```java
@Override
public void preSetMenuItems() {
    setFillerGlass(); // smart filler
    // add custom static buttons/labels only where needed
}
```

You can still exclude extra slots explicitly:

```java
setFillerGlass(4, 49, 50);
```

or

```java
setFillerGlass(Set.of(4, 49, 50));
```

---

## 10) PlayerMenuUtility

Use `PlayerMenuUtility` for state across menu transitions:

```java
util.setData("selected_world", worldName, true);
String world = util.getData("selected_world", String.class, null);
```

Always resolve utility through the library:

```java
PlayerMenuUtility util = CoolStuffLib.getLib().getPlayerMenuUtility(player.getUniqueId());
```

---

## 11) Migration Notes

- Existing `PaginatedMenu` implementations continue to work with default behavior.
- Prefer `menusystem.pagination.PaginationZone` for new code.
- Legacy `menusystem.PaginationZone` is available as a compatibility wrapper.
- Move one menu at a time:
  1. Keep existing pagination.
  2. Add a zone.
  3. Add custom controls/direction.
  4. Add transition strategy.
  5. Migrate advanced menus to `MultiPaginatedMenu` only where needed.

---

## 12) Troubleshooting

- Clicks not handled:
  - Ensure inventory holder is your menu instance and menu is opened via `menu.open()`.
- Page controls do nothing:
  - Verify control slots are valid and not overwritten later in `setMenuItems`.
- Null data on click:
  - Use `getPaginatedDataForSlot(slot, item)` in `PaginatedMenu`.
  - In `MultiPaginatedMenu`, handle via `handleZoneItemClick(...)` arguments.
- Animation oddities:
  - Increase `setAnimationTickDelay(...)`.
  - Switch to `InstantPageTransition` to isolate rendering issues.

---

Use this tutorial as the baseline pattern for all new menu work in CoolStuffLib.
