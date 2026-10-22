# Permission Manager

Design for replacing the per-player permission list with a permission manager that has groups, inheritance, temporary entries,
contexts and an audit trail, plus its own menu section and `/perms` command.

## Goals

- Groups with weights and inheritance, per-player and per-group permission entries, `true` and `false` (negation).
- Temporary permissions and temporary group membership (expiry time).
- Contexts: an entry can be limited to a world; other plugins can add more kinds of context.
- Wildcards (`a.b.*`, `*`) with the rule "the more specific entry wins".
- An explanation for every answer: which entry of which user or group decided it (`/perms user <p> check <node>`).
- Prefix and suffix per user and per group.
- Everything done through the menus can be done through `/perms`, and the other way round.
- Every change goes to the admin journal (category `Permissions`), can be switched off, and is announced as a Bukkit event.
- Extendable: a public service, change events, context providers.
- Vault: Admin-Panel registers as the Vault permission and chat provider when Vault is installed.
- Export and import as YAML.

Not goals: a web editor, network-wide sync beyond what the data client already does, per-server contexts.

## Data model

Everything is a node: `permission`, `value`, `expiresAt` (0 = never), `context` (`world=nether;gamemode=creative`, empty = everywhere).
A group membership or a group parent is the node `group.<name>`. That keeps storage, expiry, contexts and audit in one place.

Tables (CoolStuffLib JPA, same database as the rest):

| Table | Columns | Notes |
|---|---|---|
| `player_permissions` (exists) | entry_id, player_uuid, permission, value, **expires_at**, **context** | the two new columns are added by the library's schema update; old rows are global and permanent |
| `permission_groups` | name (PK), display_name, weight, prefix, suffix, created_at | `default` is created at start and every player inherits it |
| `permission_group_nodes` | entry_id (PK), group_name, permission, value, expires_at, context | parents are nodes `group.<name>` |

The entry id is a hash of owner, permission and context, so the same node can exist once per context.

## Resolution

For a player and a context set (world plus whatever providers add):

1. Drop nodes whose expiry passed or whose context does not match.
2. Order the sources: the player's own nodes first, then the player's groups by weight (higher first, then name), each followed by its
   parents in the same order, depth first; every group only once (cycles are cut and reported); the `default` group last.
3. For an exact permission the first source that defines it decides.
4. If no source defines it exactly, wildcards are tried from the most specific to `*`, again in source order.
5. Bukkit has no wildcards, so the result is expanded against the permissions registered on the server (plus children) and written
   to one `PermissionAttachment`. Permissions that no plugin registered get no wildcard expansion; this is the one limit compared to
   a permission plugin that replaces the permissible, and it is documented.

`explain(player, node)` returns the same walk as a list of steps for the check command and the menu.

## Runtime

`PermissionService` holds the data in memory (loaded at start), writes through a single thread so a slow database never blocks a
click, recalculates the affected online players after a change, sweeps expired entries every minute, and re-applies on join, quit and
world change. `PermissionsManager` stays as a thin facade with the old method names so the data client, the player listener and the
old menus keep working.

Extension points:

- `PermissionContextProvider` (key, current values for a player) registered on the service.
- Bukkit events `PermissionChangeEvent` and `GroupChangeEvent`, fired after the change.
- `AdminPanelAPI.getPermissionService()` for other plugins.

## Commands

`/perms` (aliases `/apperms`, `/permissions`), one permission per sub command under `AdminPanel.Permissions.*`:

```
/perms menu
/perms user <player> info | check <node> | clear
/perms user <player> set <node> <true|false> [duration] [world]
/perms user <player> unset <node> [world]
/perms user <player> addgroup <group> [duration] [world] | removegroup <group> [world]
/perms group list
/perms group <name> create | delete | info
/perms group <name> set <node> <true|false> [duration] [world] | unset <node> [world]
/perms group <name> addparent <group> | removeparent <group>
/perms group <name> weight <number> | prefix <text> | suffix <text> | displayname <text>
/perms search <text>            (the permission search over everything the server registers)
/perms export | import <file>
/perms sync                     (reload from the database and apply to everyone online)
```

## Menus

A new hub in the start menu, and the Permissions button of the player menu jumps to that player's page:

- **Permission Manager** (hub): Players, Groups, Browse permissions, Audit log, Tools.
- **Players**: searchable list; **Player page**: groups, own entries, effective permissions with their source, add entry, add group, check
  a node, clear.
- **Groups**: list by weight, create; **Group page**: weight, prefix, suffix, entries, parents, members, delete.
- **Entry list** (used by both): left click flips true and false, right click removes, shift left sets an expiry, middle sets the world.
- **Browse permissions**: the existing permission tree, now used to pick the node for an entry.
- **Audit log**: the journal limited to permission actions, with search.

## Switching things off

- `Journal.Enabled` and `Journal.Record.<Category>`: every kind of recorded action can be switched off on its own; the permission
  manager uses the category `Permissions`. The log file categories under `Plugin.LogActions` are honoured now (they were ignored):
  a switched off kind is not written, warnings and errors always are. The new kinds `PERMISSIONS` and `JOURNAL` exist.
- `Permissions.Enabled` (default true) turns the whole manager off; the old behaviour (player entries only) stays available through the
  facade. `Permissions.Vault`, `Permissions.WorldChangeRecalculate` and `Permissions.SweepSeconds` are plain options.

## Search index

The permission search is rebuilt only when the set of permissions or their descriptions changed (a hash is compared), not every six
minutes. A persisted Lucene directory was rejected: the index builds in about 0.4 s for 2000 permissions, so persisting it would add
a stale-index problem for no real gain.

## Phases

1. Engine: model, resolver, explanation, wildcard expansion, duration parsing, tests.
2. Storage and service: entities, repositories, service, facade, migration, sweep, events, journal, config options.
3. Commands: `/perms`.
4. Menus: hub, players, groups, entries, audit, start menu and player menu links.
5. Vault provider, export and import, search index change.
6. Wiki, release notes, smoke checks.
