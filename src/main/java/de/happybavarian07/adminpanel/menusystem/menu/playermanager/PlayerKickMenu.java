package de.happybavarian07.adminpanel.menusystem.menu.playermanager;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.adminpanel.sanctions.BanSettings;
import de.happybavarian07.adminpanel.sanctions.PlayerSanctions;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

/**
 * Kicks the selected player. The reason comes from a preset of {@code Pman.Actions.Ban.Presets} or from the chat,
 * a silent kick (no broadcast) can be switched on, it defaults to {@code Pman.Actions.Kick.Silent}.
 */
public class PlayerKickMenu extends Menu {
    private static final String KICK_PERMISSION = "AdminPanel.PlayerManager.PlayerSettings.Kick";
    private static final String PATH = "PlayerManager.ActionsMenu.KickMenu.";
    private static final String MESSAGE_PATH = "Player.PlayerManager.KickMenu.";
    private static final int[] PRESET_SLOTS = {1, 2, 3, 4, 5, 6, 7};

    private final BanSettings settings = PlayerSanctions.settings();
    private String reason = "";
    private boolean silent;

    public PlayerKickMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.OpenMenu.Kick");
        this.silent = AdminPanelMain.getPlugin().getConfig().getBoolean("Pman.Actions.Kick.Silent", false);
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.KickMenu", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PlayerKickMenu";
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    @Override
    public void handleCloseMenu(InventoryCloseEvent e) {
    }

    @Override
    public void setMenuItems() {
        setFillerGlass();
        Player player = playerMenuUtility.getOwner();
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerMenuUtility.getTargetUUID());
        String targetName = target.getName() != null ? target.getName() : playerMenuUtility.getTargetUUID().toString();

        for (int i = 0; i < settings.presets().size() && i < PRESET_SLOTS.length; i++) {
            BanSettings.Preset preset = settings.presets().get(i);
            lgm.addPlaceholder(PlaceholderType.ITEM, "%name%", preset.name(), false);
            lgm.addPlaceholder(PlaceholderType.ITEM, "%reason%", preset.reason(), false);
            lgm.addPlaceholder(PlaceholderType.ITEM, "%marker%", reason.equals(preset.reason()) ? "&a> " : "", false);
            registerButton(PRESET_SLOTS[i], lgm.getItem(PATH + "Preset", player, false),
                    MenuAction.of((p, e) -> reason = preset.reason()).then((p, e) -> refresh()));
        }

        lgm.addPlaceholder(PlaceholderType.ITEM, "%reason%", reasonText(), false);
        registerButton(getSlot(PATH + "Reason", 10), lgm.getItem(PATH + "Reason", player, false),
                MenuAction.prompt(CommandText.get(lgm, player, MESSAGE_PATH + "Reason.EnterNewReason"), (p, text) -> {
                    reason = text.trim();
                    CommandText.send(lgm, p, MESSAGE_PATH + "Reason.NewReasonSet", "%reason%", reason);
                    open();
                }));
        registerButton(getSlot(PATH + "Silent", 12), lgm.getItem(PATH + "Silent." + (silent ? "Enabled" : "Disabled"), player, false),
                MenuAction.of((p, e) -> silent = !silent).then((p, e) -> refresh()));
        registerButton(getSlot(PATH + "Kick", 16), lgm.getItem(PATH + "Kick", player, true),
                MenuAction.of((p, e) -> kick(p, target))
                        .confirm(CommandText.get(lgm, player, MESSAGE_PATH + "ConfirmKick", "%target%", targetName,
                                "%reason%", reasonText()))
                        .requires(KICK_PERMISSION));

        registerButton(getSlot("General.Close", 26), lgm.getItem("General.Close", player, false),
                MenuAction.of((p, e) -> new PlayerActionSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p)).open())
                        .requires("AdminPanel.Button.Close"));
    }

    private void kick(Player staff, OfflinePlayer target) {
        switch (PlayerSanctions.kick(staff, target, reason, silent)) {
            case KICKED -> {
                CommandText.send(lgm, staff, MESSAGE_PATH + "SuccessfullyKicked");
                reason = "";
                new PlayerSelectMenu(playerMenuUtility, new AdminPanelStartMenu(playerMenuUtility)).open();
            }
            case EXEMPT -> CommandText.send(lgm, staff, MESSAGE_PATH + "NotKickable");
            case OFFLINE -> CommandText.send(lgm, staff, MESSAGE_PATH + "NotOnline");
        }
    }

    private String reasonText() {
        return reason.isBlank() ? "-" : reason;
    }
}
