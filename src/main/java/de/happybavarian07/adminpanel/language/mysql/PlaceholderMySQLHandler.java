package de.happybavarian07.adminpanel.language.mysql;/*
 * @Author HappyBavarian07
 * @Date 25.02.2024 | 13:01
 */

import de.happybavarian07.adminpanel.language.Placeholder;
import de.happybavarian07.adminpanel.language.PlaceholderType;
import de.happybavarian07.adminpanel.language.mysql.controller.SerializationManager;
import de.happybavarian07.adminpanel.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class PlaceholderMySQLHandler {
    private final MySQLLanguageManager lgm;
    private final LanguageDatabaseController databaseController;
    // Only gets filled when Offline Content Map Mode is enabled in the LanguageDatabaseController
    private final Map<String, Placeholder> offlinePlaceholderMap;
    private final String tablePrefix;

    public PlaceholderMySQLHandler(MySQLLanguageManager lgm, LanguageDatabaseController databaseController) {
        this.lgm = lgm;
        this.databaseController = databaseController;
        this.offlinePlaceholderMap = new HashMap<>();
        this.tablePrefix = databaseController.getInnerLanguageManager().getConnectionManager().getTablePrefix();
    }

    public void setupDatabaseTable() {
        try {
            // Create a table with this schema
            // (id INT AUTO_INCREMENT PRIMARY KEY, placeholder_key VARCHAR(255) NOT NULL, placeholder_value TEXT NOT NULL, placeholder_type ENUM('MESSAGE', 'ITEM', 'MENUTITLE', 'ALL') NOT NULL, UNIQUE (placeholder_key))
            databaseController.getConnectionManager().executeUpdate("CREATE TABLE IF NOT EXISTS " + tablePrefix + "placeholders (" +
                    "PlaceholderKey VARCHAR(255) PRIMARY KEY NOT NULL, " +
                    "PlaceholderValueClassName VARCHAR(200), " +
                    "PlaceholderValue BLOB, " +
                    "PlaceholderType TEXT)");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadPlaceholderMap() {
        try {
            // Execute a SQL query to get the placeholders from the MySQL database
            ResultSet resultSet = databaseController.getConnectionManager().executeQuery("SELECT * FROM " + tablePrefix + "placeholders");

            // Iterate through the result set
            while (resultSet.next()) {
                String key = resultSet.getString("PlaceholderKey");
                byte[] value = resultSet.getBytes("PlaceholderValue");
                PlaceholderType type = PlaceholderType.valueOf(resultSet.getString("PlaceholderType"));
                String valueClassName = resultSet.getString("PlaceholderValueClassName");

                // Deserialize the value and put it into the offlinePlaceholderMap
                offlinePlaceholderMap.put(key, new Placeholder(key, SerializationManager.deserialize(value, Class.forName(valueClassName), false), type));
            }
        } catch (SQLException | ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addPlaceholder(PlaceholderType type, String key, Object value, boolean resetBefore) {
        try {
            if (resetBefore) {
                // Remove a placeholder from the table if the type matches or the Type is ALL (which means i want to search for all types during the deletion)
                resetSpecificPlaceholders(type, List.of(key));
            }
            // Check if the placeholder already exists
            ResultSet resultSet = databaseController.getConnectionManager().executeQuery(
                    "SELECT * FROM " + tablePrefix + "placeholders WHERE PlaceholderKey = ? AND PlaceholderType = ?",
                    key, type.name()
            );
            if (resultSet.next()) {
                // If it exists, update it
                PreparedStatement pstmt = databaseController.getConnectionManager().prepareStatement(
                        "UPDATE " + tablePrefix + "placeholders SET PlaceholderValue = ?, PlaceholderValueClassName = ? WHERE PlaceholderKey = ? AND PlaceholderType = ?");
                pstmt.setBytes(1, SerializationManager.serialize(value));
                pstmt.setString(2, value.getClass().getName());
                pstmt.setString(3, key);
                pstmt.setString(4, type.name());
                pstmt.executeUpdate();
            } else {
                // If it doesn't exist, insert it
                PreparedStatement pstmt = databaseController.getConnectionManager().prepareStatement(
                        "INSERT INTO " + tablePrefix + "placeholders (PlaceholderKey, PlaceholderValueClassName, PlaceholderValue, PlaceholderType) " +
                                "VALUES (?, ?, ?, ?)");
                pstmt.setString(1, key);
                pstmt.setString(2, value.getClass().getName());
                pstmt.setBytes(3, SerializationManager.serialize(value));
                pstmt.setString(4, type.name());
                pstmt.executeUpdate();
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addPlaceholders(Map<String, Placeholder> placeholders, boolean resetBefore) {
        if (resetBefore) resetSpecificPlaceholders(PlaceholderType.ALL, new ArrayList<>(placeholders.keySet()));
        // Insert a new placeholder into the table
        for (Map.Entry<String, Placeholder> entry : placeholders.entrySet()) {
            addPlaceholder(entry.getValue().getType(), entry.getKey(), entry.getValue().getValue(), resetBefore);
        }
    }

    public void removePlaceholder(PlaceholderType type, String key) {
        try {
            // Remove a placeholder from the table if the type matches or the Type is ALL (which means i want to search for all types during the deletion)
            databaseController.getConnectionManager().executeUpdate("DELETE FROM " + tablePrefix + "placeholders WHERE PlaceholderKey = ? AND (PlaceholderType = ? OR PlaceholderType = 'ALL')", key, type.name());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removePlaceholders(PlaceholderType type, List<String> keys) {
        if (keys.isEmpty()) {
            return;
        }

        // Create a comma-separated string of placeholders for the SQL query
        String placeholders = String.join(", ", Collections.nCopies(keys.size(), "?"));

        // Create the SQL query
        String query = "DELETE FROM " + tablePrefix + "placeholders WHERE (PlaceholderType = ? OR PlaceholderType = 'ALL') AND PlaceholderKey IN (" + placeholders + ")";

        try (PreparedStatement pstmt = databaseController.getConnectionManager().prepareStatement(query)) {

            // Set the type parameter
            pstmt.setString(1, type.toString());

            // Set the key parameters
            for (int i = 0; i < keys.size(); i++) {
                pstmt.setString(i + 2, keys.get(i));
            }

            // Execute the query
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void resetPlaceholders(PlaceholderType type, @Nullable List<String> excludeKeys) {
        try {
            // Execute a SQL query to get the placeholders from the MySQL database
            ResultSet resultSet = databaseController.getConnectionManager().executeQuery(
                    "SELECT PlaceholderKey FROM " + tablePrefix + "placeholders WHERE PlaceholderType = ? OR PlaceholderType = 'ALL'", type.name()
            );

            List<String> keysToRemove = new ArrayList<>();
            while (resultSet.next()) {
                String key = resultSet.getString("PlaceholderKey");
                String placeholderType = resultSet.getString("PlaceholderType");

                // If the key is in the excludeKeys list, skip this iteration
                if (excludeKeys != null && excludeKeys.contains(key)) continue;
                if (!placeholderType.equals(type.name()) && !placeholderType.equals(PlaceholderType.ALL.name()))
                    continue;

                // Add the key to the keysToRemove list
                keysToRemove.add(key);
            }

            // Call the removePlaceholders method with the type and keysToRemove as arguments
            removePlaceholders(type, keysToRemove);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void resetSpecificPlaceholders(PlaceholderType type, @Nullable List<String> includeKeys) {
        try {
            // Execute a SQL query to get the placeholders from the MySQL database
            ResultSet resultSet = databaseController.getConnectionManager().executeQuery(
                    "SELECT PlaceholderKey, PlaceholderType FROM " + tablePrefix + "placeholders WHERE PlaceholderType = ? OR PlaceholderType = 'ALL'", type.name());

            List<String> keysToRemove = new ArrayList<>();
            while (resultSet.next()) {
                String key = resultSet.getString("PlaceholderKey");
                PlaceholderType placeholderType = PlaceholderType.valueOf(resultSet.getString("PlaceholderType"));

                if (includeKeys != null && !includeKeys.contains(key)) continue;
                if (!placeholderType.equals(type) && !placeholderType.equals(PlaceholderType.ALL) && !type.equals(PlaceholderType.ALL))
                    continue;

                keysToRemove.add(key);
            }

            // Call the removePlaceholders method with the type and keysToRemove as arguments
            removePlaceholders(type, keysToRemove);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Placeholder> getPlaceholders() {
        Map<String, Placeholder> placeholders = new HashMap<>();
        try (ResultSet result = databaseController.getConnectionManager().executeQuery("SELECT * FROM " + tablePrefix + "placeholders")) {
            while (result.next()) {
                String key = result.getString("PlaceholderKey");
                byte[] value = result.getBytes("PlaceholderValue");
                PlaceholderType type = PlaceholderType.valueOf(result.getString("PlaceholderType"));
                String valueClassName = result.getString("PlaceholderValueClassName");

                // Deserialize the value and put it into the placeholders map
                placeholders.put(key, new Placeholder(key, SerializationManager.deserialize(value, Class.forName(valueClassName), false), type));
            }
        } catch (SQLException | ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
        return placeholders;
    }

    public Map<String, Placeholder> getPlaceholdersByType(PlaceholderType type) {
        Map<String, Placeholder> placeholders = new HashMap<>();
        try (ResultSet result = databaseController.getConnectionManager().executeQuery("SELECT * FROM " + tablePrefix + "placeholders WHERE PlaceholderType = ? OR PlaceholderType = 'ALL'", type.name())) {
            while (result.next()) {
                String key = result.getString("PlaceholderKey");
                byte[] value = result.getBytes("PlaceholderValue");
                PlaceholderType placeholderType = PlaceholderType.valueOf(result.getString("PlaceholderType"));
                String valueClassName = result.getString("PlaceholderValueClassName");

                if (!placeholderType.equals(type) && !placeholderType.equals(PlaceholderType.ALL)) continue;

                placeholders.put(key, new Placeholder(key, SerializationManager.deserialize(value, Class.forName(valueClassName), false), type));
            }
        } catch (SQLException | ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
        return placeholders;
    }

    public List<String> getPlaceholderKeysInMessage(String message, PlaceholderType type) {
        Map<String, Placeholder> placeholders = getPlaceholders();
        List<String> keys = new ArrayList<>();
        for (String key : placeholders.keySet()) {
            if (message.contains(key) && placeholders.get(key).getType().equals(type)) {
                keys.add(key);
            }
        }
        return keys;
    }

    public String replacePlaceholders(PlaceholderType type, String message) {
        Map<String, Placeholder> placeholders = getPlaceholdersByType(type);
        //System.out.println("Placeholders: " + placeholders);
        for (String key : placeholders.keySet()) {
            if (!placeholders.get(key).getType().equals(type) && !placeholders.get(key).getType().equals(PlaceholderType.ALL))
                continue;

            message = placeholders.get(key).replace(message);
        }
        return message;
    }

    public ItemStack replacePlaceholders(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        List<String> lore = meta.getLore();
        List<String> loreWithPlaceholders = new ArrayList<>();
        assert lore != null;
        for (String s : lore) {
            String temp = replacePlaceholders(PlaceholderType.ITEM, s);
            loreWithPlaceholders.add(Utils.format(player, temp, lgm.getPrefix()));
        }
        meta.setLore(loreWithPlaceholders);
        meta.setDisplayName(replacePlaceholders(PlaceholderType.ITEM, Utils.format(player, meta.getDisplayName(), lgm.getPrefix())));
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack replacePlaceholders(Player player, ItemStack item, Map<String, Placeholder> placeholders) {
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        List<String> lore = meta.getLore();
        List<String> loreWithPlaceholders = new ArrayList<>();
        assert lore != null;
        for (String s : lore) {
            String temp = replacePlaceholders(s, placeholders);
            loreWithPlaceholders.add(Utils.format(player, temp, lgm.getPrefix()));
        }
        meta.setLore(loreWithPlaceholders);
        meta.setDisplayName(replacePlaceholders(Utils.format(player, meta.getDisplayName(), lgm.getPrefix()), placeholders));
        item.setItemMeta(meta);
        return item;
    }


    public String replacePlaceholders(String message, Map<String, Placeholder> placeholders) {
        for (String key : placeholders.keySet()) {
            message = placeholders.get(key).replace(message);
        }
        return message;
    }

    public Map<String, Placeholder> getNewPlaceholderMap() {
        return new HashMap<>();
    }
}
