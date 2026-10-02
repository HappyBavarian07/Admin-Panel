package de.happybavarian07.adminpanel.menusystem.menu.playermanager;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.trolls.Troll;
import de.happybavarian07.adminpanel.trolls.TrollRegistry;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

/** All trolls of the {@link TrollRegistry} for one player. A click starts a troll or switches a toggle. */
public class PlayerTrollMenu extends PaginatedMenu<Troll> {
    private static final String OPEN_PERMISSION = "AdminPanel.PlayerManager.PlayerSettings.Actions.Troll.Open";
    private static final String PATH = "PlayerManager.TrollMenu.";

    private final UUID targetUUID;
    private final String targetName;
    private final TrollRegistry registry;

    public PlayerTrollMenu(PlayerMenuUtility playerMenuUtility) {
        this(playerMenuUtility, new PlayerActionsMenu(playerMenuUtility));
    }

    public PlayerTrollMenu(PlayerMenuUtility playerMenuUtility, Menu savedMenu) {
        super(playerMenuUtility, savedMenu);
        setOpeningPermission(OPEN_PERMISSION);
        this.targetUUID = playerMenuUtility.getTargetUUID();
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);
        this.targetName = target.getName() != null ? target.getName() : targetUUID.toString();
        this.registry = AdminPanelMain.getPlugin().getTrollRegistry();
    }

    @Override
    public String getMenuName() {
        lgm.addPlaceholder(PlaceholderType.MENUTITLE, "%target%", targetName, false);
        return lgm.getMenuTitle("PlayerManager.TrollMoreMenu", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PlayerTrollMenu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void preSetMenuItems() {
        List<Troll> trolls = registry.enabled();
        setPaginatedData(trolls, this::getPageItem, this::handleEntryClick);
        int perPage = getPaginatedItemSlots().length;
        if (perPage > 0) page = Math.min(page, Math.max(0, (trolls.size() - 1) / perPage));
    }

    @Override
    public void postSetMenuItems() {
        Player owner = playerMenuUtility.getOwner();
        registerButton(getSlot("PlayerManager.TrollMoreMenu.StopAll", 46), lgm.getItem(PATH + "StopAll", owner, false),
                MenuAction.of((p, e) -> stopAllTrolls(p, playerMenuUtility.getTarget(), targetName))
                        .then((p, e) -> refresh()).requires(OPEN_PERMISSION));
    }

    private void handleEntryClick(Player player, Troll troll, InventoryClickEvent event) {
        MenuAction.of((p, e) -> {
            Player target = playerMenuUtility.getTarget();
            if (target == null) {
                CommandText.send(lgm, p, "Player.General.TargetedPlayerIsNull");
                return;
            }
            TrollRegistry.Result result = registry.toggle(troll.id(), p, target);
            CommandText.send(lgm, p, "Trolls.Result." + result.name(), "%target%", target.getName(), "%troll%", troll.id());
            refresh();
        }).requires(troll.permission()).execute(player, event);
    }

    public ItemStack getPageItem(Troll troll) {
        String path = PATH + troll.itemKey();
        if (troll.kind() == Troll.Kind.TOGGLE) path += "." + troll.isActive(targetUUID);
        return lgm.getItem(path, playerMenuUtility.getOwner(), false);
    }

    /**
     * Switches off every troll of a player: the toggles of the troll menu and the trolls of the registry.
     *
     * @param staff the player who pressed the button
     * @param target the trolled player, may be {@code null} if he is offline
     * @param targetName the name shown in the message
     */
    static void stopAllTrolls(Player staff, Player target, String targetName) {
        AdminPanelMain plugin = AdminPanelMain.getPlugin();
        if (target == null) {
            CommandText.send(plugin.getLanguageManager(), staff, "Player.General.TargetedPlayerIsNull");
            return;
        }
        plugin.getPluginStateManager().clearTrollState(target.getUniqueId());
        plugin.getTrollRegistry().stopAll(target);
        CommandText.send(plugin.getLanguageManager(), staff, "Trolls.StoppedAll", "%target%", targetName);
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
