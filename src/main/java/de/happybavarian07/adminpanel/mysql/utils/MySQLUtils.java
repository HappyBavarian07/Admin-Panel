package de.happybavarian07.adminpanel.mysql.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLUtils {
    public static Connection createConnection(String url, String user, String password) throws SQLException {
        if (url.contains("sqlite")) {
            return DriverManager.getConnection(url);
        }
        return DriverManager.getConnection(url, user, password);
    }
}
