package de.happybavarian07.adminpanel.main;

import de.happybavarian07.coolstufflib.jpa.utils.DatabaseProperties;

import java.io.File;
import java.util.Locale;
import java.util.Properties;

/** Turns database.properties into connection properties for CoolStuffLib. */
public final class DatabaseSettings {
    private DatabaseSettings() {
    }

    public static DatabaseProperties parse(Properties in, File dataFolder) {
        DatabaseProperties props = new DatabaseProperties();
        String driver = in.getProperty("dbtype", "").trim().toLowerCase(Locale.ROOT);
        props.setDatabasePrefix(in.getProperty("database_prefix", "").trim());
        if (driver.equals("mysql") || driver.equals("mariadb")) {
            props.setDriver(driver);
            props.setHost(in.getProperty("mysql_host", "localhost").trim());
            props.setPort(validPort(in.getProperty("mysql_port", "3306")));
            props.setUsername(in.getProperty("mysql_user", "root"));
            props.setPassword(in.getProperty("mysql_password", ""));
            props.setDatabase(in.getProperty("mysql_database", "adminpanel").trim());
            return props;
        }
        props.setDriver("sqlite");
        String path = in.getProperty("sqlite_file_path", "").trim();
        File file = new File(path.isEmpty() ? "AdminPanelDatabase.db" : path);
        if (!file.isAbsolute()) file = new File(dataFolder, file.getPath());
        props.setDatabase(file.getAbsolutePath());
        return props;
    }

    private static String validPort(String port) {
        try {
            int value = Integer.parseInt(port.trim());
            return value > 0 && value <= 65535 ? String.valueOf(value) : "3306";
        } catch (NumberFormatException e) {
            return "3306";
        }
    }
}
