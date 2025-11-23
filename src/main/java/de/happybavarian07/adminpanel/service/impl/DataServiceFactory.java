package de.happybavarian07.adminpanel.service.impl;

import de.happybavarian07.adminpanel.service.api.DataService;
import de.happybavarian07.adminpanel.service.api.DataServiceConfig;
import de.happybavarian07.coolstufflib.service.api.ServiceDescriptor;
import de.happybavarian07.coolstufflib.service.api.ServiceFactory;
import de.happybavarian07.coolstufflib.service.api.ServiceRegistry;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/*
 * @Author HappyBavarian07
 * @Date September 29, 2025 | 20:15
 */
public class DataServiceFactory implements ServiceFactory<DataService> {
    private final DataServiceConfig config;
    private final ServiceDescriptor serviceDescriptor;

    public DataServiceFactory(DataServiceConfig config) {
        this.config = config;
        this.serviceDescriptor = new ServiceDescriptor(UUID.randomUUID(), "AdminPanel-DataService", List.of(), Duration.of(15, ChronoUnit.SECONDS), Duration.of(15, ChronoUnit.SECONDS));
    }

    public DataServiceConfig getConfig() {
        return config;
    }

    public ServiceDescriptor getServiceDescriptor() {
        return serviceDescriptor;
    }

    @Override
    public CompletableFuture<DataService> create(ServiceRegistry serviceRegistry) {
        /*if(config.getString("mode").equalsIgnoreCase("mysql")) {
            return CompletableFuture.supplyAsync(() -> new MySQLDataService(
                    config.getString("host"),
                    config.getInt("port"),
                    config.getString("database"),
                    config.getString("user"),
                    config.getString("password")
            ));
        } else */
        if (config.getString("mode").equalsIgnoreCase("sqlite")) {
            return CompletableFuture.supplyAsync(() -> new SQLiteDataService(
                    config.getString("storagePath")
            ));
        } else if (config.getString("mode").equalsIgnoreCase("file")) {
            return CompletableFuture.supplyAsync(() -> new FileDataService(
                    config.getString("storagePath")
            ));
        } else {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Unsupported mode: " + config.getString("mode")));
        }
    }
}
