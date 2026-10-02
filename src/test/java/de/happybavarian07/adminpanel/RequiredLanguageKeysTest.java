package de.happybavarian07.adminpanel;

import de.happybavarian07.adminpanel.menusystem.menu.worldmanager.time.Time;
import de.happybavarian07.adminpanel.startup.Startup;
import de.happybavarian07.adminpanel.worlds.GameRuleCategory;
import de.happybavarian07.adminpanel.worlds.WorldOperations;
import de.happybavarian07.adminpanel.worlds.WorldPreset;
import de.happybavarian07.adminpanel.menusystem.menu.worldmanager.weather.Weather;
import org.bukkit.Difficulty;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The menus cannot be opened without a server, so a wrong language key would only show up in game. This lists
 * the keys the world menus and the startup status read and checks both language files have them.
 */
class RequiredLanguageKeysTest {
    private static final List<String> BOOLEAN_ITEMS = List.of("PVP", "AutoSave", "Monsters", "Animals", "KeepInventory");

    @Test
    void englishHasEveryKeyTheWorldMenusRead() throws IOException {
        checkLanguage("en");
    }

    @Test
    void germanHasEveryKeyTheWorldMenusRead() throws IOException {
        checkLanguage("de");
    }

    @Test
    void englishHasEveryKeyTheStartupStatusReads() throws IOException {
        checkStartup("en");
    }

    @Test
    void germanHasEveryKeyTheStartupStatusReads() throws IOException {
        checkStartup("de");
    }

    private static void checkStartup(String language) throws IOException {
        Map<String, Object> root = load(language);
        List<String> missing = new ArrayList<>();
        for (Startup.Status status : Startup.Status.values()) {
            item(root, missing, "Items.StartupStatus.Entry." + status);
            text(root, missing, "Messages.StartupStatus.Status." + status);
        }
        for (String stage : List.of("libraries", "data", "api", "bungee", "placeholderapi", "supervanish", "vault", "config",
                "warnings", "trolls", "permissions", "prefix", "events", "language", "staffchat", "configupdate", "bstats", "updater",
                "autoupdater", "permissionsetup", "commands", "addons", "backup")) {
            text(root, missing, "Messages.StartupStatus.Stage." + stage);
        }
        item(root, missing, "Items.StartupStatus.Unavailable");
        item(root, missing, "Items.StartMenu.StartupProblems");
        for (String key : List.of("Unavailable", "Header", "Line", "Kind.Required", "Kind.Optional")) {
            text(root, missing, "Messages.StartupStatus." + key);
        }
        text(root, missing, "MenuTitles.StartupStatus");
        assertTrue(missing.isEmpty(), language + " is missing: " + missing);
    }

    private static void checkLanguage(String language) throws IOException {
        Map<String, Object> root = load(language);
        List<String> missing = new ArrayList<>();

        for (String item : List.of("Info", "TPToSpawn", "SetSpawn", "Players", "Seed", "GameRule", "TimeWeather", "Border",
                "Save", "Unload", "Duplicate", "Rename", "Delete")) {
            item(root, missing, "Items.WorldManager.Settings." + item);
        }
        for (String flag : BOOLEAN_ITEMS) {
            item(root, missing, "Items.WorldManager.Settings." + flag + ".true");
            item(root, missing, "Items.WorldManager.Settings." + flag + ".false");
        }
        for (Difficulty difficulty : Difficulty.values()) {
            item(root, missing, "Items.WorldManager.Settings.Difficulty." + difficulty);
            item(root, missing, "Items.WorldManager.CreateMenu.Difficulty." + difficulty);
        }
        for (String key : List.of("Create", "Import", "CreateMenu.Name", "CreateMenu.Seed", "CreateMenu.Preset",
                "CreateMenu.CreateWorld", "Select.Search", "Select.Filter.ALL", "Select.Filter.LOADED",
                "Select.Filter.UNLOADED", "Select.Loaded", "Select.Unloaded", "GameRuleMenu.Search", "GameRuleMenu.Filter.All",
                "GameRuleMenu.Rule.Boolean.Enabled", "GameRuleMenu.Rule.Boolean.Disabled", "GameRuleMenu.Rule.Number",
                "GameRuleMenu.Rule.Unknown", "TimeWeather.Info", "TimeWeather.HourBack", "TimeWeather.HourForward",
                "TimeWeather.SetExact", "TimeWeather.Freeze.true", "TimeWeather.Freeze.false", "TimeWeather.WeatherDuration",
                "Players.Entry", "CreateMenu.Environment.NORMAL", "CreateMenu.Environment.NETHER",
                "CreateMenu.Environment.THE_END", "CreateMenu.GenerateStructures.true", "CreateMenu.GenerateStructures.false",
                "CreateMenu.Hardcore.true", "CreateMenu.Hardcore.false", "CreateMenu.TeleportAfter.true",
                "CreateMenu.TeleportAfter.false")) {
            item(root, missing, "Items.WorldManager." + key);
        }
        for (GameRuleCategory category : GameRuleCategory.values()) {
            item(root, missing, "Items.WorldManager.GameRuleMenu.Filter." + capitalized(category.name()));
            text(root, missing, "Messages.GameRuleMenu.Category." + capitalized(category.name()));
        }
        for (Time time : Time.values()) item(root, missing, "Items.WorldManager.Time." + time);
        for (Weather weather : Weather.values()) item(root, missing, "Items.WorldManager.Weather." + weather);

        for (WorldOperations.Result result : WorldOperations.Result.values()) {
            text(root, missing, "Messages.WorldManager.Result." + result);
        }
        for (String action : List.of("Load", "Import", "Create", "Save", "Unload", "Duplicate", "Rename", "Delete")) {
            text(root, missing, "Messages.WorldManager.Action." + action);
        }
        for (WorldPreset preset : WorldPreset.values()) text(root, missing, "Messages.WorldCreate.Preset." + preset.languageKey());
        for (String key : List.of("WorldSelect.EnterImport", "WorldSelect.EnterSearch", "WorldSelect.SearchSet",
                "WorldSelect.SearchCleared", "WorldSelect.Teleported", "WorldSelect.MainWorld", "WorldSettings.Seed",
                "WorldSettings.SpawnSet", "WorldSettings.SpawnWrongWorld", "WorldSettings.EnterBorder",
                "WorldSettings.BorderSet", "WorldSettings.NotASize", "WorldSettings.EnterDuplicate",
                "WorldSettings.EnterRename", "WorldCreate.EnterName", "WorldCreate.EnterSeed", "WorldPlayers.Sent",
                "WorldPlayers.Teleported", "GameRuleMenu.EnterSearch", "GameRuleMenu.SearchSet", "GameRuleMenu.SearchCleared",
                "GameRuleMenu.EnterValue", "GameRuleMenu.NotAValue", "GameRuleMenu.ValueSet", "TimeWeatherMenu.EnterTime",
                "TimeWeatherMenu.NotATime", "TimeWeatherMenu.TimeSet", "TimeWeatherMenu.EnterDuration",
                "TimeWeatherMenu.DurationSet", "TimeWeatherMenu.NotADuration", "TimeWeatherMenu.WeatherSet")) {
            text(root, missing, "Messages." + key);
        }
        for (String title : List.of("WorldSelector", "WorldSettings", "WorldCreateMenu", "GameRuleMenu", "TimeWeatherMenu",
                "WorldPlayers")) {
            text(root, missing, "MenuTitles.WorldManager." + title);
        }

        assertTrue(missing.isEmpty(), language + " is missing: " + missing);
    }

    private static void item(Map<String, Object> root, List<String> missing, String path) {
        Object node = node(root, path);
        if (!(node instanceof Map<?, ?> map) || !map.containsKey("material") || !map.containsKey("displayName")) missing.add(path);
    }

    private static void text(Map<String, Object> root, List<String> missing, String path) {
        if (!(node(root, path) instanceof String)) missing.add(path);
    }

    private static Object node(Map<String, Object> root, String path) {
        Object current = root;
        for (String part : path.split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) return null;
            Object next = null;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (String.valueOf(entry.getKey()).equals(part)) {
                    next = entry.getValue();
                    break;
                }
            }
            current = next;
        }
        return current;
    }

    private static String capitalized(String name) {
        return name.charAt(0) + name.substring(1).toLowerCase(Locale.ROOT);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> load(String language) throws IOException {
        try (InputStream in = RequiredLanguageKeysTest.class.getResourceAsStream("/languages/" + language + ".yml")) {
            return new Yaml().load(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }
}
