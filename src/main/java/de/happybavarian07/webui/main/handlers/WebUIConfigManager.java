package de.happybavarian07.webui.main.handlers;/*
 * @Author HappyBavarian07
 * @Date 09.05.2024 | 13:44
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Base64;

public class WebUIConfigManager {
    public String generateSecretKey(File secretKeyFile) {
        // Check if the secret key file exists and is not empty
        if (secretKeyFile.exists() && secretKeyFile.length() > 0) {
            // Load the secret key from the file
            try {
                return Files.readString(secretKeyFile.toPath());
            } catch (IOException e) {
                throw new RuntimeException("Failed to read secret key from file", e);
            }
        } else {
            // Generate a new secret key
            int keyLength = 2048;
            byte[] keyBytes = new byte[keyLength];
            new SecureRandom().nextBytes(keyBytes);
            String secretKey = Base64.getEncoder().encodeToString(keyBytes);

            // Save the secret key to the file
            try {
                Files.writeString(secretKeyFile.toPath(), secretKey);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save secret key to file", e);
            }

            return secretKey;
        }
    }
}
