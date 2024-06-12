package de.happybavarian07.adminpanel.language.mysql.controller;/*
 * @Author HappyBavarian07
 * @Date 08.05.2024 | 16:43
 */

import de.happybavarian07.adminpanel.main.AdminPanelMain;
import de.happybavarian07.adminpanel.utils.LogPrefix;
import org.mariadb.jdbc.Driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Level;

public class ConnectionManager {
    private final Connection connection;
    private final String ignoreString;
    private final AdminPanelMain plugin;
    private final String tablePrefix;
    private ConnectionManager instance;

    public ConnectionManager(AdminPanelMain plugin, File propertiesOrDatabaseFile, boolean isPropertiesFile, String mysqlSchema) {
        instance = this;

        this.plugin = plugin;

        if (mysqlSchema.equals("mysql") || mysqlSchema.equals("mariadb")) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                ignoreString = "IGNORE";
            } catch (ClassNotFoundException ignored) {
                throw new RuntimeException("Failed to load the MySQL driver while trying to determine the schema type of the database");
            }
        } else if (mysqlSchema.equals("sqlite")) {
            ignoreString = "OR IGNORE";
        } else {
            // Use the mysql schema as default and hope for the best
            ignoreString = "IGNORE";
        }

        if (isPropertiesFile) {
            Properties props = new Properties();
            if (mysqlSchema.equals("sqlite")) {
                // Load the properties file and create a connection to the database
                try (FileInputStream fis = new FileInputStream(propertiesOrDatabaseFile)) {
                    props.load(fis);
                    // Get URL from properties file and create a connection to the database
                    String url = "jdbc:sqlite:" + Paths.get(plugin.getDataFolder().getAbsolutePath(), props.getProperty("sqlite_path_language"));
                    tablePrefix = props.getProperty("mysql_prefix");
                    this.connection = DriverManager.getConnection(url);
                } catch (IOException | SQLException e) {
                    // Error Logging
                    plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to create a connection to the database using the properties file: " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
                    throw new RuntimeException("Failed to create a connection to the database using the properties file");
                }
            } else {
                // Load the properties file and create a connection to the database
                try (FileInputStream fis = new FileInputStream(propertiesOrDatabaseFile)) {
                    props.load(fis);
                    String url = "jdbc:" +
                            props.getProperty("dbtype") + "://" +
                            props.getProperty("mysql_host") + ":" +
                            props.getProperty("mysql_port") + "/" +
                            props.getProperty("mysql_database");
                    String user = props.getProperty("mysql_user");
                    String password = props.getProperty("mysql_password");
                    tablePrefix = props.getProperty("mysql_prefix");
                    DriverManager.registerDriver(new Driver());
                    this.connection = DriverManager.getConnection(url, user, password);
                } catch (IOException | SQLException e) {
                    plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to create a connection to the database using the properties file: " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
                    throw new RuntimeException("Failed to create a connection to the database using the properties file");
                }
            }
        } else {
            // Create a connection to the database file and create the tables if they don't exist
            String url = "jdbc:sqlite:" + propertiesOrDatabaseFile.getAbsolutePath();
            tablePrefix = "adminpanel_";
            if (!propertiesOrDatabaseFile.exists()) {
                try {
                    propertiesOrDatabaseFile.createNewFile();
                } catch (IOException e) {
                    plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to create the database file: " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
                    throw new RuntimeException("Failed to create the database file");
                }
            }
            try {
                this.connection = DriverManager.getConnection(url);
                DriverManager.drivers().forEach(driver -> System.out.println(driver.getClass().getName()));
            } catch (SQLException e) {
                plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to create a connection to the database using the properties file: " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
                throw new RuntimeException("Failed to create a connection to the database using the database file");
            }
        }
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public Connection getConnection() {
        return connection;
    }

    public String getIgnoreString() {
        return ignoreString;
    }

    public ConnectionManager getInstance() {
        if (instance == null)
            instance = this;

        return instance;
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to close the connection to the database: " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
        }
    }

    public PreparedStatement prepareStatement(String query) {
        try {
            if (tablePrefix.matches("[a-zA-Z0-9_]*") && ignoreString.matches("[a-zA-Z0-9_]*")) {
                query = query.replace("{table_prefix}", tablePrefix).replace("{ignore}", ignoreString);
                return connection.prepareStatement(query);
            } else {
                throw new IllegalArgumentException("Invalid tablePrefix or ignoreString");
            }
        } catch (SQLException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to prepare the statement: " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
            return null;
        }
    }

    public void createTable(String query) {
        try {
            if (tablePrefix.matches("[a-zA-Z0-9_]*") && ignoreString.matches("[a-zA-Z0-9_]*")) {
                query = query.replace("{table_prefix}", tablePrefix).replace("{ignore}", ignoreString);
                connection.prepareStatement(query).execute();
            } else {
                throw new IllegalArgumentException("Invalid tablePrefix or ignoreString");
            }
        } catch (SQLException e) {
            plugin.getFileLogger().writeToLog(Level.SEVERE, "Failed to create the table: " + e, LogPrefix.ACTIONSLOGGER_PLUGIN);
        }
    }

    public int executeUpdate(String query) throws SQLException {
        return prepareStatement(query).executeUpdate();
    }

    public int executeUpdate(String query, Object... args) throws SQLException {
        PreparedStatement pstmt = prepareStatement(query);
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
        return pstmt.executeUpdate();
    }

    public ResultSet executeQuery(String query) throws SQLException {
        return prepareStatement(query).executeQuery();
    }

    public ResultSet executeQuery(String query, Object... args) throws SQLException {
        PreparedStatement pstmt = prepareStatement(query);
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
        return pstmt.executeQuery();
    }
}
