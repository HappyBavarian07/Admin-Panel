# Language Configuration Tutorial

## Introduction

The AdminPanel plugin provides a powerful language configuration system through the `LanguageManager` class. This allows
you to create multi-language support for your plugin with dynamic content using the Expression Parser system.

## Language File Structure

Language files are YAML files stored in the `languages` folder of your plugin. Each language has its own file (e.g.,
`en.yml`, `de.yml`).

## Language Manager API

### Initializing the Language Manager

```java
// Create a new language manager
File langFolder = new File(plugin.getDataFolder(), "languages");
LanguageManager languageManager = new LanguageManager(plugin, langFolder, "[AdminPanel] ");

// Load available languages
languageManager.addLanguagesToList(true);

// Set the current language based on config
String configLanguage = plugin.getConfig().getString("Plugin.language");
languageManager.setCurrentLang(languageManager.getLang(configLanguage, true), true);
```

### Accessing Messages

```java
// Get a simple message
String message = languageManager.getMessage("Messages.Welcome", player, true);

// Get a message with placeholders
languageManager.addPlaceholder(PlaceholderType.MESSAGE, "%player%", player.getName(), true);
String personalized = languageManager.getMessage("Messages.PlayerJoin", player, true);
```

### Working with Materials and Items

```java
// Get a material from config
Material material = languageManager.getMaterial("DIAMOND", null);

// Get an item from config
ItemStack item = languageManager.getItem("Items.BackButton", player, true);
```

## Working with Custom Variables Section

The language configuration system supports a special section called `CustomVariables` that allows you to define
variables, expressions, and functions that can be used throughout your language files.

### Structure of CustomVariables Section

In your language file (e.g., `en.yml`):

```yaml
CustomVariables:
  # Simple variable
  serverName: "Awesome Server"
  
  # Variable with VARIABLE prefix
  maxPlayers: "VARIABLE 100"
  
  # Expression with EXPR prefix (or EXPRESSION prefix) (TODO IMPLEMENT)
  playerCount: "EXPR getOnlinePlayers().size()"
  
  # Function definition with FUNCTION prefix
  colorFunction: "FUNCTION getColor(player) = if player.hasPermission('admin'): '&c' elif player.hasPermission('mod'): '&6' else: '&7'"
```

### How CustomVariables Are Processed

The `LanguageManager` automatically processes the `CustomVariables` section when loading a language file:

1. Variables are registered with the ExpressionParser
2. Expressions are evaluated and their results are stored as variables
3. Functions are registered with the ExpressionParser's function manager

This allows you to reference these variables and functions in other parts of your language files.

### Types of Custom Variable Entries

#### Simple Variables

Simple variables are stored as-is and can be referenced in expressions:

```yaml
CustomVariables:
  serverName: "My Server"
  maxPlayers: 100
  welcomePrefix: "&6&l"
```

#### VARIABLE Prefix

Entries with the `VARIABLE` prefix are explicitly marked as variables:

```yaml
CustomVariables:
  adminColor: "VARIABLE &c"
  userColor: "VARIABLE &7"
```

#### EXPR/EXPRESSION Prefix

Entries with the `EXPR` or `EXPRESSION` prefix are evaluated as expressions, and the result is stored as a variable:

```yaml
CustomVariables:
  # Mathematical expression
  multiplier: "EXPR 2 * 3"
  
  # Conditional expression
  defaultMaterial: "EXPR if serverVersion > 1.16: NETHERITE_BLOCK else: DIAMOND_BLOCK"
```

#### FUNCTION Prefix

Entries with the `FUNCTION` prefix define functions that can be called from expressions:

```yaml
CustomVariables:
  # Simple function
  doubleFunction: "FUNCTION double(x) = x * 2"
  
  # Conditional function
  getRankColor: "FUNCTION getRankColor(rank) = if rank == 'admin': '&c' elif rank == 'mod': '&6' else: '&7'"
```

### Using Variables in Messages

You can use variables in your messages using the expression syntax:

```yaml
Messages:
  Welcome: "${welcomePrefix}Welcome to ${serverName}!"
  ServerInfo: "There are currently ${playerCount} out of ${maxPlayers} players online."
  PlayerRank: "Your rank color is ${getRankColor(playerRank)}"
```

## Using Expressions in Language Files

In addition to the `CustomVariables` section, you can use expressions directly in your language files:

### Conditional Item Materials

```yaml
Items:
  RankItem:
    Material: "if player.hasPermission('admin'): DIAMOND_BLOCK elif player.hasPermission('mod'): GOLD_BLOCK else: STONE"
```

### Dynamic Messages

```yaml
Messages:
  Welcome: "${if time > 18: 'Good evening' elif time > 12: 'Good afternoon' else: 'Good morning'}, ${player.getName()}!"
```

## Advanced Usage

### Per-Player Language Support

The LanguageManager includes a `PerPlayerLanguageHandler` that allows players to choose their preferred language:

```java
// Set up per-player language handler
PerPlayerLanguageHandler handler = new PerPlayerLanguageHandler(plugin, languageManager);
languageManager.setPlhandler(handler);

// Get message in player's preferred language
String message = languageManager.getMessage("Messages.Welcome", player, true);
```

### Updating Language Files

The LanguageManager can automatically update language files when new keys are added in the default language file:

```java
// Update all language files with any new keys
languageManager.updateLangFiles();
```

### Handling Placeholders

Placeholders allow you to insert dynamic content into messages:

```java
// Add a placeholder
languageManager.addPlaceholder(PlaceholderType.MESSAGE, "%player%", player.getName(), true);

// Add multiple placeholders
Map<String, Placeholder> placeholders = languageManager.getNewPlaceholderMap();
placeholders.put("%player%", new Placeholder("%player%", player.getName(), PlaceholderType.MESSAGE));
placeholders.put("%world%", new Placeholder("%world%", player.getWorld().getName(), PlaceholderType.MESSAGE));
languageManager.addPlaceholders(placeholders, true);

// Get message with placeholders
String message = languageManager.getMessage("Messages.PlayerLocation", player, true);
// Example message: "Player %player% is in world %world%"
```

## Best Practices

1. **Organize language files** into logical sections (Messages, Items, Menus, etc.)
2. **Use CustomVariables** for values used in multiple places
3. **Keep expressions simple** when possible for easier maintenance
4. **Document your language file structure** for other developers
5. **Include complete examples** in your default language file
6. **Reset placeholders** after use to prevent unexpected replacements

## Integration with ExpressionParser

The LanguageManager integrates with the ExpressionParser to provide dynamic content evaluation. See the ExpressionParser
tutorial for more details on writing expressions.
