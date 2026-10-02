package de.happybavarian07.adminpanel.menusystem.menu.playermanager;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.adminpanel.sanctions.BanSettings;
import de.happybavarian07.adminpanel.sanctions.Durations;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.concurrent.TimeUnit;

/**
 * Bans the selected player. The reason comes from a preset of the config or from the chat, the length from one of
 * the configured durations or from the chat. An IP ban and a silent ban (no broadcast) can be switched on here, both
 * default to the values of {@code Pman.Actions.Ban}.
 */
public class PlayerBanMenu extends Menu {
    private static final String BAN_PERMISSION = "AdminPanel.PlayerManager.PlayerSettings.Ban";
    private static final String IP_PERMISSION = "AdminPanel.PlayerManager.PlayerSettings.BanIp";
    private static final String PATH = "PlayerManager.ActionsMenu.BanMenu.";
    private static final String MESSAGE_PATH = "Player.PlayerManager.BanMenu.";
    private static final long MAX_BAN_MILLIS = TimeUnit.DAYS.toMillis(36_500);
    private static final int[] PRESET_SLOTS = {10, 11, 12, 13, 14, 15, 16};
    private static final int[] DURATION_SLOTS = {19, 20, 21, 22, 23, 24, 25};

    private final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    private final BanSettings settings = PlayerSanctions.settings();
    private String reason = "";
    private long durationMillis;
    private boolean ipBan;
    private boolean silent;

    public PlayerBanMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.PlayerManager.PlayerSettings.OpenMenu.Ban");
        this.ipBan = plugin.getConfig().getBoolean("Pman.Actions.Ban.IpBan", false);
        this.silent = plugin.getConfig().getBoolean("Pman.Actions.Ban.Silent", false);
        this.durationMillis = settings.lengths().isEmpty() ? 0 : settings.lengths().get(0).millis();
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.BanMenu", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PlayerBanMenu";
    }

    @Override
    public int getSlots() {
        return 45;
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

        lgm.addPlaceholder(PlaceholderType.ITEM, "%target%", targetName, false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%reason%", reasonText(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%time%", durationText(player), false);
        ItemStack summary = lgm.getItem(PATH + "Summary", player, false);
        if (summary.getItemMeta() instanceof SkullMeta meta) {
            meta.setOwningPlayer(target);
            summary.setItemMeta(meta);
        }
        inventory.setItem(getSlot(PATH + "Summary", 4), summary);

        for (int i = 0; i < settings.presets().size() && i < PRESET_SLOTS.length; i++) {
            BanSettings.Preset preset = settings.presets().get(i);
            lgm.addPlaceholder(PlaceholderType.ITEM, "%name%", preset.name(), false);
            lgm.addPlaceholder(PlaceholderType.ITEM, "%reason%", preset.reason(), false);
            lgm.addPlaceholder(PlaceholderType.ITEM, "%marker%", marker(reason.equals(preset.reason())), false);
            registerButton(PRESET_SLOTS[i], lgm.getItem(PATH + "Preset", player, false),
                    MenuAction.of((p, e) -> reason = preset.reason()).then((p, e) -> refresh()));
        }
        for (int i = 0; i < settings.lengths().size() && i < DURATION_SLOTS.length; i++) {
            BanSettings.Length length = settings.lengths().get(i);
            lgm.addPlaceholder(PlaceholderType.ITEM, "%label%", length.millis() > 0 ? length.label() : permanentText(player), false);
            lgm.addPlaceholder(PlaceholderType.ITEM, "%marker%", marker(durationMillis == length.millis()), false);
            registerButton(DURATION_SLOTS[i], lgm.getItem(PATH + "Duration", player, false),
                    MenuAction.of((p, e) -> durationMillis = length.millis()).then((p, e) -> refresh()));
        }

        lgm.addPlaceholder(PlaceholderType.ITEM, "%reason%", reasonText(), false);
        registerButton(getSlot(PATH + "Reason", 28), lgm.getItem(PATH + "Reason", player, false),
                MenuAction.prompt(CommandText.get(lgm, player, MESSAGE_PATH + "Reason.EnterNewReason"), (p, text) -> {
                    reason = text.trim();
                    CommandText.send(lgm, p, MESSAGE_PATH + "Reason.NewReasonSet", "%reason%", reason);
                    open();
                }));
        lgm.addPlaceholder(PlaceholderType.ITEM, "%time%", durationText(player), false);
        registerButton(getSlot(PATH + "DurationCustom", 29), lgm.getItem(PATH + "DurationCustom", player, false),
                MenuAction.prompt(CommandText.get(lgm, player, MESSAGE_PATH + "EnterDuration"), this::setDuration));

        registerButton(getSlot(PATH + "IpBan", 31), lgm.getItem(PATH + "IpBan." + (ipBan ? "Enabled" : "Disabled"), player, false),
                MenuAction.of((p, e) -> ipBan = !ipBan).then((p, e) -> refresh()).requires(IP_PERMISSION));
        registerButton(getSlot(PATH + "Silent", 32), lgm.getItem(PATH + "Silent." + (silent ? "Enabled" : "Disabled"), player, false),
                MenuAction.of((p, e) -> silent = !silent).then((p, e) -> refresh()));

        lgm.addPlaceholder(PlaceholderType.ITEM, "%reason%", reasonText(), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%time%", durationText(player), false);
        registerButton(getSlot(PATH + "Ban", 40), lgm.getItem(PATH + "Ban", player, true),
                MenuAction.of((p, e) -> ban(p, target))
                        .confirm(CommandText.get(lgm, player, MESSAGE_PATH + "ConfirmBan", "%target%", targetName,
                                "%time%", durationText(player), "%reason%", reasonText()))
                        .requires(BAN_PERMISSION));

        registerButton(getSlot("General.Close", 44), lgm.getItem("General.Back", player, false),
                MenuAction.of((p, e) -> new PlayerActionSelectMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p)).open())
                        .requires("AdminPanel.Button.Close"));
    }

    private void setDuration(Player player, String text) {
        try {
            long millis = Durations.parse(text);
            if (millis > MAX_BAN_MILLIS) throw new IllegalArgumentException("too long");
            durationMillis = millis;
            CommandText.send(lgm, player, MESSAGE_PATH + "DurationSet", "%duration%", durationText(player));
        } catch (IllegalArgumentException invalid) {
            CommandText.send(lgm, player, MESSAGE_PATH + "NotADuration");
        }
        open();
    }

    private void ban(Player staff, OfflinePlayer target) {
        PlayerSanctions.BanResult result = PlayerSanctions.ban(staff, target, reason, durationMillis, ipBan, silent);
        switch (result) {
            case BANNED, BANNED_IP_UNKNOWN -> {
                CommandText.send(lgm, staff, MESSAGE_PATH + "SuccessfullyBanned");
                if (result == PlayerSanctions.BanResult.BANNED_IP_UNKNOWN) CommandText.send(lgm, staff, MESSAGE_PATH + "IpUnknown");
                reason = "";
                new PlayerSelectMenu(playerMenuUtility, new AdminPanelStartMenu(playerMenuUtility)).open();
            }
            case EXEMPT -> CommandText.send(lgm, staff, MESSAGE_PATH + "NotBannable");
            case ALREADY_BANNED -> CommandText.send(lgm, staff, MESSAGE_PATH + "AlreadyBanned");
            case UNKNOWN_PLAYER -> CommandText.send(lgm, staff, MESSAGE_PATH + "UnknownPlayer");
        }
    }

    private String reasonText() {
        return reason.isBlank() ? "-" : reason;
    }

    private String durationText(Player viewer) {
        return durationMillis > 0 ? Durations.format(durationMillis) : permanentText(viewer);
    }

    private String permanentText(Player viewer) {
        return CommandText.get(lgm, viewer, "Warnings.Format.Permanent");
    }

    private static String marker(boolean selected) {
        return selected ? "&a> " : "";
    }
}
