# CoolStuffLib Menu System Tutorial

This tutorial covers the menu architecture: buttons with click actions, chainable action decorators,
language-driven buttons, list menus, pagination zones, multi-zone pagination, page transitions and
in-place refreshing.

## Contents
1. Quick Start
2. Core Concepts
3. Build a Basic Menu
4. Buttons and Click Actions
5. Chaining Action Decorators
6. Buttons from the Language File
7. Navigation, Chat Prompts and Commands
8. Different Actions per Click Type
9. List Menus Without Subclassing
10. Build a Paginated Menu (Single Data Source)
11. Pagination Zones (Constrained Layouts)
12. MultiPaginatedMenu (Multiple Independent Lists)
13. Transition Strategies and Animation
14. Refreshing Without Reopening
15. Smart Filler API (No Manual Frame Loops)
16. PlayerMenuUtility
17. Migration Notes
18. Troubleshooting

---

## 1) Quick Start

Initialize via `CoolStuffLibBuilder`:

```java
CoolStuffLib lib = new CoolStuffLibBuilder(plugin)
        .withMenuSystem()
        .build()
        .createCoolStuffLib();
lib.setup();
```

Get the player's menu utility:

```java
PlayerMenuUtility util = lib.getPlayerMenuUtility(player.getUniqueId());
```

---

## 2) Core Concepts

- `Menu`: base class for all inventory menus.
- `MenuAction`: what happens when a slot is clicked. Register one per button with `registerButton`.
- `PaginatedMenu<T>`: one paginated data source, with optional zones and transitions.
- `ListMenu<T>`: a ready-made paginated menu built from a list, no subclass needed.
- `MultiPaginatedMenu`: several independent paginated zones in one inventory.
- `MenuListener`: routes clicks to the registered action of the slot (registered automatically by `setup()`).

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
        // register buttons here (next step), then fill the rest
        setFillerGlass();
    }

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

`handleMenu(InventoryClickEvent)` no longer needs to be overridden. It is deprecated; clicks are handled by
the actions you register.

---

## 4) Buttons and Click Actions

Register a button in `setMenuItems()`. The lambda runs when the slot is clicked:

```java
@Override
public void setMenuItems() {
    registerButton(13, healItem, (player, event) -> player.setHealth(20));
    registerButton(closeItem, MenuAction.close()); // first free slot
    setFillerGlass();
}
```

- `registerButton(slot, item, action)` places the item and remembers the action.
- `registerButton(item, action)` uses the first free slot.
- The four-argument versions additionally take a set of slots that must stay free.
- Actions are cleared on every `open()`, so a button that is not registered again is no longer clickable.

---

## 5) Chaining Action Decorators

Wrap an action with `MenuAction.of(...)` and chain decorators:

```java
registerButton(22, stopItem, MenuAction.of((p, e) -> Bukkit.shutdown())
        .confirm("Stop the server?")          // opens a confirmation menu first
        .cooldown(5000, "Please wait.")        // per-player cooldown in ms
        .requires("admin.server.stop"));       // permission check
```

Each decorator wraps everything before it, so the **last one runs first**. Above, the permission is
checked first, then the cooldown, and only then does the confirmation menu open.

| Decorator | Effect |
|---|---|
| `requires(permission)` | Runs only with the permission, otherwise sends `Player.General.NoPermissions` |
| `confirm(reason)` | Opens the confirmation menu; afterwards the clicked menu is reopened |
| `cooldown(ms)` / `cooldown(ms, message)` | Blocks repeated clicks per player |
| `withSound(sound)` | Plays a sound before the action |
| `then(next)` | Runs another action afterwards |
| `thenClose()` / `thenBack()` | Closes the inventory / returns to the parent menu afterwards |

The decorator classes (`PermissionAction`, `ConfirmationAction`, `CooldownAction`, `SoundAction`,
`CompositeAction`) still exist if you prefer constructing them directly.

---

## 6) Buttons from the Language File

Items and their slots usually live in the language file:

```yaml
Items:
  PlayerManager:
    Heal:
      material: GOLDEN_APPLE
      displayName: '&aHeal'
      slot: 13
```

One call places the item at the configured slot and registers the action:

```java
button("PlayerManager.Heal", MenuAction.of((p, e) -> heal(p)).requires("admin.heal"));
```

Without a `slot` entry the first free slot is used. `slot: 0` is valid.

---

## 7) Navigation, Chat Prompts and Commands

Built-in actions:

```java
registerButton(49, backItem, MenuAction.back());                        // parent menu, or close
registerButton(50, closeItem, MenuAction.close());
registerButton(10, worldsItem, MenuAction.open(() -> new WorldMenu(util, this))); // built on click
registerButton(40, infoItem, MenuAction.none());                        // does nothing
```

`MenuAction.open(...)` creates the target menu only when the button is clicked, so opening a menu with
many navigation buttons stays cheap.

Ask for chat input. The menu closes, the message is sent, and the player's next chat message is
handed to your code on the main thread:

```java
registerButton(31, renameItem, MenuAction.prompt("Type the new name:", (player, text) -> {
    rename(text);
    open(); // reopen this menu
}));
```

Run commands:

```java
registerButton(11, spawnItem, MenuAction.command("spawn").thenClose());               // as the player
registerButton(12, rewardItem, MenuAction.consoleCommand("give {player} diamond 1")  // as the console
        .requires("shop.reward"));
```

`{player}` and `{uuid}` are replaced; a leading `/` is optional. Always guard console commands with
`requires(...)`, because the console can run anything.

---

## 8) Different Actions per Click Type

```java
registerButton(20, entryItem, ClickAction.of()
        .left(openDetails)
        .right(removeEntry.confirm("Remove this entry?"))
        .shift(copyEntry)
        .middle(showInfo)
        .otherwise(MenuAction.none()));
```

Shift clicks use the `shift` action if set, otherwise they fall through to `left`/`right`.

---

## 9) List Menus Without Subclassing

For "pick one of these" menus, build a `ListMenu` directly:

```java
new ListMenu<>(util, this, "&8Online Players", 54,
        new ArrayList<>(Bukkit.getOnlinePlayers()),
        target -> ItemUtils.createSkull(target.getName(), "&e" + target.getName(), false),
        (player, target, event) -> new PlayerProfileMenu(util, target).open())
        .open();
```

Arguments: utility, parent menu, title, size (27–54), entries, how to render an entry, what to do when
an entry is clicked. The border, page buttons and close button come from `PaginatedMenu`.

---

## 10) Build a Paginated Menu (Single Data Source)

Subclass `PaginatedMenu` when you need custom buttons around the list:

```java
public class WorldSelectMenu extends PaginatedMenu<World> {
    public WorldSelectMenu(PlayerMenuUtility util, Menu previous) {
        super(util, previous);
        setPaginatedData(Bukkit.getWorlds(), this::render,
                (player, world, event) -> player.sendMessage("Selected: " + world.getName()));
    }

    private ItemStack render(World world) {
        ItemStack item = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§a" + world.getName());
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public String getMenuName() { return "Select World"; }

    @Override
    public String getConfigMenuAddonFeatureName() { return "WorldSelect"; }

    @Override
    public int getSlots() { return 54; }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {}

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {}
}
```

`preSetMenuItems()`, `postSetMenuItems()`, `handlePageItemClick(...)` and `handleCustomItemClick(...)` are
optional hooks; override them only when needed. Buttons around the list are registered with
`registerButton` in `postSetMenuItems()`.

---

## 11) Pagination Zones (Constrained Layouts)

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

## 12) MultiPaginatedMenu (Multiple Independent Lists)

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
Control slots are drawn with the `General.Left/Close/Right/Refresh` language items; override them in
`postSetMenuItems()`. Redefining a zone with the same id (for example in `preSetMenuItems()` with fresh
data) keeps its page, controls, direction and transition.

---

## 13) Transition Strategies and Animation

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

## 14) Refreshing Without Reopening

`open()` creates a new inventory. When only the contents change (a toggle, a counter), redraw in place:

```java
registerButton(15, toggleItem(), (player, event) -> {
    enabled = !enabled;
    refresh();
});
```

`refresh()` clears the inventory and its click actions, runs `setMenuItems()` and menu addons again, and
keeps the cursor where it is. The title is not changed (reopen with `open()` for that). The refresh
button of paginated menus uses it.

---

## 15) Smart Filler API (No Manual Frame Loops)

`setFillerGlass()` is zone-aware:

- In `PaginatedMenu`, it skips the active pagination slots and the pagination control slots.
- In `MultiPaginatedMenu`, it skips all defined zone slots and all zone control slots.

So in most paginated menus, this is enough:

```java
@Override
public void preSetMenuItems() {
    setFillerGlass(); // smart filler
}
```

You can still exclude extra slots explicitly:

```java
setFillerGlass(4, 49, 50);
setFillerGlass(Set.of(4, 49, 50));
```

Register buttons **before** filling, or on slots that only hold filler: `registerButton` refuses a slot
that already holds a real item.

---

## 16) PlayerMenuUtility

Use `PlayerMenuUtility` for state across menu transitions:

```java
util.setData("selected_world", worldName, true);
String world = util.getData("selected_world", String.class, null);
```

Always resolve the utility through the library:

```java
PlayerMenuUtility util = CoolStuffLib.getLib().getPlayerMenuUtility(player.getUniqueId());
```

---

## 17) Migration Notes

Moving a menu from `handleMenu` item matching to actions, one menu at a time:

1. Replace each `if (item.isSimilar(...))` branch with a `registerButton(slot, item, action)` (or
   `button("Path", action)`) call in `setMenuItems()`.
2. Replace permission checks inside the branches with `.requires(...)`, confirmations with
   `.confirm(...)`, and "go back" code with `MenuAction.back()`.
3. Delete the `handleMenu` override.
4. For paginated menus, pass the click handler to `setPaginatedData(list, renderer, onClick)` and drop
   `handlePageItemClick`, or switch to `ListMenu`.

Other changes:
- Menus implementing `Listener` are unregistered again when closed.
- Prefer `menusystem.pagination.PaginationZone`; the legacy `menusystem.PaginationZone` is a compatibility wrapper.

---

## 18) Troubleshooting

- Clicks not handled:
  - The menu must be opened with `menu.open()` so it is the inventory holder.
  - The button must be registered during the current render (`setMenuItems()` or after `refresh()`).
- `IllegalStateException: Slot occupied`:
  - Another real item is on that slot. Register the button before placing other items, or use a free slot.
- Page controls do nothing:
  - Verify control slots are valid and not overwritten later in `setMenuItems`.
- Null data on click:
  - Use the `onClick` handler of `setPaginatedData(...)` or `getPaginatedDataForSlot(slot, item)`.
  - In `MultiPaginatedMenu`, handle via `handleZoneItemClick(...)` arguments.
- Animation oddities:
  - Increase `setAnimationTickDelay(...)`.
  - Switch to `InstantPageTransition` to isolate rendering issues.

---

Use this tutorial as the baseline pattern for all new menu work in CoolStuffLib.
