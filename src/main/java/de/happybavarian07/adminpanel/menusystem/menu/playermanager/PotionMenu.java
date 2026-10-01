package de.happybavarian07.adminpanel.menusystem.menu.playermanager;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.player.GiveEffectToPlayerEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.playeractions.EffectTypes;
import de.happybavarian07.adminpanel.playeractions.PotionPreset;
import de.happybavarian07.adminpanel.sanctions.Durations;
import de.happybavarian07.coolstufflib.languagemanager.PlaceholderType;
import de.happybavarian07.coolstufflib.menusystem.PaginatedMenu;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Locale;

/**
 * Gives potion effects to the targeted player and takes them away again. A click acts on the clicked effect.
 * The duration and the level are typed into the chat, presets apply a whole set of effects with one click.
 */
public class PotionMenu extends PaginatedMenu<PotionEffectType> {
    private static final String PERMISSION = "AdminPanel.PlayerManager.PlayerSettings.Actions.Potions";
    private static final String PATH = "PlayerManager.PotionMenu.";
    private static final String ACTIONS_PATH = "PlayerManager.ActionsMenu.";
    private static final int MAX_SECONDS = 50_000;
    private static final int MAX_LEVEL = 255;
    private static final int[] PRESET_SLOTS = {1, 2, 3, 4, 5};

    private final List<PotionEffectType> allTypes = EffectTypes.all();
    private int durationSeconds = 30;
    private int level = 1;
    private String search = "";
    private EffectTypes.Category category;

    public PotionMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission(PERMISSION);
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("PlayerManager.PotionMenu", playerMenuUtility.getTarget());
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "PotionMenu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void preSetMenuItems() {
        String needle = search.toLowerCase(Locale.ROOT);
        List<PotionEffectType> entries = allTypes.stream()
                .filter(type -> category == null || EffectTypes.category(type) == category)
                .filter(type -> needle.isEmpty() || EffectTypes.key(type).contains(needle))
                .toList();
        setPaginatedData(entries, this::getPageItem, this::handleEntryClick);
        int perPage = getPaginatedItemSlots().length;
        if (perPage > 0) page = Math.min(page, Math.max(0, (entries.size() - 1) / perPage));
    }

    @Override
    public void postSetMenuItems() {
        Player owner = playerMenuUtility.getOwner();
        PotionPreset[] presets = PotionPreset.values();
        for (int i = 0; i < presets.length && i < PRESET_SLOTS.length; i++) {
            PotionPreset preset = presets[i];
            String presetPath = PATH + "Presets." + preset.languageKey();
            registerButton(getSlot(presetPath, PRESET_SLOTS[i]), lgm.getItem(presetPath, owner, false),
                    MenuAction.of((p, e) -> applyPreset(p, preset)).requires(PERMISSION));
        }

        lgm.addPlaceholder(PlaceholderType.ITEM, "%search%", search.isEmpty() ? "-" : search, false);
        MenuAction promptSearch = MenuAction.prompt(CommandText.get(lgm, owner, "PotionMenu.EnterSearch"), (p, text) -> {
            search = text.trim().equals("-") ? "" : text.trim();
            page = 0;
            CommandText.send(lgm, p, search.isEmpty() ? "PotionMenu.SearchCleared" : "PotionMenu.SearchSet", "%search%", search);
            open();
        });
        registerButton(getSlot(PATH + "Search", 6), lgm.getItem(PATH + "Search", owner, true), (p, e) -> {
            if (e.isRightClick()) {
                search = "";
                page = 0;
                refresh();
            } else {
                promptSearch.execute(p, e);
            }
        });

        String filterPath = PATH + "Filter." + (category == null ? "All" : categoryKey(category));
        registerButton(getSlot(PATH + "Filter", 7), lgm.getItem(filterPath, owner, false),
                MenuAction.of((p, e) -> {
                    category = nextCategory(category);
                    page = 0;
                }).then((p, e) -> refresh()));

        lgm.addPlaceholder(PlaceholderType.ITEM, "%duration%", Durations.format(durationSeconds * 1000L), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%amplifier%", level, false);
        registerButton(getSlot(ACTIONS_PATH + "SetDuration", 45), lgm.getItem(ACTIONS_PATH + "SetDuration", owner, false),
                MenuAction.prompt(CommandText.get(lgm, owner, "PotionMenu.EnterDuration"), this::setDuration).requires(PERMISSION));
        registerButton(getSlot(ACTIONS_PATH + "SetAmplifier", 46), lgm.getItem(ACTIONS_PATH + "SetAmplifier", owner, true),
                MenuAction.prompt(CommandText.get(lgm, owner, "PotionMenu.EnterAmplifier"), this::setLevel).requires(PERMISSION));
        registerButton(getSlot(ACTIONS_PATH + "ClearPotions", 47), lgm.getItem(ACTIONS_PATH + "ClearPotions", owner, false),
                MenuAction.of((p, e) -> clearEffects(p)).then((p, e) -> refresh()).requires(PERMISSION));
        registerButton(getSlot("General.Close", 53), lgm.getItem("General.Close", owner, false),
                MenuAction.of((p, e) -> new PlayerActionsMenu(AdminPanelMain.getAPI().getPlayerMenuUtility(p)).open())
                        .requires("AdminPanel.Button.Close"));
    }

    private void handleEntryClick(Player staff, PotionEffectType type, InventoryClickEvent event) {
        MenuAction.of((p, e) -> {
            Player target = playerMenuUtility.getTarget();
            if (target == null) {
                CommandText.send(lgm, p, "Player.General.TargetedPlayerIsNull");
                return;
            }
            if (e.isRightClick()) {
                if (target.hasPotionEffect(type)) {
                    target.removePotionEffect(type);
                    CommandText.send(lgm, p, "PotionMenu.EffectRemoved", "%type%", EffectTypes.name(type), "%target%", target.getName());
                }
            } else if (e.isLeftClick() && addEffect(target, type, durationSeconds, level)) {
                CommandText.send(lgm, p, "PotionMenu.EffectAdded", "%type%", EffectTypes.name(type),
                        "%duration%", Durations.format(durationSeconds * 1000L), "%amplifier%", level, "%target%", target.getName());
            }
            refresh();
        }).requires(PERMISSION).execute(staff, event);
    }

    private boolean addEffect(Player target, PotionEffectType type, int seconds, int effectLevel) {
        PotionEffect effect = new PotionEffect(type, seconds * 20, effectLevel - 1, false, false, false);
        GiveEffectToPlayerEvent event = new GiveEffectToPlayerEvent(target, effect);
        try {
            AdminPanelMain.getAPI().callAdminPanelEvent(event);
        } catch (NotAPanelEventException e) {
            AdminPanelMain.logError("The potion effect action was not a valid panel event", e);
            return false;
        }
        if (event.isCancelled()) return false;
        try {
            return target.addPotionEffect(effect);
        } catch (IllegalArgumentException refused) {
            return false;
        }
    }

    private void applyPreset(Player staff, PotionPreset preset) {
        Player target = playerMenuUtility.getTarget();
        if (target == null) {
            CommandText.send(lgm, staff, "Player.General.TargetedPlayerIsNull");
            return;
        }
        int applied = 0;
        for (PotionPreset.Effect effect : preset.effects()) {
            PotionEffectType type = EffectTypes.byKey(effect.key());
            if (type != null && addEffect(target, type, effect.seconds(), effect.level())) applied++;
        }
        CommandText.send(lgm, staff, applied > 0 ? "PotionMenu.PresetApplied" : "PotionMenu.PresetFailed",
                "%preset%", CommandText.get(lgm, staff, "PotionMenu.Preset." + preset.languageKey()), "%target%", target.getName());
        refresh();
    }

    private void clearEffects(Player staff) {
        Player target = playerMenuUtility.getTarget();
        if (target == null) {
            CommandText.send(lgm, staff, "Player.General.TargetedPlayerIsNull");
            return;
        }
        for (PotionEffect effect : target.getActivePotionEffects()) {
            target.removePotionEffect(effect.getType());
        }
        CommandText.send(lgm, staff, "PotionMenu.EffectsCleared", "%target%", target.getName());
    }

    private void setDuration(Player player, String text) {
        try {
            long seconds = Durations.parse(text) / 1000;
            if (seconds < 1 || seconds > MAX_SECONDS) throw new IllegalArgumentException("out of range");
            durationSeconds = (int) seconds;
            CommandText.send(lgm, player, "PotionMenu.DurationSet", "%duration%", Durations.format(seconds * 1000));
        } catch (IllegalArgumentException invalid) {
            CommandText.send(lgm, player, "PotionMenu.NotADuration", "%max%", Durations.format(MAX_SECONDS * 1000L));
        }
        open();
    }

    private void setLevel(Player player, String text) {
        try {
            int typed = Integer.parseInt(text.trim());
            if (typed < 1 || typed > MAX_LEVEL) throw new NumberFormatException("out of range");
            level = typed;
            CommandText.send(lgm, player, "PotionMenu.AmplifierSet", "%amplifier%", level);
        } catch (NumberFormatException invalid) {
            CommandText.send(lgm, player, "PotionMenu.NotANumber", "%max%", MAX_LEVEL);
        }
        open();
    }

    public ItemStack getPageItem(PotionEffectType type) {
        Player viewer = playerMenuUtility.getOwner();
        Player target = playerMenuUtility.getTarget();
        boolean active = target != null && target.hasPotionEffect(type);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%effect%", EffectTypes.name(type), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%category%",
                CommandText.get(lgm, viewer, "PotionMenu.Category." + categoryKey(EffectTypes.category(type))), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%duration%", Durations.format(durationSeconds * 1000L), false);
        lgm.addPlaceholder(PlaceholderType.ITEM, "%amplifier%", level, false);
        return lgm.getItem(PATH + "EffectItem." + (active ? "Active" : "Inactive"), viewer, true);
    }

    private static EffectTypes.Category nextCategory(EffectTypes.Category current) {
        if (current == null) return EffectTypes.Category.BENEFICIAL;
        return switch (current) {
            case BENEFICIAL -> EffectTypes.Category.HARMFUL;
            case HARMFUL -> EffectTypes.Category.NEUTRAL;
            case NEUTRAL -> null;
        };
    }

    private static String categoryKey(EffectTypes.Category value) {
        String name = value.name();
        return name.charAt(0) + name.substring(1).toLowerCase(Locale.ROOT);
    }

    public void handleOpenMenu(InventoryOpenEvent e) {
    }

    public void handleCloseMenu(InventoryCloseEvent e) {
    }
}
