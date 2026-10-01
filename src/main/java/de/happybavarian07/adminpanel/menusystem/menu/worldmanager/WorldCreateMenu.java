package de.happybavarian07.adminpanel.menusystem.menu.worldmanager;

import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.world.WorldCreateEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.actions.MenuAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class WorldCreateMenu extends Menu {
    private String worldName = "Name";
    private WorldType worldType = WorldType.NORMAL;
    private World.Environment worldEnvironment = World.Environment.NORMAL;
    private boolean hardcore = false;
    private boolean generateStructures = false;

    public WorldCreateMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.WorldManager.Create");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("WorldManager.WorldCreateMenu", null);
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "WorldCreateMenu";
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
        Player player = playerMenuUtility.getOwner();
        String path = "WorldManager.CreateMenu.";
        setFillerGlass();
        ItemStack nameItem = lgm.getItem(path + "Name", player, false);
        ItemMeta nameMeta = nameItem.getItemMeta();
        List<String> nameLore = new ArrayList<>();
        for (String string : nameMeta.getLore()) {
            nameLore.add(string.replace("%name%", this.worldName));
        }
        nameMeta.setLore(nameLore);
        nameItem.setItemMeta(nameMeta);
        registerButton(getSlot(path + "Name", 10), nameItem,
                MenuAction.prompt(lgm.getMessage("Player.WorldManager.CreateMenu.Name", player, true), (p, input) -> {
                    this.worldName = input.replace(" ", "-");
                    open();
                }));

        ItemStack typeItem = lgm.getItem(path + "Type." + worldType.toString(), player, false);
        ItemMeta typeMeta = typeItem.getItemMeta();
        List<String> typeLore = new ArrayList<>();
        for (String string : typeMeta.getLore()) {
            typeLore.add(string.replace("%type%", this.worldType.toString()));
        }
        typeMeta.setLore(typeLore);
        typeItem.setItemMeta(typeMeta);
        registerButton(getSlot(path + "Type." + worldType.toString(), 12), typeItem, MenuAction.of((p, e) -> {
            if (this.worldType == WorldType.NORMAL) this.worldType = WorldType.FLAT;
            else if (this.worldType == WorldType.FLAT) this.worldType = WorldType.AMPLIFIED;
            else if (this.worldType == WorldType.AMPLIFIED) this.worldType = WorldType.LARGE_BIOMES;
            else this.worldType = WorldType.NORMAL;
        }).then((p, e) -> refresh()));

        ItemStack environmentItem = lgm.getItem(path + "Environment." + worldEnvironment.toString(), player, false);
        ItemMeta environmentMeta = environmentItem.getItemMeta();
        List<String> environmentLore = new ArrayList<>();
        for (String string : environmentMeta.getLore()) {
            environmentLore.add(string.replace("%environment%", this.worldEnvironment.toString()));
        }
        environmentMeta.setLore(environmentLore);
        environmentItem.setItemMeta(environmentMeta);
        registerButton(getSlot(path + "Environment." + worldEnvironment.toString(), 14), environmentItem, MenuAction.of((p, e) -> {
            if (this.worldEnvironment == World.Environment.NORMAL) this.worldEnvironment = World.Environment.NETHER;
            else if (this.worldEnvironment == World.Environment.NETHER) this.worldEnvironment = World.Environment.THE_END;
            else this.worldEnvironment = World.Environment.NORMAL;
        }).then((p, e) -> refresh()));

        registerButton(getSlot(path + "GenerateStructures." + generateStructures, 16), lgm.getItem(path + "GenerateStructures." + generateStructures, player, false),
                MenuAction.of((p, e) -> this.generateStructures = !this.generateStructures).then((p, e) -> refresh()));

        registerButton(getSlot(path + "Hardcore." + hardcore, 22), lgm.getItem(path + "Hardcore." + hardcore, player, false),
                MenuAction.of((p, e) -> this.hardcore = !this.hardcore).then((p, e) -> refresh()));

        registerButton(getSlot("General.Close", 26), lgm.getItem("General.Close", player, false),
                MenuAction.open(() -> new WorldSelectMenu(playerMenuUtility, new AdminPanelStartMenu(playerMenuUtility)))
                        .requires("AdminPanel.Button.Close"));
        registerButton(getSlot(path + "CreateWorld", 18), lgm.getItem(path + "CreateWorld", player, false), MenuAction.of((p, e) -> {
            WorldCreateEvent worldCreateEvent = new WorldCreateEvent(p, worldName, worldType, worldEnvironment, generateStructures, hardcore);
            try {
                AdminPanelMain.getAPI().callAdminPanelEvent(worldCreateEvent);
                if (!worldCreateEvent.isCancelled()) {
                    WorldCreator worldCreator = new WorldCreator(worldCreateEvent.getName());
                    worldCreator.type(worldCreateEvent.getWorldType());
                    worldCreator.environment(worldCreateEvent.getWorldEnvironment());
                    worldCreator.generateStructures(worldCreateEvent.isGenerateStructures());
                    worldCreator.hardcore(worldCreateEvent.isHardcore());
                    World world = worldCreator.createWorld();
                    p.closeInventory();
                    if (world != null) p.teleport(world.getSpawnLocation());
                }
            } catch (NotAPanelEventException ex) {
                AdminPanelMain.logError("The world create action was not a valid panel event", ex);
            }
        }));


    }
}
