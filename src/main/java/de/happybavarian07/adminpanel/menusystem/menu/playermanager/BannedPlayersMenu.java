package de.happybavarian07.adminpanel.menusystem.menu.playermanager;

import de.happybavarian07.adminpanel.journal.AdminJournal;
import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.sanctions.BanInfo;
import de.happybavarian07.adminpanel.sanctions.Durations;
import de.happybavarian07.adminpanel.warnings.WarningText;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

/**
 * The server's ban list with reason, who banned, when and the time left. A click unbans the clicked entry.
 */
public class BannedPlayersMenu extends PaginatedMenu<BanInfo> {
    private static final String UNBAN_PERMISSION = "AdminPanel.PlayerManager.PlayerSettings.Unban";
    private static final String PATH = "PlayerManager.BannedPlayersMenu.";
    private static final String MESSAGE_PATH = "Player.PlayerManager.BanMenu.";

    public BannedPlayersMenu(PlayerMenuUtility playerMenuUtility, Menu savedMenu) {
        super(playerMenuUtility, savedMenu);
        setOpeningPermission("AdminPanel.PlayerManager.BannedPlayers");
        setSearchable(info -> info.target() + " " + info.reason());
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.BannedPlayers", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "BannedPlayersMenu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void preSetMenuItems() {
        List<BanInfo> entries = BanInfo.readAll();
        setPaginatedData(entries, this::getPageItem, this::handleEntryClick);
        int perPage = getPaginatedItemSlots().length;
        if (perPage > 0) page = Math.min(page, Math.max(0, (paginatedData.size() - 1) / perPage));
    }

    private void handleEntryClick(Player staff, BanInfo info, InventoryClickEvent event) {
        MenuAction.of((p, e) -> {
            BanList<?> list = Bukkit.getBanList(info.ip() ? BanList.Type.IP : BanList.Type.NAME);
            if (list.isBanned(info.target())) {
                list.pardon(info.target());
                AdminJournal.log(p, info.ip() ? "UNBAN_IP" : "UNBAN", info.target(), info.reason());
                CommandText.send(lgm, p, MESSAGE_PATH + "SuccessfullyUnbanned", "%target%", info.target());
            } else {
                CommandText.send(lgm, p, MESSAGE_PATH + "NotBanned", "%target%", info.target());
            }
            refresh();
        }).requires(UNBAN_PERMISSION).execute(staff, event);
    }

    public ItemStack getPageItem(BanInfo info) {
        Player viewer = playerMenuUtility.getOwner();
        String permanent = CommandText.get(lgm, viewer, "Warnings.Format.Permanent");
        lgm.addPlaceholder(PlaceholderType.ITEM, "%target%", info.target(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%reason%", info.reason().isBlank() ? "-" : info.reason(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%source%", info.source().isBlank() ? "-" : info.source(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%created%", info.created() > 0
                ? WarningText.date(lgm, viewer, info.created()) : CommandText.get(lgm, viewer, "Warnings.Format.Unknown"), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%expires%", info.permanent()
                ? permanent : WarningText.date(lgm, viewer, info.expires()), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%left%", info.permanent()
                ? permanent : Durations.format(info.remainingMillis(System.currentTimeMillis())), false);
        ItemStack item = lgm.getItem(PATH + (info.ip() ? "EntryIp" : "Entry"), viewer, true);
        if (!info.ip() && info.uuid() != null && item.getItemMeta() instanceof SkullMeta meta) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(info.uuid()));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
