package de.happybavarian07.adminpanel.menusystem.menu.misc;/*
 * @Author HappyBavarian07
 * @Date 04.11.2023 | 13:27
 */

import de.happybavarian07.adminpanel.language.PlaceholderType;
import de.happybavarian07.adminpanel.menusystem.Menu;
import de.happybavarian07.adminpanel.menusystem.PlayerMenuUtility;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ConfirmationMenu extends Menu {
    public ConfirmationMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        return lgm.getMenuTitle("Misc.ConfirmationMenu", playerMenuUtility.getOwner());
    }

    @Override
    public String getConfigMenuAddonFeatureName() {
        return "ConfirmationMenu";
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = playerMenuUtility.getOwner();
        ItemStack item = e.getCurrentItem();

        ItemStack confirmItem = lgm.getItem("General.ConfirmationMenu.Confirm", player, false);
        ItemStack cancelItem = lgm.getItem("General.ConfirmationMenu.Cancel", player, false);

        // Handle Confirm button click
        assert item != null;
        if (item.isSimilar(confirmItem)) {
            // Implement the confirmation action here
            // Create a list to collect method arguments
            List<Object> methodArgs = new ArrayList<>();

            // Initialize the index to 0
            int i = 0;

            // Loop until data with the key "ConfirmationMenu_MethodArgs_<i>" is found
            while (playerMenuUtility.hasData("ConfirmationMenu_MethodArgs_" + i)) {
                Object dataValue = playerMenuUtility.getData("ConfirmationMenu_MethodArgs_" + i);
                // Add the data to the list of method arguments
                methodArgs.add(dataValue);
                i++; // Increment the index
            }

            // Execute the method given in the data with methodArgs as arguments
            Method methodToExecute = (Method) playerMenuUtility.getData("ConfirmationMenu_MethodToExecuteAfter");
            Object objectToInvokeOn = playerMenuUtility.getData("ConfirmationMenu_ObjectToInvokeMethodOn");
            List<Class<? extends Exception>> exceptionsToCatch = (List<Class<? extends Exception>>) playerMenuUtility.getData("ConfirmationMenu_ExceptionsToCatch");
            if (methodToExecute != null) {
                try {
                    methodToExecute.invoke(objectToInvokeOn, methodArgs.toArray());
                } catch (Exception ex) {
                    if(exceptionsToCatch.contains(ex.getClass())) {
                        lgm.addPlaceholder(PlaceholderType.MESSAGE, "%error%", ex.getMessage(), true);
                        player.sendMessage(lgm.getMessage("Player.General.Error", player, true));
                    }
                    ex.printStackTrace();
                }
            }


            // Open the old menu
            String menuToOpenAfter = (String) playerMenuUtility.getData("ConfirmationMenu_MenuToOpenAfter");
            if (menuToOpenAfter != null) {
                Menu oldMenu = Utils.getMenuByClassName(menuToOpenAfter, player);
                if (oldMenu != null) {
                    oldMenu.open();
                }
            }
        } else if (item.isSimilar(cancelItem)) {
            // Open the old menu
            String menuToOpenAfter = (String) playerMenuUtility.getData("ConfirmationMenu_MenuToOpenAfter");
            if (menuToOpenAfter != null) {
                Menu oldMenu = Utils.getMenuByClassName(menuToOpenAfter, player);
                if (oldMenu != null) {
                    oldMenu.open();
                }
            }
        }
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
        // Retrieve items using the getItem method with appropriate paths.
        ItemStack confirmItem = lgm.getItem("General.ConfirmationMenu.Confirm", player, false);
        ItemStack cancelItem = lgm.getItem("General.ConfirmationMenu.Cancel", player, false);

        // Calculate the middle row based on the number of slots in the menu
        int middleRow = getSlots() / 9 / 2;

        // Calculate the number of slots for the left and right sides
        int leftSideSlots = middleRow * 9;
        int rightSideSlots = middleRow * 9;

        // Fill the left side with Confirm buttons
        for (int i = 0; i < leftSideSlots; i++) {
            if (i % 9 != 4) { // Skip the middle column
                inventory.setItem(i, confirmItem); // Place Confirm button in the left side
            }
        }

        // Fill the right side with Cancel buttons
        for (int i = getSlots() - 1; i >= getSlots() - rightSideSlots; i--) {
            if (i % 9 != 4) { // Skip the middle column
                inventory.setItem(i, cancelItem); // Place Cancel button in the right side
            }
        }

        // Place the reason item in the middle row
        lgm.addPlaceholder(PlaceholderType.ITEM, "%reason%", playerMenuUtility.getData("ConfirmationMenu_Reason"), true);
        inventory.setItem(4 + middleRow * 9, lgm.getItem("General.ConfirmationMenu.ReasonItem", player, true));
    }
}
