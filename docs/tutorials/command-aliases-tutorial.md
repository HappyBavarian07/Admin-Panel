# Command Aliases Tutorial

Command aliases are shortcuts: a new command that runs another command line, for example `/gmc` running
`/gamemode creative`. This tutorial goes from a fixed alias in code to aliases that admins create at runtime
and that are saved in your plugin's data file or database.

## Table of Contents

1. [A First Alias in Code](#1-a-first-alias-in-code)
2. [Placeholders](#2-placeholders)
3. [Console Aliases](#3-console-aliases)
4. [Aliases Created at Runtime](#4-aliases-created-at-runtime)
5. [Storing Aliases in data.yml](#5-storing-aliases-in-datayml)
6. [Storing Aliases in a Database Table](#6-storing-aliases-in-a-database-table)
7. [An Admin Command to Manage Aliases](#7-an-admin-command-to-manage-aliases)
8. [Read-Only Aliases from a Section](#8-read-only-aliases-from-a-section)
9. [Safety Checks](#9-safety-checks)
10. [Commands in Menus](#10-commands-in-menus)

---

## 1. A First Alias in Code

```java
CommandManagerRegistry registry = coolStuffLib.getCommandManagerRegistry();

registry.command("gmc")
        .runs("gamemode creative {args}")
        .register();
```

`/gmc` now runs `/gamemode creative` as the player who typed it, so their own permissions for `/gamemode`
still apply. `/gmc Steve` runs `/gamemode creative Steve`.

Everything else of a builder command is available: `.permission(...)`, `.aliases(...)`, `.info(...)`,
`.playerOnly()`, `.cooldown(Duration)`, `.confirm()`.

---

## 2. Placeholders

| Placeholder | Replaced with | Required? |
|---|---|---|
| `{0}`, `{1}`, ... | the first, second, ... argument of the alias | yes |
| `{args}` | all arguments | no |
| `{args:1}` | all arguments from the second on | no |
| `{player}` | the sender's name | — |
| `{uuid}` | the sender's UUID (empty for the console) | — |

The argument limits and the usage line follow from the template:

```java
registry.command("tpto").runs("tp {player} {0}").register();
// usage: /tpto <arg1>; exactly one argument; "/tpto" alone answers "Too few Arguments!" and the usage

registry.command("broadcastto").runs("msg {0} {args:1}").register();
// usage: /broadcastto <arg1> [args...]
```

A leading `/` in the template is optional.

---

## 3. Console Aliases

Some shortcuts need rights the player does not have, for example giving an item:

```java
registry.command("starterkit")
        .permission("myplugin.starterkit")
        .runsAsConsole("give {player} stone_sword 1")
        .cooldown(Duration.ofHours(24))
        .register();
```

The console can run anything, so the alias itself is the only protection. Therefore `runsAsConsole` requires
an explicit `.permission(...)`; registering without one throws.

---

## 4. Aliases Created at Runtime

To let admins add and remove aliases while the server runs, use a `CommandAliasManager` with an `AliasStore`
that remembers them across restarts:

```java
AliasStore store = new YamlAliasStore(new File(getDataFolder(), "data.yml"));
CommandAliasManager aliases = new CommandAliasManager(registry, store);

aliases.loadAll();                                                    // on enable: restore saved aliases
aliases.create(CommandAlias.of("gmc", "gamemode creative {args}"));   // registers and saves
aliases.remove("gmc");                                                // unregisters and deletes
aliases.list();                                                       // all active aliases
```

A `CommandAlias` has the same options as the builder:

```java
new CommandAlias(
        "starterkit",                       // name
        "give {player} stone_sword 1",      // command line
        "myplugin.starterkit",              // permission, null for "<name>"
        true,                               // run as console
        "Gives the starter kit",            // description, may be null
        List.of("kit"),                     // extra names
        true,                               // players only
        86400,                              // cooldown in seconds
        false);                             // needs confirmation
```

`create` validates first (name taken, alias runs itself, console alias without permission) and only saves an
alias that could be registered. `remove` really removes the command from the server, so tab completion
forgets it immediately.

---

## 5. Storing Aliases in data.yml

`YamlAliasStore` keeps aliases in their own section of your data file:

```yaml
CommandAliases:
  gmc:
    command: gamemode creative {args}
  starterkit:
    command: give {player} stone_sword 1
    permission: myplugin.starterkit
    console: true
    aliases:
    - kit
    player-only: true
    cooldown-seconds: 86400
```

If your plugin already keeps `data.yml` loaded in memory, pass that object so both write the same
configuration and do not overwrite each other:

```java
new YamlAliasStore(dataYml, dataFile, YamlAliasStore.DEFAULT_SECTION);
```

Aliases are runtime data, not configuration: the store refuses `config.yml`, because saving a config through
Bukkit would remove all its comments.

---

## 6. Storing Aliases in a Database Table

With the JPA layer set up, `SqlAliasStore` keeps aliases in their own table
(`<prefix>command_aliases`) on SQLite or MySQL/MariaDB:

```java
AliasStore store = new SqlAliasStore(repositoryController, "myplugin_");
CommandAliasManager aliases = new CommandAliasManager(registry, store);
aliases.loadAll();
```

The table is created automatically. Only use letters, digits and `_` in the prefix.

---

## 7. An Admin Command to Manage Aliases

Putting it together with the builders from the [Basic Tutorial](command-system-basic.md):

```java
registry.command("alias")
        .permission("myplugin.alias")
        .sub(new SubCommandBuilder("alias", "create")
                        .arguments(Argument.word("name"), Argument.text("command"))
                        .executes((sender, args) -> {
                            aliases.create(CommandAlias.of(args.string(0), args.joined(1)));
                            sender.sendMessage("Created /" + args.string(0));
                            return true;
                        }).build(),
                new SubCommandBuilder("alias", "remove")
                        .arguments(Argument.custom("name", s -> aliases.list().stream().map(CommandAlias::name).toList()))
                        .executes((sender, args) -> {
                            sender.sendMessage(aliases.remove(args.string(0)) ? "Removed." : "No such alias.");
                            return true;
                        }).build(),
                new SubCommandBuilder("alias", "list")
                        .executes((sender, args) -> {
                            aliases.list().forEach(a -> sender.sendMessage("/" + a.name() + " -> /" + a.command()));
                            return true;
                        }).build())
        .register();
```

`create` throws `IllegalStateException`/`IllegalArgumentException` for invalid aliases; the command system
reports that to the sender as an error, or catch it to send your own message.

---

## 8. Read-Only Aliases from a Section

For aliases that are part of your plugin's configuration and never change at runtime, register them from a
section. The section is only read, never written:

```java
registry.loadAliases(getConfig().getConfigurationSection("Aliases"));
```

Each key is either `name: "command"` or a section with the keys from step 5. Invalid entries are logged and
skipped. Use step 4 instead whenever aliases can change while the server runs.

---

## 9. Safety Checks

- **Self reference**: an alias whose command is itself (or one of its own extra names) is rejected.
- **Loops**: aliases calling each other stop after `CommandTemplate.MAX_DISPATCH_DEPTH` (5) levels; the
  sender gets `Player.Commands.AliasFailed` instead of the server hanging.
- **Unknown target**: if the target command does not exist, the sender gets `Player.Commands.AliasFailed`.
- **Missing arguments**: a missing `{0}`/`{1}` gives the usual `Player.Commands.MissingArgument` message.
- **Console aliases** need a permission.

---

## 10. Commands in Menus

The same templates power menu buttons:

```java
registerButton(11, spawnItem, MenuAction.command("spawn"));
registerButton(12, kitItem, MenuAction.consoleCommand("give {player} diamond 1").requires("shop.diamond"));
```

See the [Menu Tutorial](menu-system-tutorial.md#7-navigation-chat-prompts-and-commands).
