package de.happybavarian07.adminpanel.mysql.utils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/*
 * @Author HappyBavarian07
 * @Date April 11, 2025 | 15:21
 */
public class DatabaseProperties {
    private final String host;
    private final int port;
    private final String databaseFilePath;
    private final String username;
    private final String password;
    private final String driver;
    private String databasePrefix;

    public DatabaseProperties(String host, int port, String databaseFilePath, String username, String password, String driver, String databasePrefix) {
        this.host = host;
        this.port = port;
        this.databaseFilePath = databaseFilePath;
        this.username = username;
        this.password = password;
        this.driver = driver;
        this.databasePrefix = databasePrefix;
    }

    public DatabaseProperties(String databaseFilePath, String username, String password) {
        this(null, -1, databaseFilePath, username, password, "sqlite", "");
    }

    public DatabaseProperties(String host, int port, String username, String password, String databasePrefix) {
        this(host, port, null, username, password, "mariadb", databasePrefix);
    }

    public String getConnectionString() {
        if (driver.isEmpty() || username.isEmpty() || password.isEmpty() ||
                (driver.equalsIgnoreCase("sqlite") && databaseFilePath.isEmpty()) ||
                ((driver.equalsIgnoreCase("mariadb") || driver.equalsIgnoreCase("mysql")) && (host.isEmpty() || port == -1))) {
            return "";
        }
        /*try {
            DriverManager.registerDriver(new JDBC());
            DriverManager.registerDriver(new Driver());
            System.out.println("Driver registered.");
            Enumeration<java.sql.Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                java.sql.Driver driver = drivers.nextElement();
                System.out.println("Driver: " + driver.getClass().getName());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }*/
        if (driver.equalsIgnoreCase("sqlite")) {
            System.out.println("Database file path: " + databaseFilePath);
            File file = new File(databaseFilePath);
            if (!file.exists()) {
                try {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException("Could not create SQLite database file: " + databaseFilePath, e);
                }
            }
            if (!file.isFile()) {
                throw new RuntimeException("The SQLite database file is not a file: " + databaseFilePath);
            }
            return "jdbc:" + driver.toLowerCase() + ":" + file.getAbsolutePath();
        } else if (driver.equalsIgnoreCase("mariadb") || driver.equalsIgnoreCase("mysql")) {
            return "jdbc:" + driver.toLowerCase() + "://" + host + ":" + port;
        }
        return "";
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabaseFilePath() {
        return databaseFilePath;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabasePrefix() {
        if (databasePrefix == null) {
            return "";
        }
        return databasePrefix;
    }

    public String getDriver() {
        return driver;
    }

    public void setDatabasePrefix(String prefix) {
        this.databasePrefix = Objects.requireNonNullElse(prefix, "");
    }
}
