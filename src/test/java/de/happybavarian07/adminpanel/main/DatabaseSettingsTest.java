package de.happybavarian07.adminpanel.main;

import de.happybavarian07.coolstufflib.jpa.utils.DatabaseProperties;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseSettingsTest {
    private final File dataFolder = new File("plugins/Admin-Panel").getAbsoluteFile();

    @Test
    void missingValuesFallBackToSqliteInTheDataFolder() {
        DatabaseProperties props = DatabaseSettings.parse(new Properties(), dataFolder);
        assertEquals("sqlite", props.getDriver());
        assertEquals(new File(dataFolder, "AdminPanelDatabase.db").getAbsolutePath(), props.getDatabase());
        assertEquals("", props.getDatabasePrefix());
    }

    @Test
    void relativeSqlitePathIsResolvedAgainstTheDataFolder() {
        Properties in = new Properties();
        in.setProperty("dbtype", "sqlite");
        in.setProperty("sqlite_file_path", "data/panel.db");
        assertEquals(new File(dataFolder, "data/panel.db").getAbsolutePath(), DatabaseSettings.parse(in, dataFolder).getDatabase());
    }

    @Test
    void mysqlKeysAreRead() {
        Properties in = new Properties();
        in.setProperty("dbtype", "mariadb");
        in.setProperty("mysql_host", "db.example");
        in.setProperty("mysql_port", "3307");
        in.setProperty("mysql_user", "panel");
        in.setProperty("mysql_password", "secret");
        in.setProperty("mysql_database", "adminpanel");
        in.setProperty("database_prefix", "ap_");
        DatabaseProperties props = DatabaseSettings.parse(in, dataFolder);
        assertEquals("mariadb", props.getDriver());
        assertEquals("db.example", props.getHost());
        assertEquals("3307", props.getPort());
        assertEquals("panel", props.getUsername());
        assertEquals("secret", props.getPassword());
        assertEquals("adminpanel", props.getDatabase());
        assertEquals("ap_", props.getDatabasePrefix());
    }

    @Test
    void invalidPortFallsBackToTheDefault() {
        Properties in = new Properties();
        in.setProperty("dbtype", "mysql");
        in.setProperty("mysql_port", "abc");
        assertEquals("3306", DatabaseSettings.parse(in, dataFolder).getPort());
    }
}
