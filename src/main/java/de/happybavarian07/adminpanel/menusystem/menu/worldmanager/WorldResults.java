package de.happybavarian07.adminpanel.menusystem.menu.worldmanager;

import de.happybavarian07.adminpanel.commands.CommandText;
import de.happybavarian07.adminpanel.worlds.WorldOperations;
import de.happybavarian07.coolstufflib.languagemanager.LanguageManager;
import org.bukkit.entity.Player;

/**
 * Turns the result of a world action into the chat message of the language file. The action name comes from
 * {@code WorldManager.Action.<action>}, the text from {@code WorldManager.Result.<result>}.
 */
final class WorldResults {
    private WorldResults() {
    }

    static void send(LanguageManager lgm, Player staff, String action, WorldOperations.Result result, String world) {
        CommandText.send(lgm, staff, "WorldManager.Result." + result.name(),
                "%action%", CommandText.get(lgm, staff, "WorldManager.Action." + action), "%world%", world);
    }
}
