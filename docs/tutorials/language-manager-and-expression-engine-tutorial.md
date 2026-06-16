# Language Manager & Expression Engine Tutorial

## Table of Contents

1. Overview
2. Features
3. Initialization & Setup
4. Language Registration & Switching
5. Per-Player Language Handling
6. Placeholders & Dynamic Replacement
7. Caching & Performance
8. Updating & Reloading Languages
9. Expression Engine Integration
10. Advanced Usage
11. Troubleshooting & Tips
12. References

---

## 1. Overview

The Language Manager in CoolStuffLib provides multi-language support, dynamic placeholders, per-player language
handling, language file caching, and seamless integration with the Expression Engine for advanced logic in language
files.

---

## 2. Features

- Register and switch languages at runtime
- Per-player language preferences
- Dynamic placeholders in messages and items
- Fast access via LanguageCache
- Expression Engine for logic in language strings

---

## 3. Initialization & Setup

Use CoolStuffLibBuilder to set up the Language Manager:

```java
CoolStuffLib lib = new CoolStuffLibBuilder(pluginInstance)
        .setLanguageManager(new LanguageManager(pluginInstance, langFolder, "languages", "&7[Prefix] "))
        .setUsePlayerLangHandler(true)
        .setDataFile(dataFile)
        .build();
lib.

setup();
```

---

## 4. Language Registration & Switching

Languages are registered automatically from the language folder:

```java
LanguageManager langManager = lib.getLanguageManager();
langManager.

addLanguagesToList(true);
langManager.

setCurrentLang(langManager.getLang("en_US", true), true);
```

---

## 5. Per-Player Language Handling

Set and get a player's language:

```java
PerPlayerLanguageHandler handler = langManager.getPLHandler();
handler.

setPlayerLanguage(player.getUniqueId(), "de_DE");
String lang = handler.getPlayerLanguageName(player.getUniqueId());
```

---

## 6. Placeholders & Dynamic Replacement

Add or reset placeholders for dynamic replacement:

```java
langManager.addPlaceholder(PlaceholderType.MESSAGE, "%player%",player.getName(), false);
        langManager.

resetPlaceholders(PlaceholderType.MESSAGE, null);
```

Get a message or item with placeholders and expressions:

```java
String msg = langManager.getMessage("Welcome", player, true);
ItemStack item = langManager.getItem("General.FillerItem", player, true);
```

---

## 7. Caching & Performance

Access and clear the cache for a language:

```java
LanguageCache cache = langManager.getLanguageCache("en_US");
cache.

clearCache();
```

---

## 8. Updating & Reloading Languages

Update all language files and reload:

```java
langManager.updateLangFiles();
langManager.

reloadLanguages(commandSender, true);
```

---

## 9. Expression Engine Integration

Embed logic in language files using expressions:

```yml
player_status: "Health: ${player.health} | Level: ${player.level}"
```

Use the Expression Engine directly:

```java
ExpressionEngine engine = new ExpressionEngine();
Object result = engine.parsePrimitive("Out<double>(2 + 3 * 4)");
engine.

setVariable("x",10);

Object result2 = engine.parsePrimitive("Out<double>(x * 2)");
```

Register custom functions:

```java
engine.registerFunction("square",(i, args, t) ->{
double v = ((Number) args.get(0)).doubleValue();
    return Out<double>(v *v);
        },"double",new Class<?>[]{Double .class},Double .class);
Object result = engine.parsePrimitive("Out<double>(square(5))");
engine.

unregisterFunction("square");
```

---

## 10. Advanced Usage

- Implement the `Placeholder` interface for custom logic
- Use `replacePlaceholders` for advanced formatting
- Use per-player language for personalized messages
- Handle missing languages/paths with fallback logic

---

## 11. Troubleshooting & Tips

- Ensure language files are present and correctly formatted
- Use `addLanguagesToList(true)` to log registration
- Use `updateLangFiles()` and `reloadLanguages()` after changes
- Use the Expression Engine for dynamic logic in messages

---

## 12. References

- CoolStuffLibBuilder.java
- LanguageManager.java
- PerPlayerLanguageHandler.java
- LanguageFile.java
- LanguageCache.java
- ExpressionEngine.java
- See `EXPRESSION_ENGINE_TUTORIAL.md` for advanced expression usage

---

This tutorial covers all major aspects of the CoolStuffLib Language Manager and Expression Engine, including setup,
per-player language, placeholders, caching, updating, and advanced logic integration. For further details, consult the
API documentation or source code.

