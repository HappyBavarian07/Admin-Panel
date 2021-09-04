package de.happybavarian07.menusystem.menu.worldmanager;

import de.happybavarian07.events.NotAPanelEventException;
import de.happybavarian07.events.world.WorldCreateEvent;
import de.happybavarian07.main.LanguageManager;
import de.happybavarian07.main.Main;
import de.happybavarian07.menusystem.Menu;
import de.happybavarian07.menusystem.PlayerMenuUtility;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;

public class WorldCreateMenu extends Menu implements Listener {
    private final Main plugin = Main.getPlugin();
    private final LanguageManager lgm = plugin.getLanguageManager();
    private String worldName = "Name";
    private WorldType worldType = WorldType.NORMAL;
    private World.Environment worldEnvironment = World.Environment.NORMAL;
    private boolean hardcore = false;
    private boolean generateStructures = false;

    public WorldCreateMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        setOpeningPermission("AdminPanel.WorldManagment.Create");
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("WorldManager.WorldCreateMenu", null);
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        String itemPath = "WorldManager.CreateMenu.";
        ItemStack nameItem = lgm.getItem(itemPath + "Name", player);
        ItemMeta nameMeta = nameItem.getItemMeta();
        List<String> nameLore = new ArrayList<>();
        for (String string : nameItem.getItemMeta().getLore()) {
            nameLore.add(string.replace("%name%", this.worldName));
        }
        nameMeta.setLore(nameLore);
        nameItem.setItemMeta(nameMeta);

        ItemStack typeItem = lgm.getItem(itemPath + "Type." + worldType.toString(), player);
        ItemMeta typeMeta = typeItem.getItemMeta();
        List<String> typeLore = new ArrayList<>();
        for (String string : typeMeta.getLore()) {
            typeLore.add(string.replace("%type%", this.worldType.toString()));
        }
        typeMeta.setLore(typeLore);
        typeItem.setItemMeta(typeMeta);

        ItemStack environmentItem = lgm.getItem(itemPath + "Environment." + worldEnvironment.toString(), player);
        ItemMeta environmentMeta = environmentItem.getItemMeta();
        List<String> environmentLore = new ArrayList<>();
        for (String string : environmentMeta.getLore()) {
            environmentLore.add(string.replace("%environment%", this.worldEnvironment.toString()));
        }
        environmentMeta.setLore(environmentLore);
        environmentItem.setItemMeta(environmentMeta);

        String noPerms = lgm.getMessage("Player.General.NoPermissions", player);

        if (item == null || !item.hasItemMeta()) return;
        if (item.equals(nameItem)) {
            player.setMetadata("createWorldSetName", new FixedMetadataValue(plugin, true));
            player.sendMessage(lgm.getMessage("Player.WorldManager.CreateMenu.Name", player));
            player.closeInventory();
        } else if (item.equals(typeItem)) {
            if(this.worldType == WorldType.NORMAL) {
                this.worldType = WorldType.FLAT;
            } else if (this.worldType == WorldType.FLAT) {
                this.worldType = WorldType.AMPLIFIED;
            } else if (this.worldType == WorldType.AMPLIFIED) {
                this.worldType = WorldType.LARGE_BIOMES;
            } else if (this.worldType == WorldType.LARGE_BIOMES) {
                this.worldType = WorldType.NORMAL;
            }
            super.open();
        } else if (item.equals(environmentItem)) {
            if(this.worldEnvironment == World.Environment.NORMAL) {
                this.worldEnvironment = World.Environment.NETHER;
            } else if (this.worldEnvironment == World.Environment.NETHER) {
                this.worldEnvironment = World.Environment.THE_END;
            } else if (this.worldEnvironment == World.Environment.THE_END) {
                this.worldEnvironment = World.Environment.NORMAL;
            }
            super.open();
        } else if (item.equals(lgm.getItem(itemPath + "GenerateStructures.true", player))) {
            this.generateStructures = false;
            super.open();
        } else if (item.equals(lgm.getItem(itemPath + "GenerateStructures.false", player))) {
            this.generateStructures = true;
            super.open();
        } else if (item.equals(lgm.getItem(itemPath + "Hardcore.true", player))) {
            this.hardcore = false;
            super.open();
        } else if (item.equals(lgm.getItem(itemPath + "Hardcore.false", player))) {
            this.hardcore = true;
            super.open();
        } else if (item.equals(lgm.getItem("General.Close", player))) {
            if(!player.hasPermission("AdminPanel.Button.Close")) {
                player.sendMessage(noPerms);
                return;
            }
            new WorldSelectMenu(playerMenuUtility).open();
        } else if (item.equals(lgm.getItem(itemPath + "CreateWorld", player))) {
            WorldCreateEvent worldCreateEvent = new WorldCreateEvent(player, worldName, worldType, worldEnvironment, generateStructures, hardcore);
            try {
                Main.getAPI().callAdminPanelEvent(worldCreateEvent);
                if(!worldCreateEvent.isCancelled()) {
                    WorldCreator worldCreator = new WorldCreator(worldCreateEvent.getName());
                    worldCreator.type(worldCreateEvent.getWorldType());
                    worldCreator.environment(worldCreateEvent.getWorldEnvironment());
                    worldCreator.generateStructures(worldCreateEvent.isGenerateStructures());
                    worldCreator.hardcore(worldCreateEvent.isHardcore());
                    World world = worldCreator.createWorld();
                    player.closeInventory();
                    assert world != null;
                    player.teleport(world.getSpawnLocation());
                }
            } catch (NotAPanelEventException notAPanelEventException) {
                notAPanelEventException.printStackTrace();
            }

        }
    }

    @Override
    public void setMenuItems() {
        Player player = playerMenuUtility.getOwner();
        String itemPath = "WorldManager.CreateMenu.";
        setFillerGlass();
        ItemStack nameItem = lgm.getItem(itemPath + "Name", player);
        ItemMeta nameMeta = nameItem.getItemMeta();
        List<String> nameLore = new ArrayList<>();
        for (String string : nameMeta.getLore()) {
            nameLore.add(string.replace("%name%", this.worldName));
        }
        nameMeta.setLore(nameLore);
        nameItem.setItemMeta(nameMeta);
        inventory.setItem(10, nameItem);

        ItemStack typeItem = lgm.getItem(itemPath + "Type." + worldType.toString(), player);
        ItemMeta typeMeta = typeItem.getItemMeta();
        List<String> typeLore = new ArrayList<>();
        for (String string : typeMeta.getLore()) {
            typeLore.add(string.replace("%type%", this.worldType.toString()));
        }
        typeMeta.setLore(typeLore);
        typeItem.setItemMeta(typeMeta);
        inventory.setItem(12, typeItem);

        ItemStack environmentItem = lgm.getItem(itemPath + "Environment." + worldEnvironment.toString(), player);
        ItemMeta environmentMeta = environmentItem.getItemMeta();
        List<String> environmentLore = new ArrayList<>();
        for (String string : environmentMeta.getLore()) {
            environmentLore.add(string.replace("%environment%", this.worldEnvironment.toString()));
        }
        environmentMeta.setLore(environmentLore);
        environmentItem.setItemMeta(environmentMeta);
        inventory.setItem(14, environmentItem);

        if(generateStructures) {
            inventory.setItem(16, lgm.getItem(itemPath + "GenerateStructures.true", player));
        } else {
            inventory.setItem(16, lgm.getItem(itemPath + "GenerateStructures.false", player));
        }

        if(hardcore) {
            inventory.setItem(22, lgm.getItem(itemPath + "Hardcore.true", player));
        } else {
            inventory.setItem(22, lgm.getItem(itemPath + "Hardcore.false", player));
        }

        inventory.setItem(26, lgm.getItem("General.Close", player));
        inventory.setItem(18, lgm.getItem(itemPath + "CreateWorld", player));


    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata("createWorldSetName")) {
            this.worldName = event.getMessage().replace(" ", "-");
            player.removeMetadata("createWorldSetName", plugin);
            super.open();
            event.setCancelled(true);
        }
    }
}
