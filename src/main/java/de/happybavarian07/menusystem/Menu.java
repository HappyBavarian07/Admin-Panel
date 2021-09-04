package de.happybavarian07.menusystem;

import de.happybavarian07.events.NotAPanelEventException;
import de.happybavarian07.events.general.PanelOpenEvent;
import de.happybavarian07.main.Main;
import de.happybavarian07.menusystem.menu.AdminPanelStartMenu;
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

/*
    Defines the behavior and attributes of all menus in our plugin
 */
public abstract class Menu implements InventoryHolder {

    //Protected values that can be accessed in the menus
    protected String openingPermission = "";
    protected PlayerMenuUtility playerMenuUtility;
    protected Inventory inventory;
    protected List<Inventory> inventorys = new ArrayList<>();
    protected ItemStack FILLER = Main.getPlugin().getLanguageManager().getItem("General.FillerItem", null);

    //Constructor for Menu. Pass in a PlayerMenuUtility so that
    // we have information on who's menu this is and
    // what info is to be transfered
    public Menu(PlayerMenuUtility playerMenuUtility) {
        this.playerMenuUtility = playerMenuUtility;
    }

    //let each menu decide their name
    public abstract String getMenuName();

    //let each menu decide their slot amount
    public abstract int getSlots();

    //let each menu decide how the items in the menu will be handled when clicked
    public abstract void handleMenu(InventoryClickEvent e);

    //let each menu decide what items are to be placed in the inventory menu
    public abstract void setMenuItems();

    public void setOpeningPermission(String permission) {
        this.openingPermission = permission;
    }

    public String getOpeningPermission() {
        return openingPermission;
    }

    //When called, an inventory is created and opened for the player
    public void open() {
        //The owner of the inventory created is the Menu itself,
        // so we are able to reverse engineer the Menu object from the
        // inventoryHolder in the MenuListener class when handling clicks
        if(!playerMenuUtility.getOwner().hasPermission(this.openingPermission)) {
            playerMenuUtility.getOwner().sendMessage(
                    Main.getPlugin().getLanguageManager().getMessage("Player.General.NoPermissions", playerMenuUtility.getOwner()));
            playerMenuUtility.getOwner().closeInventory();
            return;
        }

        PanelOpenEvent panelOpenEvent = new PanelOpenEvent(playerMenuUtility.getOwner(), this, playerMenuUtility);
        try {
            Main.getAPI().callAdminPanelEvent(panelOpenEvent);
            if(!panelOpenEvent.isCancelled()) {
                inventory = Bukkit.createInventory(this, getSlots(), getMenuName());
                inventorys.add(inventory);

                //grab all the items specified to be used for this menu and add to inventory
                this.setMenuItems();

                if(Listener.class.isAssignableFrom(this.getClass())) {
                    Bukkit.getPluginManager().registerEvents((Listener) this, Main.getPlugin());
                }

                //open the inventory for the player
                playerMenuUtility.getOwner().openInventory(inventory);
                if(this instanceof AdminPanelStartMenu) {
                    playerMenuUtility.getOwner().sendMessage(
                            Main.getPlugin().getLanguageManager().getMessage("Player.General.OpeningMessageSelf", playerMenuUtility.getOwner()));
                }
            }
        } catch (NotAPanelEventException e) {
            e.printStackTrace();
        }
    }

    //Overridden method from the InventoryHolder interface
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    //Helpful utility method to fill all remaining slots with "filler glass"
    public void setFillerGlass(){
        for (int i = 0; i < getSlots(); i++) {
            if (inventory.getItem(i) == null){
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

}

