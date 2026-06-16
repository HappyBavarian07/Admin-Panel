# Language Manager System Tutorial

## Overview
The Language Manager provides multi-language support, dynamic placeholders, per-player language handling, language file caching, and integration with an expression engine. It is designed for Bukkit/Spigot plugins.

---

## Features
- **Language Registration & Switching:** Register multiple languages from a folder; switch language at runtime.
- **Per-Player Language:** Assign and retrieve language preferences per player.
- **Placeholder System:** Define and use dynamic placeholders in language files and code.
- **Caching:** Fast access to language values via LanguageCache.
- **Expression Engine Integration:** Embed logic in language strings using expressions.

---

## Initialization (Preferred Method)

The recommended way to initialize the Language Manager is via the builder pattern:

```java
CoolStuffLib lib = new CoolStuffLibBuilder(plugin)
    .withLanguageManager()
        .setLanguageFolder(langFolder)
        .setResourceDirectory("languages")
        .setPrefix("&7[Prefix] ")
        .enablePlayerLanguageHandler()
    .build()
    .setDataFile(dataFile)
    .createCoolStuffLib();
```

This ensures all language features and per-player handling are enabled and integrated with other systems.

---

## Per-Player Language
Set and get a player's language:
```java
PerPlayerLanguageHandler handler = langManager.getPLHandler();
handler.setPlayerLanguage(player.getUniqueId(), "de_DE");
String lang = handler.getPlayerLanguageName(player.getUniqueId());
```

---

## Switching Language
Set the current language globally:
```java
langManager.setCurrentLang(langManager.getLang("en_US", true), true);
```

---

## Getting Messages and Items
Get a message with placeholders and expressions:
```java
String msg = langManager.getMessage("Welcome", player, true);
```
Get an item from the language file:
```java
ItemStack item = langManager.getItem("General.FillerItem", player, true);
```

---

## Placeholders
Add or reset placeholders for dynamic replacement:
```java
langManager.addPlaceholder(PlaceholderType.MESSAGE, "%player%", player.getName(), false);
langManager.resetPlaceholders(PlaceholderType.MESSAGE, null); // Reset all message placeholders
```

---

## Caching
Access the cache for a language:
```java
LanguageCache cache = langManager.getLanguageCache("en_US");
cache.clearCache();
```

---

## Updating and Reloading Languages
Update all language files:
```java
langManager.updateLangFiles();
```
Reload and re-register languages:
```java
langManager.reloadLanguages(commandSender, true);
```

---

## Expression Engine Integration
Use expressions in language files, e.g.:
```yml
player_status: "Health: ${player.health} | Level: ${player.level}"
```
See `EXPRESSION_ENGINE_TUTORIAL.md` for advanced usage.

---

## Advanced
- **Custom Placeholders:** Implement the `Placeholder` interface for custom logic.
- **Per-Player Language:** Use `PerPlayerLanguageHandler` for player-specific language.
- **Item/Message Formatting:** Use `replacePlaceholders` for advanced formatting.
- **Error Handling:** If a language or path is missing, fallback and error messages are returned.

---

## References
- `CoolStuffLibBuilder.java`
- `LanguageManager.java`
- `PerPlayerLanguageHandler.java`
- `CoolStuffLib.java`
- `LanguageFile.java`
- `LanguageCache.java`

For expression engine details, see `EXPRESSION_ENGINE_TUTORIAL.md`.
