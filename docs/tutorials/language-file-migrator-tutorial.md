# LanguageFileMigrator Integration Tutorial

## Overview
LanguageFileMigrator helps you keep your language config files up-to-date with the latest resource files, detecting missing or changed keys and allowing safe, selective migration. It integrates with LanguageManager for easy access and workflow.

## Creating a Migrator
To create a migrator for a specific language:

<pre><code>LanguageFileMigrator migrator = languageManager.createMigratorForLanguage("de");
</code></pre>

## Migration Workflow
1. **Check for Differences**
   <pre><code>if (migrator.filesDifferByHash()) {
    List<LanguageFileMigrator.MigrationEntry> entries = migrator.getMigrationEntries();
    // Present entries to user for review
}
</code></pre>

2. **Review Migration Entries**
   Each entry contains:
   - `key`: The config key
   - `userValue`: Value in the current config
   - `resourceValue`: Value in the resource file
   - `status`: MISSING_IN_USER, DIFFERENT_VALUE, UNCHANGED
   - `selectedForMigration`: Whether to migrate this entry

3. **Edit or Select Entries**
   <pre><code>for (LanguageFileMigrator.MigrationEntry entry : entries) {
    if (entry.getStatus() == LanguageFileMigrator.MigrationStatus.DIFFERENT_VALUE) {
        entry.setSelectedForMigration(true); // or false to skip
        entry.setUserValue("Custom Value"); // optional: edit before migrating
    }
}
</code></pre>

4. **Migrate Selected Entries**
   <pre><code>migrator.migrateSelected();
</code></pre>

## Example: Full Migration
<pre><code>LanguageFileMigrator migrator = languageManager.createMigratorForLanguage("de");
if (migrator.filesDifferByHash()) {
    List<LanguageFileMigrator.MigrationEntry> entries = migrator.getMigrationEntries();
    for (LanguageFileMigrator.MigrationEntry entry : entries) {
        entry.setSelectedForMigration(true);
    }
    migrator.migrateSelected();
}
</code></pre>

## Notes
- Migration never removes keys from your config, only adds or updates them.
- You can review and edit entries before migration.
- Use this system after plugin updates or when adding new language features.

## API Reference
- `LanguageManager.createMigratorForLanguage(String langName)`
- `LanguageFileMigrator.filesDifferByHash()`
- `LanguageFileMigrator.getMigrationEntries()`
- `LanguageFileMigrator.migrateSelected()`
- `LanguageFileMigrator.MigrationEntry` (fields: key, userValue, resourceValue, status, selectedForMigration)

## Integration
This system is designed for incremental adoption and is compatible with legacy language files. Use it to keep your language files in sync and avoid manual updates.

