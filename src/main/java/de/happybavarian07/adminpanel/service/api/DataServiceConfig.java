package de.happybavarian07.adminpanel.service.api;

import de.happybavarian07.coolstufflib.service.api.Config;

import java.util.HashMap;
import java.util.Map;

/*
 * @Author HappyBavarian07
 * @Date September 29, 2025 | 20:18
 */
public class DataServiceConfig implements Config {
    private final Map<String, Object> configMap;

    public DataServiceConfig(String mode, int port, String host, String database, String user, String password) {
        this.configMap = new HashMap<>();
        configMap.put("mode", mode);
        configMap.put("port", port);
        configMap.put("host", host);
        configMap.put("database", database);
        configMap.put("user", user);
        configMap.put("password", password);
    }

    public DataServiceConfig(String mode, String storagePath) {
        this.configMap = new HashMap<>();
        configMap.put("mode", mode);
        configMap.put("storagePath", storagePath);
    }

    @Override
    public String getString(String s) {
        return configMap.getOrDefault(s, "").toString();
    }

    @Override
    public int getInt(String s) {
        return configMap.getOrDefault(s, 0) instanceof Integer ? (int) configMap.get(s) : 0;
    }

    @Override
    public boolean getBoolean(String s) {
        return configMap.getOrDefault(s, false) instanceof Boolean && (boolean) configMap.get(s);
    }

    @Override
    public Object get(String s) {
        return configMap.getOrDefault(s, null);
    }
}
