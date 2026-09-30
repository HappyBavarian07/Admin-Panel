package de.happybavarian07.adminpanel.menusystem.menu.worldmanager;

import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.world.WorldCreateEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import de.happybavarian07.coolstufflib.menusystem.Menu;
import de.happybavarian07.coolstufflib.menusystem.actions.PermissionAction;
import de.happybavarian07.coolstufflib.menusystem.PlayerMenuUtility;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class WorldCreateMenu extends Menu implements Listener {
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
    public void handleMenu(InventoryClickEvent e) {
        // Logic is now handled by registered MenuActions in setMenuItems
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
        registerButton(getSlot(path + "Name", 10), nameItem, (p, e) -> {
            playerMenuUtility.addData("createWorldSetName", true);
            p.sendMessage(lgm.getMessage("Player.WorldManager.CreateMenu.Name", p, true));
            p.closeInventory();
        }, null);

        ItemStack typeItem = lgm.getItem(path + "Type." + worldType.toString(), player, false);
        ItemMeta typeMeta = typeItem.getItemMeta();
        List<String> typeLore = new ArrayList<>();
        for (String string : typeMeta.getLore()) {
            typeLore.add(string.replace("%type%", this.worldType.toString()));
        }
        typeMeta.setLore(typeLore);
        typeItem.setItemMeta(typeMeta);
        registerButton(getSlot(path + "Type." + worldType.toString(), 12), typeItem, (p, e) -> {
            if (this.worldType == WorldType.NORMAL) this.worldType = WorldType.FLAT;
            else if (this.worldType == WorldType.FLAT) this.worldType = WorldType.AMPLIFIED;
            else if (this.worldType == WorldType.AMPLIFIED) this.worldType = WorldType.LARGE_BIOMES;
            else this.worldType = WorldType.NORMAL;
            super.open();
        }, null);

        ItemStack environmentItem = lgm.getItem(path + "Environment." + worldEnvironment.toString(), player, false);
        ItemMeta environmentMeta = environmentItem.getItemMeta();
        List<String> environmentLore = new ArrayList<>();
        for (String string : environmentMeta.getLore()) {
            environmentLore.add(string.replace("%environment%", this.worldEnvironment.toString()));
        }
        environmentMeta.setLore(environmentLore);
        environmentItem.setItemMeta(environmentMeta);
        registerButton(getSlot(path + "Environment." + worldEnvironment.toString(), 14), environmentItem, (p, e) -> {
            if (this.worldEnvironment == World.Environment.NORMAL) this.worldEnvironment = World.Environment.NETHER;
            else if (this.worldEnvironment == World.Environment.NETHER) this.worldEnvironment = World.Environment.THE_END;
            else this.worldEnvironment = World.Environment.NORMAL;
            super.open();
        }, null);

        if (generateStructures) {
            registerButton(getSlot(path + "GenerateStructures.true", 16), lgm.getItem(path + "GenerateStructures.true", player, false),
                    (p, e) -> { this.generateStructures = false; super.open(); }, null);
        } else {
            registerButton(getSlot(path + "GenerateStructures.false", 16), lgm.getItem(path + "GenerateStructures.false", player, false),
                    (p, e) -> { this.generateStructures = true; super.open(); }, null);
        }

        if (hardcore) {
            registerButton(getSlot(path + "Hardcore.true", 22), lgm.getItem(path + "Hardcore.true", player, false),
                    (p, e) -> { this.hardcore = false; super.open(); }, null);
        } else {
            registerButton(getSlot(path + "Hardcore.false", 22), lgm.getItem(path + "Hardcore.false", player, false),
                    (p, e) -> { this.hardcore = true; super.open(); }, null);
        }

        registerButton(getSlot("General.Close", 26), lgm.getItem("General.Close", player, false),
                new PermissionAction("AdminPanel.Button.Close", (p, e) ->
                        new WorldSelectMenu(playerMenuUtility, new AdminPanelStartMenu(playerMenuUtility)).open(), lgm), null);
        registerButton(getSlot(path + "CreateWorld", 18), lgm.getItem(path + "CreateWorld", player, false), (p, e) -> {
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
        }, null);


    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (playerMenuUtility.getOwner() != player) return;
        
        if (playerMenuUtility.hasData("createWorldSetName")) {
            this.worldName = event.getMessage().replace(" ", "-");
            playerMenuUtility.removeData("createWorldSetName");
            super.open();
            event.setCancelled(true);
        }
    }
}
