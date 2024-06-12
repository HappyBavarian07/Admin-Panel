package de.happybavarian07.webui.utils;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.UUIDDeserializer;
import com.fasterxml.jackson.databind.ser.std.UUIDSerializer;

import java.util.List;
import java.util.UUID;

public class User {
    @JsonDeserialize(using = UUIDDeserializer.class)
    @JsonSerialize(using = UUIDSerializer.class)
    private UUID uuid;
    private String salt;
    private String username;
    private String hashedPassword;
    private List<String> roles;

    public User() {
    }

    public User(UUID uuid, String username, String hashedPassword, List<String> roles) {
        this.uuid = uuid;
        this.salt = "";
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.roles = new java.util.ArrayList<>(roles); // Create a mutable copy of the roles list
    }

    public User(String uuid, String username, String hashedPassword, List<String> roles) {
        this.uuid = UUID.fromString(uuid);
        this.salt = "";
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.roles = new java.util.ArrayList<>(roles); // Create a mutable copy of the roles list
    }

    public String getUsername() {
        return username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public UUID getUUID() {
        return uuid;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean hasRole(String role, boolean ignoreCase) {
        if (ignoreCase) {
            for (String r : roles) {
                if (r.equalsIgnoreCase(role)) {
                    return true;
                }
            }
        } else {
            return hasRole(role);
        }
        return false;
    }

    public boolean addRole(String role) {
        if (!hasRole(role)) {
            roles.add(role);
            return true;
        }
        return false;
    }

    public boolean removeRole(String role) {
        if (hasRole(role)) {
            roles.remove(role);
            return true;
        }
        return false;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}