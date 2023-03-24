package de.happybavarian07.adminpanel.menusystem;

import de.happybavarian07.adminpanel.events.NotAPanelEventException;
import de.happybavarian07.adminpanel.events.general.PanelOpenEvent;
import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.main.LanguageManager;
import de.happybavarian07.adminpanel.menusystem.menu.AdminPanelStartMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/*
    Defines the behavior and attributes of all menus in our plugin
 */
public abstract class Menu implements InventoryHolder {

    //Protected values that can be accessed in the menus
    protected final AdminPanelMain plugin = AdminPanelMain.getPlugin();
    protected final LanguageManager lgm = plugin.getLanguageManager();
    protected String openingPermission = "";
    protected PlayerMenuUtility playerMenuUtility;
    protected Inventory inventory;
    protected List<Inventory> inventorys = new ArrayList<>();
    protected ItemStack FILLER = lgm.getItem("General.FillerItem", null, false);

    //Constructor for Menu. Pass in a PlayerMenuUtility so that
    // we have information on who's menu this is and
    // what info is to be transfered
    public Menu(PlayerMenuUtility playerMenuUtility) {
        this.playerMenuUtility = playerMenuUtility;
    }

    //let each menu decide their name
    public abstract String getMenuName();

    public abstract String getConfigMenuAddonFeatureName();

    //let each menu decide their slot amount
    public abstract int getSlots();

    //let each menu decide how the items in the menu will be handled when clicked
    public abstract void handleMenu(InventoryClickEvent e);

    //let each menu decide what items are to be placed in the inventory menu
    public abstract void setMenuItems();

    public String getOpeningPermission() {
        return openingPermission;
    }

    public void setOpeningPermission(String permission) {
        this.openingPermission = permission;
    }

    protected boolean legacyServer() {
        String serverVersion = Bukkit.getServer().getVersion();
        return serverVersion.contains("1.12") ||
                serverVersion.contains("1.11") ||
                serverVersion.contains("1.10") ||
                serverVersion.contains("1.9") ||
                serverVersion.contains("1.8") ||
                serverVersion.contains("1.7");
    }

    //When called, an inventory is created and opened for the player
    public void open() {
        //The owner of the inventory created is the Menu itself,
        // so we are able to reverse engineer the Menu object from the
        // inventoryHolder in the MenuListener class when handling clicks
        if (!playerMenuUtility.getOwner().hasPermission(this.openingPermission)) {
            playerMenuUtility.getOwner().sendMessage(
                    AdminPanelMain.getPlugin().getLanguageManager().getMessage("Player.General.NoPermissions", playerMenuUtility.getOwner(), true));
            playerMenuUtility.getOwner().closeInventory();
            return;
        }

        PanelOpenEvent panelOpenEvent = new PanelOpenEvent(playerMenuUtility.getOwner(), this, playerMenuUtility);
        try {
            AdminPanelMain.getAPI().callAdminPanelEvent(panelOpenEvent);
            if (!panelOpenEvent.isCancelled()) {
                inventory = Bukkit.createInventory(this, getSlots(), getMenuName());
                inventorys.add(inventory);

                Map<String, MenuAddon> addonList = plugin.getMenuAddons(this.getConfigMenuAddonFeatureName());

                //grab all the items specified to be used for this menu and add to inventory
                this.setMenuItems();
                for(String menuAddonName : addonList.keySet()) {
                    MenuAddon addon = addonList.get(menuAddonName);
                    addon.setMenuAddonItems();
                }

                if (Listener.class.isAssignableFrom(this.getClass())) {
                    Bukkit.getPluginManager().registerEvents((Listener) this, AdminPanelMain.getPlugin());
                }

                //open the inventory for the player
                playerMenuUtility.getOwner().openInventory(inventory);

                // Try executing Menu Addons onOpenEvent
                for(String menuAddonName : addonList.keySet()) {
                    MenuAddon addon = addonList.get(menuAddonName);
                    addon.onOpenEvent();
                }
                if (this instanceof AdminPanelStartMenu) {
                    playerMenuUtility.getOwner().sendMessage(
                            AdminPanelMain.getPlugin().getLanguageManager().getMessage("Player.General.OpeningMessageSelf", playerMenuUtility.getOwner(), true));
                }
            }
        } catch (NotAPanelEventException e) {
            e.printStackTrace();
        }
        plugin.getFileLogger().writeToLog(Level.INFO, playerMenuUtility.getOwner().getName() + "(UUID: " + playerMenuUtility.getOwner().getUniqueId() + ") opened the Panel: " + this.getClass().getName(), "ActionsLogger - Panel");
    }

    //Overridden method from the InventoryHolder interface
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    //Helpful utility method to fill all remaining slots with "filler glass"
    public void setFillerGlass() {
        for (int i = 0; i < getSlots(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, FILLER);
            }
        }
    }

    public ItemStack makeItem(Material material, String displayName, String... lore) {

        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(displayName);

        itemMeta.setLore(Arrays.asList(lore));
        item.setItemMeta(itemMeta);

        return item;
    }


    public int getSlot(String path, int defaultInt) {
        return lgm.getCustomObject("Items." + path + ".slot", null, defaultInt, false);
    }
}

