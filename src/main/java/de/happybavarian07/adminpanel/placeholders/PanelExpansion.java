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
        return switch (params) {
            case "openingsound" -> AdminPanelMain.getPlugin().getConfig().getString("Panel.SoundWhenOpened");
            case "effectwhileopen" -> AdminPanelMain.getPlugin().getConfig().getString("Panel.EffectWhenOpened");
            case "currentlang_shortname" -> plugin.getLanguageManager().getCurrentLang().getLangName();
            case "currentlang_fullname" -> plugin.getLanguageManager().getCurrentLang().getFullName();
            case "currentlang_version" -> plugin.getLanguageManager().getCurrentLang().getFileVersion();
            case "currentlang_path" -> plugin.getLanguageManager().getCurrentLang().getLangFile().getPath();
            default -> null;
        };
    }
}
