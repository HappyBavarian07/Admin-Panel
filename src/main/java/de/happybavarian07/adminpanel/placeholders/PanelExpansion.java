package de.happybavarian07.adminpanel.placeholders;

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PanelExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "appanel";
    }

    @Override
    public @NotNull String getAuthor() {
        return "HappyBavarian07";
    }

    @Override
    public @NotNull String getVersion() {
        return AdminPanelMain.getPlugin().getDescription().getVersion();
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return null;
        }
        AdminPanelMain plugin = AdminPanelMain.getPlugin();
        if (params.equals("openingsound")) {
            return AdminPanelMain.getPlugin().getConfig().getString("Panel.SoundWhenOpened");
        }
        if (params.equals("effectwhileopen")) {
            return AdminPanelMain.getPlugin().getConfig().getString("Panel.EffectWhenOpened");
        }
        if (params.equals("currentlang_shortname")) {
            return plugin.getLanguageManager().getCurrentLang().getLangName();
        }
        if (params.equals("currentlang_fullname")) {
            return plugin.getLanguageManager().getCurrentLang().getFullName();
        }
        if (params.equals("currentlang_version")) {
            return plugin.getLanguageManager().getCurrentLang().getFileVersion();
        }
        if (params.equals("currentlang_path")) {
            return plugin.getLanguageManager().getCurrentLang().getLangFile().getPath();
        }
        return null;
    }
}
